package io.avro.decoder;

import java.io.IOException;

import org.apache.avro.AvroRuntimeException;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.junit.jupiter.api.Test;

class DecoderTests extends DecoderRunner {

    /**
     * Ensure that even if the order of fields in JSON is different from the order
     * in schema, it works.
     * 
     * @throws Exception if occurred
     */
    @Test
    public void testReorderFields() throws Exception {
        String w = getAvroSchema("avro/reorder.avsc");
        Schema ws = parseSchema(w);
        String data = "{\"a\":[1,2],\"l\":100}{\"l\": 200, \"a\":[1,2]}";
        JsonOptionalDecoder in = new JsonOptionalDecoder(ws, data);
        assertEquals(100, in.readLong());
        in.skipArray();
        assertEquals(200, in.readLong());
        in.skipArray();
    }

    @Test
    public void testDefaultValuesAreInferred() throws IOException {
        String w = getAvroSchema("avro/default_inferred.avsc");
        GenericRecord record = readRecord(w, "{}");

        assertEquals(7L, record.get("a"));
    }

    @Test
    public void testNestedNullsAreInferred() throws IOException {
        String w = getAvroSchema("avro/nullable_nested_inferred.avsc");
        String data = "{\"S\": {\"b\":1}}";
        GenericRecord record = ((GenericRecord) readRecord(w, data).get("S"));
        assertNull(record.get("a"));
    }

    @Test
    public void testArraysCanBeNull() throws IOException {
        String w = getAvroSchema("avro/nullable_array.avsc");
        String data = "{}";
        GenericRecord record = readRecord(w, data);
        assertNull(record.get("A"));
    }

    @Test
    public void testRecordCanBeNull() throws IOException {
        String w = getAvroSchema("avro/nullable_record.avsc");
        String data = "{}";
        GenericRecord record = readRecord(w, data);
        assertNull(record.get("S"));
    }

    @Test
    public void testComplex() throws IOException {
        String w = getAvroSchema("avro/nullable_complex.avsc");
        String data = "{\"data\":[{\"r1\":[],\"r2\":[{\"notfound1\":{\"array\":[\"val1\",\"val2\"]}}]}]}";
        GenericRecord record = readRecord(w, data);

        Throwable exception = assertThrows(AvroRuntimeException.class, () -> record.get("S"));
        assertEquals(exception.getMessage(), "Not a valid schema field: S");
    }
}
