package io.avro.decoder;

import java.io.IOException;

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
    void testReorderFields() throws Exception {
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
    void testNullByDefault() throws IOException {
        String w = getAvroSchema("avro/null_by_default.avsc");
        GenericRecord record = readRecord(w, "{\"username\":\"bob\"}");

        assertEquals("bob", record.get("username").toString());
    }

    @Test
    void testNullByDefaultAnyPosition() throws IOException {
        String w = getAvroSchema("avro/null_by_default.avsc");
        GenericRecord record = readRecord(w, "{\"username\":\"bob\"}");

        assertEquals("bob", record.get("username").toString());
    }

    @Test
    void testDefaultValuesAreInferred() throws IOException {
        String w = getAvroSchema("avro/default_inferred.avsc");
        GenericRecord record = readRecord(w, "{}");

        assertEquals(7L, record.get("a"));
    }

    @Test
    void testNestedNullsAreInferred() throws IOException {
        String w = getAvroSchema("avro/nullable_nested_inferred.avsc");
        String data = "{\"S\": {\"b\":1}}";
        GenericRecord record = ((GenericRecord) readRecord(w, data).get("S"));
        assertNull(record.get("a"));
    }

    @Test
    void testNestedNullsAreInferredWhenNotPresent() throws IOException {
        String w = getAvroSchema("avro/nullable_record.avsc");
        String data = "{\"required\":\"bob\"}";
        GenericRecord record = readRecord(w, data);
        assertNotNull(record.get("required"));
    }

    @Test
    void testNestedNullsAreInferredWhenPresentRequiredField() throws IOException {
        String w = getAvroSchema("avro/nullable_record.avsc");
        String data = "{\"required\":\"bob\"}";
        GenericRecord record = readRecord(w, data);
        assertNotNull(record.get("required"));
    }

    @Test
    void testNestedRecordNullableOnlyNoInner() throws IOException {
        String w = getAvroSchema("avro/nullable_record_only.avsc");
        String data = "{\"req1\":\"bob\"}";
        GenericRecord record = readRecord(w, data);
        assertNotNull(record.get("req1"));
    }

    @Test
    void testNestedRecordNullableOnlyAllFields() throws IOException {
        String w = getAvroSchema("avro/nullable_record_only.avsc");
        String data = "{\"req1\":\"bob\", \"inner\":{\"req\":\"1\",\"code\":1}}";
        GenericRecord record = readRecord(w, data);
        assertNotNull(record.get("req1"));
    }

    @Test
    void testNestedNullsAreInferredIfPresent() throws IOException {
        String w = getAvroSchema("avro/nullable_record.avsc");
        String data = "{\"required\":\"bob\",\"inner\":{\"req\":\"my\"}}";
        GenericRecord record = readRecord(w, data);
        assertNotNull(record.get("required"));
    }

    @Test
    void testArraysCanBeNull() throws IOException {
        String w = getAvroSchema("avro/nullable_array.avsc");
        String data = "{}";
        GenericRecord record = readRecord(w, data);
        assertNull(record.get("A"));
    }

    @Test
    void testArraysCanBeNullWithDefault() throws IOException {
        String w = getAvroSchema("avro/nullable_array_default.avsc");
        String data = "{}";
        GenericRecord record = readRecord(w, data);
        assertNull(record.get("A"));
    }

    @Test
    void testRecordCanBeNull() throws IOException {
        String w = getAvroSchema("avro/nullable_record_default.avsc");
        String data = "{}";
        GenericRecord record = readRecord(w, data);
        assertNull(record.get("S"));
    }

    @Test
    void testComplex() throws IOException {
        String w = getAvroSchema("avro/nullable_complex.avsc");
        String data = "{\"data\":[{\"r1\":[{\"sr2\":\"val1\"}],\"r2\":[{\"notfound1\":[\"val1\",\"val2\"]}]}]}";
        GenericRecord record = readRecord(w, data);
        assertNull(record.get("S"));
    }
}
