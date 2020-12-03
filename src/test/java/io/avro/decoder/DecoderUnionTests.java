package io.avro.decoder;

import org.apache.avro.generic.GenericRecord;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

/**
 * @author Anton Kurako (GoodforGod)
 * @since 22.10.2020
 */
class DecoderUnionTests extends DecoderRunner {

    private static Stream<Arguments> testData() {
        return Stream.of(
                Arguments.of(null, "{}"),
                Arguments.of(null, "{}"),
                Arguments.of(null, "{}"),
                Arguments.of(null, "{\"a\":null}"),
                Arguments.of(42L, "{\"a\":42}"));
    }

    @ParameterizedTest(name = "{index} {1} field is optional")
    @MethodSource("testData")
    void testUnionWithDefault(Object expected, String data) {
        try {
            String w = getAvroSchema("avro/nullable_union_default.avsc");
            GenericRecord record = readRecord(w, data);
            assertEquals(expected, record.get("a"));
        } catch (Exception e) {
            fail(e.getMessage() + " for " + data);
        }
    }
}
