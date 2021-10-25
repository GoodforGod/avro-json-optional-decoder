package io.avro.decoder;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.Decoder;
import org.junit.jupiter.api.Assertions;

/**
 * @author Anton Kurako (GoodforGod)
 * @since 22.10.2020
 */
abstract class DecoderRunner extends Assertions {

    protected GenericRecord readRecord(String schemaString, String jsonData) throws IOException {
        Schema schema = parseSchema(schemaString);
        Decoder decoder = new JsonOptionalDecoder(schema, jsonData);
        DatumReader<GenericRecord> datumReader = new GenericDatumReader<>(schema);
        return datumReader.read(null, decoder);
    }

    protected static Schema parseSchema(String schema) {
        return new Schema.Parser().parse(schema);
    }

    protected static String getAvroSchema(String path) {
        try {
            final URI uri = DecoderTests.class.getClassLoader().getResource(path).toURI();
            return new String(Files.readAllBytes(Paths.get(uri)));
        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }
}
