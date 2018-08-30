package com.oz.segmentio.avro;

import com.oz.segmentio.json.JsonToAvroUtils;
import java.util.ArrayList;
import java.util.Collection;
import org.apache.avro.specific.SpecificRecordBase;
import org.junit.Test;

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
        Collection<String> testCases = new ArrayList<>();
        schemata.stream().filter(cls -> {
            try {

                return true;
            } catch (Exception ignored) {
                return false;
            }
        });
    }
}
