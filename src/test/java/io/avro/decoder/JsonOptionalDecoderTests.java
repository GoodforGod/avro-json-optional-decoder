package io.avro.decoder;

import java.io.IOException;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.Decoder;
import org.junit.jupiter.api.Test;

public class JsonOptionalDecoderTests extends DecoderRunner {

    @Test
    public void testInt() throws Exception {
        checkNumeric("int", 1);
    }

    @Test
    public void testLong() throws Exception {
        checkNumeric("long", 1L);
    }

    @Test
    public void testFloat() throws Exception {
        checkNumeric("float", 1.0F);
    }

    @Test
    public void testDouble() throws Exception {
        checkNumeric("double", 1.0);
    }

    private void checkNumeric(String type, Object value) throws Exception {
        String def = String.format(getAvroSchema("avro/template.avsc"), type);
        Schema schema = parseSchema(def);
        DatumReader<GenericRecord> reader = new GenericDatumReader<>(schema);

        String[] records = { "{\"n\":1}", "{\"n\":1.0}" };

        for (String record : records) {
            Decoder decoder = new JsonOptionalDecoder(schema, record);
            GenericRecord r = reader.read(null, decoder);
            assertEquals(value, r.get("n"));
        }
    }

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
    public void testNullsAreInferred() throws IOException {
        String w = getAvroSchema("avro/nullable_union_default.avsc");
        GenericRecord record = readRecord(w, "{}");

        assertNull(record.get("a"));
    }

    @Test
    public void testUnionNullImplicit() throws IOException {
        String w = getAvroSchema("avro/nullable_union_default.avsc");
        GenericRecord record = readRecord(w, "{}");
        assertNull(record.get("a"));
    }

    @Test
    public void testUnionNullExplicitUntagged() throws IOException {
        String w = getAvroSchema("avro/nullable_union_default.avsc");
        GenericRecord record = readRecord(w, "{\"a\":null}");
        assertNull(record.get("a"));
    }

    @Test
    public void testUnionNullExplicitTagged() throws IOException {
        String w = getAvroSchema("avro/nullable_union_default.avsc");
        GenericRecord record = readRecord(w, "{\"a\":{\"null\": null}}");
        assertNull(record.get("a"));
    }

    @Test
    public void testUnionLongExplicitTagged() throws IOException {
        String w = getAvroSchema("avro/nullable_union_default.avsc");
        GenericRecord record = readRecord(w, "{\"a\":{\"long\": 42}}");
        assertEquals(42L, record.get("a"));
    }

    @Test
    public void testUnionLongExplicitUntagged() throws IOException {
        String w = getAvroSchema("avro/nullable_union_default.avsc");
        GenericRecord record = readRecord(w, "{\"a\":42}");
        assertEquals(42L, record.get("a"));
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
        assertNull(record.get("S"));
    }
}
