package io.avro.decoder;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.Decoder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.stream.Stream;

/**
 * @author Anton Kurako (GoodforGod)
 * @since 22.10.2020
 */
class DecoderNumericTests extends DecoderRunner {

    private static Stream<Arguments> testData() {
        return Stream.of(
                Arguments.of("int", 1),
                Arguments.of("long", 1L),
                Arguments.of("float", 1.0F),
                Arguments.of("double", 1.0));
    }

    @ParameterizedTest(name = "{index} {1} field is optional")
    @MethodSource("testData")
    public void testUnionWithDefault(String type, Object value) throws IOException {
        final String def = String.format(getAvroSchema("avro/template.avsc"), type);
        final Schema schema = parseSchema(def);
        final DatumReader<GenericRecord> reader = new GenericDatumReader<>(schema);

        final String[] records = { "{\"n\":1}", "{\"n\":1.0}" };
        for (String record : records) {
            Decoder decoder = new JsonOptionalDecoder(schema, record);
            GenericRecord r = reader.read(null, decoder);
            assertEquals(value, r.get("n"));
        }
    }
}
