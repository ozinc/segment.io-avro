package com.oz.segmentio.avro;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.common.io.Resources;
import com.oz.segmentio.json.JsonToAvroUtils;
import com.oz.segmentio.json.SchemaIncompatibilityException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.avro.Schema;
import org.apache.avro.specific.SpecificRecordBase;
import org.junit.Test;

import static com.oz.segmentio.avro.FullCycleTests.assertFullCycle;
import static org.junit.Assert.fail;

public final class IntegrationTests {

    @Test
    public void runCases() throws Exception {
        Collection<Class<? extends SpecificRecordBase>> schemata = new ArrayList<>(JsonToAvroUtils.allSchemata());
        if (!schemata.remove(Tracking.class)) {
            fail(
                "Aggregate Tracking Schema not found. " +
                    "If you are running these tests from an IDE please invoke `mvn clean test` instead to rebuild AVRO schemata."
            );
        }
        // Add aggregate Tracking event at the end of the classes to test so it's used as a fallback should no specific class match
        schemata.add(Tracking.class);
        Path resourceTestCases = Paths.get(Resources.getResource(IntegrationTests.class, "").toURI());
        Path customTestCases = Paths.get(System.getProperty("com.oz.segmentio.avro.projectRoot", ".")).resolve("testCases");
        for (final Path cases : Arrays.asList(resourceTestCases, customTestCases)) {
            try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(cases)) {
                directoryStream.forEach(file -> {
                    if (file.toString().endsWith(".json")) {
                        try {
                            for (byte[] raw : maybeFlattenBatch(Files.readAllBytes(file))) {
                                String type = typeFromJsonFile(raw);
                                String jsonString = new String(raw, StandardCharsets.UTF_8);
                                List<Class<?>> matched = schemata.stream().filter(
                                    cls -> {
                                        Schema schema;
                                        try {
                                            schema = extractSchema(cls);
                                        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                                            throw new AssertionError(
                                                "Extracting schema from AVRO class failed, " +
                                                    "this is likely an issue with the AVRO dependency itself.",
                                                e
                                            );
                                        }
                                        if (type != null && !type.equals(schema.getProp("_type")) && !Tracking.class.equals(cls)) {
                                            return false;
                                        }
                                        try {
                                            try {
                                                assertFullCycle(schema, cls, new ByteArrayInputStream(raw));
                                            } catch (Throwable t) {
                                                boolean isTrackingSchema = Tracking.class.equals(cls);
                                                // If this schema should have matched we try to get a more descriptive error message,
                                                // otherwise we just pass the given Throwable up the stack.
                                                if (type != null && type.equals(schema.getProp("_type")) || (isTrackingType(raw) && isTrackingSchema)) {
                                                    failWithProblemMessage(t, raw, schema, type, isTrackingSchema);
                                                } else {
                                                    throw t;
                                                }
                                            }
                                            return true;
                                        } catch (Exception ignored) {
                                            return false;
                                        }
                                    }
                                ).collect(Collectors.toList());
                                if (matched.isEmpty()) {
                                    throw new AssertionError("Failed to find schema for:\n" + jsonString);
                                } else if (isTrackingType(raw) && !"track".equals(type)) {
                                    if (matched.size() == 1) {
                                        if (!matched.contains(Tracking.class)) {
                                            throw new AssertionError(
                                                "Failed to find correctly identify schema for:\n" + jsonString +
                                                    "\nGeneric Tracking schema did not match but type [" + type + "] was found to be of 'track' type."
                                            );
                                        }
                                    } else if (matched.size() != 2) {
                                        throw new AssertionError(
                                            "Found more than 2 schemata for:\n" + jsonString +
                                                "\nbut only 2 should have been found for identified type [" + type + "] (Tracking and type specific schema)."
                                        );
                                    }
                                }
                            }
                        } catch (AssertionError | IOException e) {
                            throw new AssertionError("Integration test failed to find valid schema for [" + file + ']', e);
                        }
                    }
                });
            }
        }
    }

    /**
     * Identify what lead to the given {@link Throwable} and throw a more descriptive error.
     * @param t Throwable that was caught
     * @param raw Raw JSON bytes
     * @param schema Schema that should have matched
     * @param type Type deduced from the JSON bytes
     * @param isTrackingSchema Whether or not this is a 'track' type schema
     * @throws IOException On Failure to deserialize JSON bytes (should be impossible)
     */
    private static void failWithProblemMessage(Throwable t, byte[] raw, Schema schema, String type,
        boolean isTrackingSchema) throws IOException {
        final List<String> problems = new ArrayList<>();
        if (t instanceof JsonMappingException) {
            problems.add(t.getMessage());
        }
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = TestUtil.OBJECT_MAPPER.readValue(raw, Map.class);
            JsonToAvroUtils.normalizedJSON(schema, map);
        } catch (SchemaIncompatibilityException e) {
            problems.add(e.getMessage());
        }
        throw new AssertionError(
            "JSON: \n"
                + new String(raw, StandardCharsets.UTF_8)
                + "\nShould have matched ["
                + (isTrackingSchema ? "Tracking (generic type)" : type)
                + "] but the following problems were found:\n"
                + String.join("\n", problems)
        );
    }

    private static Iterable<byte[]> maybeFlattenBatch(byte[] raw) throws IOException {
        Map<String, Object> map = bytesToMap(raw);
        if (map.containsKey("batch")) {
            Map<String, Object> commonAttributes = new HashMap<>(map);
            commonAttributes.remove("batch");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> events = (List<Map<String, Object>>) map.get("batch");
            return events.stream().map(event -> {
                Map<String, Object> enriched = new HashMap<>(event);
                enriched.putAll(commonAttributes);
                try {
                    return TestUtil.OBJECT_MAPPER.writerFor(Map.class).writeValueAsBytes(enriched);
                } catch (JsonProcessingException e) {
                    throw new IllegalArgumentException("Could not serialize enriched event", e);
                }
            }).collect(Collectors.toList());
        }
        return Collections.singletonList(raw);
    }

    private static boolean isTrackingType(byte[] raw) throws IOException {
        Map<String, Object> asMap = bytesToMap(raw);
        String type = (String) asMap.get("type");
        return "track".equals(type);
    }

    private static String typeFromJsonFile(byte[] raw) throws IOException {
        Map<String, Object> asMap = bytesToMap(raw);
        String type = (String) asMap.get("type");
        if ("track".equals(type) && asMap.containsKey("event")) {
            type = (String) asMap.get("event");
        }
        return type;
    }

    private static Map<String, Object> bytesToMap(byte[] raw) throws IOException {
        Map<String, Object> map = TestUtil.OBJECT_MAPPER.readerFor(Map.class).readValue(raw);
        if (map == null) {
            throw new IllegalArgumentException("Could not read JSON map.");
        }
        return map;
    }

    private static Schema extractSchema(Class<? extends SpecificRecordBase> cls)
        throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        return (Schema) cls.getMethod("getClassSchema", null).invoke(null);
    }
}
