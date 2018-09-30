package com.oz.segmentio.json;

import com.oz.segmentio.avro.AdEventLoadType;
import com.oz.segmentio.avro.AdType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.avro.Schema;
import org.apache.avro.specific.SpecificRecordBase;
import org.joda.time.format.ISODateTimeFormat;
import org.reflections.Reflections;

public final class JsonToAvroUtils {

    private static final Collection<Schema.Type> NUMERIC_AVRO_TYPES = Arrays.asList(
        Schema.Type.DOUBLE, Schema.Type.FLOAT, Schema.Type.INT, Schema.Type.LONG
    );

    private static final String ESCAPED_SPACE = "__escape_space__";

    private JsonToAvroUtils() {
        // Utility Class
    }

    public static Collection<Class<? extends SpecificRecordBase>> allSchemata() {
        Reflections reflections = new Reflections("com.oz.segmentio.avro");
        return reflections.getSubTypesOf(SpecificRecordBase.class);
    }

    /**
     * Replaces all spaces in JSON map keys with {@link #ESCAPED_SPACE} since Avro does not support
     * spaces in keys.
     * @param map Map to sanitize keys in
     * @return Map with sanitized keys
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> sanitizeJsonKeysDeep(Map<String, Object> map) {
        Map<String, Object> result = new HashMap<>();
        map.forEach(
            (key, value) -> result.put(
                maybeRenameKey(key.replaceAll(" ", ESCAPED_SPACE)),
                value instanceof Map ? sanitizeJsonKeysDeep((Map<String, Object>) value) : value
            )
        );
        return result;
    }

    /**
     * Adjusts numeric types in gives map, so that they match what is demanded by the Avro schema.
     * Jackson's Avro implementation unfortunately does not automatically do this conversion, so we
     * have to.
     * TODO: Raise and issue with Jackson AVRO implementation?
     * TODO: Dry up this method.
     * TODO: This is not a complete conversion implementation, only relevant paths for tests have
     * been implemented so far!
     * @param map Map with numeric types to adjust
     * @return Map with numeric types adjusted
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> sanitizeNumericTypes(Schema avro, Map<String, Object> map) {
        Map<String, Object> result = new HashMap<>();
        map.forEach((key, value) -> {
            Schema.Field relevantField = avro.getField(key);
            if (relevantField == null) {
                throw new SchemaIncompatibilityException("Found key [" + key + "] in JSON data but it's not part of the AVRO schema.");
            }
            Schema.Type relevantType = relevantField.schema().getType();
            if (value instanceof Number) {
                if (relevantType == Schema.Type.UNION) {
                    Schema.Type unionType = extractUnionType(relevantField);
                    if (NUMERIC_AVRO_TYPES.contains(unionType)) {
                        putAdjustedNumericValue(result, key, (Number) value, unionType);
                    } else if (key.endsWith("_date")) {
                        //TODO: This is very hacky, identify date fields more safely by type lookup from some provided metadata
                        result.put(key, ISODateTimeFormat.dateTimeNoMillis().print(((Number) value).longValue()));
                    } else if (unionType == Schema.Type.STRING) {
                        result.put(key, String.valueOf(value));
                    } else {
                        throw new IllegalArgumentException(
                            "Numeric type sanitization for non numeric union type not supported yet."
                        );
                    }
                } else if (NUMERIC_AVRO_TYPES.contains(relevantType)) {
                    putAdjustedNumericValue(result, key, (Number) value, relevantType);
                } else {
                    throw new IllegalArgumentException("Type conflict! Found numeric type in map but schema specifies non numeric type");
                }
            } else if (relevantType == Schema.Type.RECORD) {
                result.put(key, sanitizeNumericTypes(relevantField.schema(), (Map<String, Object>) value));
            } else if (relevantType == Schema.Type.UNION) {
                Schema.Type nestedType = extractUnionType(relevantField);
                if (nestedType == Schema.Type.STRING) {
                    // cast to enforce type
                    result.put(key, String.valueOf(value));
                } else if (nestedType == Schema.Type.RECORD) {
                    result.put(
                        key,
                        sanitizeNumericTypes(extractUnionSchema(relevantField), (Map<String, Object>) value)
                    );
                } else if (nestedType == Schema.Type.MAP) {
                    Schema elementSchema = extractUnionSchema(relevantField).getValueType();
                    Schema.Type elementType = elementSchema.getType();
                    if (NUMERIC_AVRO_TYPES.contains(elementType)) {
                        throw new IllegalArgumentException(
                            "Numeric type sanitization for nested [map] of [numeric] not yet supported."
                        );
                    } else {
                        result.put(key, value);
                    }
                } else if (nestedType == Schema.Type.ARRAY) {
                    Schema elementSchema = extractUnionSchema(relevantField).getElementType();
                    if (elementSchema.getType() == Schema.Type.STRING) {
                        result.put(key, value);
                    } else {
                        throw new IllegalArgumentException(
                            "Numeric type sanitization for nested [" + elementSchema + "] in [ARRAY] not yet supported."
                        );
                    }
                } else if (nestedType == Schema.Type.BOOLEAN) {
                    result.put(key, value);
                } else if (nestedType == Schema.Type.ENUM) {
                    result.put(key, value instanceof Enum ? value : stringToEnum((String) value));
                } else {
                    throw new IllegalArgumentException(
                        "Numeric type sanitization for nested [" + nestedType + "] not yet supported."
                    );
                }
            } else if (relevantType == Schema.Type.STRING) {
                // cast to enforce type
                result.put(key, String.valueOf(value));
            } else if (relevantType == Schema.Type.BOOLEAN) {
                // cast to enforce type
                result.put(key, Boolean.valueOf((boolean) value));
            } else if (relevantType == Schema.Type.ARRAY) {
                Schema elementSchema = relevantField.schema().getElementType();
                Schema.Type elementType = elementSchema.getType();
                if (elementType == Schema.Type.RECORD) {
                    List<Object> foundArray = (List<Object>) value;
                    int size = foundArray.size();
                    ArrayList<Object> resultArray = new ArrayList<>(size);
                    for (int i = 0; i < size; ++i) {
                        resultArray.add(i, sanitizeNumericTypes(elementSchema, (Map<String, Object>) foundArray.get(i)));
                    }
                    result.put(key, resultArray);
                } else if (elementType == Schema.Type.STRING) {
                    result.put(key, value);
                } else if (elementType == Schema.Type.ENUM) {
                    result.put(
                        key,
                        ((Collection<String>) value).stream()
                            .map((Function<String, Object>) JsonToAvroUtils::stringToEnum)
                            .collect(Collectors.toList())
                    );
                } else {
                    throw new IllegalArgumentException(
                        "Numeric type sanitization for nested array value type [" + elementType + "] not yet supported."
                    );
                }
            } else if (relevantType == Schema.Type.ENUM) {
                result.put(key, value instanceof Enum ? value : stringToEnum((String) value));
            } else {
                throw new IllegalArgumentException(
                    "Numeric type sanitization for nested [" + relevantType + "] not yet supported."
                );
            }
        });
        return result;
    }

    private static Schema extractUnionSchema(Schema.Field field) {
        return field.schema().getTypes().stream()
            .filter(schema -> schema.getType() != Schema.Type.NULL)
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("No nested type found"));
    }

    private static Schema.Type extractUnionType(Schema.Field field) {
        List<Schema.Type> types = field.schema().getTypes().stream()
            .map(Schema::getType).filter(t -> t != Schema.Type.NULL)
            .collect(Collectors.toList());
        if (types.size() != 1) {
            throw new IllegalArgumentException(
                "Should only have one optional type besides [null] for [" + field + "]."
            );
        }
        return types.get(0);
    }

    private static void putAdjustedNumericValue(Map<String, Object> result, String key, Number value,
        Schema.Type relevantType) {
        switch (relevantType) {
            case FLOAT:
                result.put(key, value.floatValue());
                break;
            case DOUBLE:
                result.put(key, value.doubleValue());
                break;
            case INT:
                result.put(key, value.intValue());
                break;
            case LONG:
                result.put(key, value.longValue());
                break;
        }
    }

    /**
     * Converts known {@link String}s to their correct representation as an Avro generated {@link Enum}.
     * @param string String to convert
     * @return Enum
     */
    private static Enum<?> stringToEnum(String string) {
        switch (string) {
            case "mid-roll":
                return AdType.MID_ROLL;
            case "post-roll":
                return AdType.POST_ROLL;
            case "pre-roll":
                return AdType.PRE_ROLL;
            case "none":
                return AdType.NONE;
            case "dynamic":
                return AdEventLoadType.DYNAMIC;
            case "linear":
                return AdEventLoadType.LINEAR;
            default:
                throw new IllegalArgumentException("Unknown ENUM value [" + string + "]");
        }
    }

    private static String maybeRenameKey(String key) {
        switch (key) {
            case "sessionId":
                return "session_id";
            case "fullEpisode":
                return "full_episode";
            case "totalLength":
                return "total_length";
            case "podId":
                return "pod_id";
            case "assetId":
                return "asset_id";
            default:
                return key;
        }
    }
}
