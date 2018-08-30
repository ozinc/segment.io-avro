package com.oz.segmentio.avro;

import com.google.common.io.Resources;
import com.oz.segmentio.json.JsonToAvroUtils;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import org.apache.avro.Schema;
import org.apache.avro.specific.SpecificRecordBase;
import org.assertj.core.api.Assertions;
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
        try (DirectoryStream<Path> directoryStream =
                 Files.newDirectoryStream(
                     Paths.get(Resources.getResource(IntegrationTests.class, "").toURI())
                 )
        ) {
            directoryStream.forEach(file -> {
                if (file.toString().endsWith(".json")) {
                    Assertions.assertThat(schemata.stream().filter(cls -> {
                        try {
                            assertFullCycle(
                                file.getFileName().toString().replaceAll(".json$", ""),
                                (Schema) cls.getMethod("getClassSchema", null).invoke(null),
                                cls
                            );
                            return true;
                        } catch (Exception ignored) {
                            return false;
                        }
                    }).findFirst()).isPresent();
                }
            });
        }
    }
}
