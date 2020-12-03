package io.avro.decoder;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.avro.AvroTypeException;
import org.apache.avro.JsonProperties;
import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.ParsingDecoder;
import org.apache.avro.io.parsing.JsonGrammarGenerator;
import org.apache.avro.io.parsing.Parser;
import org.apache.avro.io.parsing.Symbol;
import org.apache.avro.util.Utf8;
import org.apache.avro.util.internal.JacksonUtils;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * A {@link Decoder} for Avro's JSON data encoding.
 * <p>
 * Construct using {@link DecoderFactory}.
 * </p>
 * JsonOptionalDecoder is not thread-safe.
 * <p>
 * Based on {@link org.apache.avro.io.JsonDecoder JsonDecoder} and
 * <a href="https://github.com/zolyfarkas/avro">ExtendedJsonDecoder</a>. Infers
 * default arguments, if they are not present.
 * </p>
 **/
public class JsonOptionalDecoder extends ParsingDecoder implements Parser.ActionHandler {

    private static final JsonFactory JSON_FACTORY = new JsonFactory();

    private final Stack<ReorderBuffer> reorderBuffers = new Stack<>();

    private JsonParser in;
    private ReorderBuffer currentReorderBuffer;

    private final Schema schema;

    private static class ReorderBuffer {

        public Map<String, List<JsonElement>> savedFields = new HashMap<>();
        public JsonParser origParser = null;
    }

    public JsonOptionalDecoder(Schema schema, InputStream in) throws IOException {
        super(getSymbol(schema));
        configure(in);
        this.schema = schema;
    }

    public JsonOptionalDecoder(Schema schema, String in) throws IOException {
        super(getSymbol(schema));
        configure(in);
        this.schema = schema;
    }

    private static Symbol getSymbol(Schema schema) {
        if (null == schema)
            throw new NullPointerException("Schema cannot be null!");

        return new JsonGrammarGenerator().generate(schema);
    }

    /**
     * Reconfigures this JsonDecoder to use the InputStream provided. If the
     * InputStream provided is null, a NullPointerException is thrown. Otherwise,
     * this JsonDecoder will reset its state and then reconfigure its input.
     *
     * @param in The InputStream to read from. Cannot be null.
     * @return this JsonDecoder
     * @throws IOException in case of factory parser error
     */
    public JsonOptionalDecoder configure(InputStream in) throws IOException {
        if (null == in)
            throw new NullPointerException("InputStream to read from cannot be null!");

        parser.reset();
        this.in = JSON_FACTORY.createParser(in);
        this.in.nextToken();
        return this;
    }

    /**
     * Reconfigures this JsonDecoder to use the String provided for input. If the
     * String provided is null, a NullPointerException is thrown. Otherwise, this
     * JsonDecoder will reset its state and then reconfigure its input.
     *
     * @param in The String to read from. Cannot be null.
     * @return this JsonDecoder
     * @throws IOException from json factory
     */
    public JsonOptionalDecoder configure(String in) throws IOException {
        if (null == in)
            throw new NullPointerException("String to read from cannot be null!");

        parser.reset();
        this.in = new JsonFactory().createParser(in);
        this.in.nextToken();
        return this;
    }

    private void advance(Symbol symbol) throws IOException {
        this.parser.processTrailingImplicitActions();
        if (in.getCurrentToken() == null && this.parser.depth() == 1)
            throw new EOFException();

        parser.advance(symbol);
    }

    @Override
    public void readNull() throws IOException {
        advance(Symbol.NULL);
        if (in.getCurrentToken() == JsonToken.VALUE_NULL) {
            in.nextToken();
        } else {
            throw getErrorTypeMismatch("null");
        }
    }

    @Override
    public boolean readBoolean() throws IOException {
        advance(Symbol.BOOLEAN);
        JsonToken t = in.getCurrentToken();
        if (t == JsonToken.VALUE_TRUE || t == JsonToken.VALUE_FALSE) {
            in.nextToken();
            return t == JsonToken.VALUE_TRUE;
        } else {
            throw getErrorTypeMismatch("boolean");
        }
    }

    @Override
    public int readInt() throws IOException {
        advance(Symbol.INT);
        if (in.getCurrentToken().isNumeric()) {
            int result = in.getIntValue();
            in.nextToken();
            return result;
        } else {
            throw getErrorTypeMismatch("int");
        }
    }

    @Override
    public long readLong() throws IOException {
        advance(Symbol.LONG);
        if (in.getCurrentToken().isNumeric()) {
            long result = in.getLongValue();
            in.nextToken();
            return result;
        } else {
            throw getErrorTypeMismatch("long");
        }
    }

    @Override
    public float readFloat() throws IOException {
        advance(Symbol.FLOAT);
        if (in.getCurrentToken().isNumeric()) {
            float result = in.getFloatValue();
            in.nextToken();
            return result;
        } else {
            throw getErrorTypeMismatch("float");
        }
    }

    @Override
    public double readDouble() throws IOException {
        advance(Symbol.DOUBLE);
        if (in.getCurrentToken().isNumeric()) {
            double result = in.getDoubleValue();
            in.nextToken();
            return result;
        } else {
            throw getErrorTypeMismatch("double");
        }
    }

    @Override
    public Utf8 readString(Utf8 old) throws IOException {
        return new Utf8(readString());
    }

    @Override
    public String readString() throws IOException {
        parseSymbolInAdvance();
        String result = in.getText();
        in.nextToken();
        return result;
    }

    @Override
    public void skipString() throws IOException {
        parseSymbolInAdvance();
        in.nextToken();
    }

    private void parseSymbolInAdvance() throws IOException {
        advance(Symbol.STRING);
        if (parser.topSymbol() == Symbol.MAP_KEY_MARKER) {
            parser.advance(Symbol.MAP_KEY_MARKER);
            if (in.getCurrentToken() != JsonToken.FIELD_NAME) {
                throw getErrorTypeMismatch("map-key");
            }
        } else if (in.getCurrentToken() != JsonToken.VALUE_STRING) {
            throw getErrorTypeMismatch("string");
        }
    }

    @Override
    public ByteBuffer readBytes(ByteBuffer old) throws IOException {
        advance(Symbol.BYTES);
        if (in.getCurrentToken() == JsonToken.VALUE_STRING) {
            byte[] result = readByteArray();
            in.nextToken();
            return ByteBuffer.wrap(result);
        } else {
            throw getErrorTypeMismatch("bytes");
        }
    }

    private byte[] readByteArray() throws IOException {
        return in.getText().getBytes(StandardCharsets.ISO_8859_1);
    }

    @Override
    public void skipBytes() throws IOException {
        advance(Symbol.BYTES);
        if (in.getCurrentToken() == JsonToken.VALUE_STRING) {
            in.nextToken();
        } else {
            throw getErrorTypeMismatch("bytes");
        }
    }

    private void checkFixed(int size) throws IOException {
        advance(Symbol.FIXED);
        Symbol.IntCheckAction top = (Symbol.IntCheckAction) parser.popSymbol();
        if (size != top.size) {
            throw new AvroTypeException("Incorrect length for fixed binary: expected " +
                    top.size + " but received " + size + " bytes.");
        }
    }

    @Override
    public void readFixed(byte[] bytes, int start, int len) throws IOException {
        checkFixed(len);
        if (in.getCurrentToken() == JsonToken.VALUE_STRING) {
            byte[] result = readByteArray();
            in.nextToken();
            if (result.length != len)
                throw new AvroTypeException("Expected fixed length " + len + ", but got" + result.length);

            System.arraycopy(result, 0, bytes, start, len);
        } else {
            throw getErrorTypeMismatch("fixed");
        }
    }

    @Override
    public void skipFixed(int length) throws IOException {
        checkFixed(length);
        doSkipFixed(length);
    }

    private void doSkipFixed(int length) throws IOException {
        if (in.getCurrentToken() == JsonToken.VALUE_STRING) {
            byte[] result = readByteArray();
            in.nextToken();
            if (result.length != length)
                throw new AvroTypeException("Expected fixed length " + length + ", but got" + result.length);
        } else {
            throw getErrorTypeMismatch("fixed");
        }
    }

    @Override
    protected void skipFixed() throws IOException {
        advance(Symbol.FIXED);
        Symbol.IntCheckAction top = (Symbol.IntCheckAction) parser.popSymbol();
        doSkipFixed(top.size);
    }

    @Override
    public int readEnum() throws IOException {
        advance(Symbol.ENUM);
        Symbol.EnumLabelsAction top = (Symbol.EnumLabelsAction) parser.popSymbol();
        if (in.getCurrentToken() == JsonToken.VALUE_STRING) {
            in.getText();
            int n = top.findLabel(in.getText());
            if (n >= 0) {
                in.nextToken();
                return n;
            }
            throw new AvroTypeException("Unknown symbol in enum " + in.getText());
        } else {
            throw getErrorTypeMismatch("fixed");
        }
    }

    @Override
    public long readArrayStart() throws IOException {
        advance(Symbol.ARRAY_START);
        if (in.getCurrentToken() == JsonToken.START_ARRAY) {
            in.nextToken();
            return doArrayNext();
        } else {
            throw getErrorTypeMismatch("array-start");
        }
    }

    @Override
    public long arrayNext() throws IOException {
        advance(Symbol.ITEM_END);
        return doArrayNext();
    }

    private long doArrayNext() throws IOException {
        if (in.getCurrentToken() == JsonToken.END_ARRAY) {
            parser.advance(Symbol.ARRAY_END);
            in.nextToken();
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public long skipArray() throws IOException {
        advance(Symbol.ARRAY_START);
        if (in.getCurrentToken() == JsonToken.START_ARRAY) {
            in.skipChildren();
            in.nextToken();
            advance(Symbol.ARRAY_END);
        } else {
            throw getErrorTypeMismatch("array-start");
        }
        return 0;
    }

    @Override
    public long readMapStart() throws IOException {
        advance(Symbol.MAP_START);
        if (in.getCurrentToken() == JsonToken.START_OBJECT) {
            in.nextToken();
            return doMapNext();
        } else {
            throw getErrorTypeMismatch("map-start");
        }
    }

    @Override
    public long mapNext() throws IOException {
        advance(Symbol.ITEM_END);
        return doMapNext();
    }

    private long doMapNext() throws IOException {
        if (in.getCurrentToken() == JsonToken.END_OBJECT) {
            in.nextToken();
            advance(Symbol.MAP_END);
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public long skipMap() throws IOException {
        advance(Symbol.MAP_START);
        if (in.getCurrentToken() == JsonToken.START_OBJECT) {
            in.skipChildren();
            in.nextToken();
            advance(Symbol.MAP_END);
        } else {
            throw getErrorTypeMismatch("map-start");
        }
        return 0;
    }

    @Override
    public int readIndex() throws IOException {
        advance(Symbol.UNION);
        final Symbol.Alternative a = (Symbol.Alternative) parser.popSymbol();

        String label;
        final JsonToken currentToken = in.getCurrentToken();

        if (currentToken == JsonToken.VALUE_NULL) {
            label = "null";
        } else if (a.size() == 2 &&
                (a.getSymbol(0) == Symbol.NULL || a.getSymbol(1) == Symbol.NULL)) {
            label = (a.getSymbol(0) == Symbol.NULL)
                    ? a.getLabel(1)
                    : a.getLabel(0);
        } else if (currentToken == JsonToken.START_OBJECT
                && in.nextToken() == JsonToken.FIELD_NAME) {
            label = in.getText();
            in.nextToken();
            parser.pushSymbol(Symbol.UNION_END);
        } else {
            throw getErrorTypeMismatch("start-union");
        }

        int n = a.findLabel(label);
        if (n < 0) {
            throw new AvroTypeException("Unknown union branch " + label);
        }

        parser.pushSymbol(a.getSymbol(n));
        return n;
    }

    @Override
    public Symbol doAction(Symbol input, Symbol top) throws IOException {
        if (top instanceof Symbol.FieldAdjustAction) {
            Symbol.FieldAdjustAction fa = (Symbol.FieldAdjustAction) top;
            String name = fa.fname;
            if (currentReorderBuffer != null) {
                List<JsonElement> node = currentReorderBuffer.savedFields.get(name);
                if (node != null) {
                    currentReorderBuffer.savedFields.remove(name);
                    currentReorderBuffer.origParser = in;
                    in = makeParser(node);
                    return null;
                }
            }

            if (in.getCurrentToken() == JsonToken.FIELD_NAME) {
                do {
                    String fn = in.getText();
                    in.nextToken();
                    if (name.equals(fn)) {
                        return null;
                    } else {
                        if (currentReorderBuffer == null)
                            currentReorderBuffer = new ReorderBuffer();

                        currentReorderBuffer.savedFields.put(fn, getValueAsTree(in));
                    }
                } while (in.getCurrentToken() == JsonToken.FIELD_NAME);
            }

            injectDefaultValueIfAvailable(in, fa.fname);
        } else if (top == Symbol.FIELD_END) {
            if (currentReorderBuffer != null && currentReorderBuffer.origParser != null) {
                in = currentReorderBuffer.origParser;
                currentReorderBuffer.origParser = null;
            }
        } else if (top == Symbol.RECORD_START) {
            if (in.getCurrentToken() == JsonToken.START_OBJECT) {
                in.nextToken();
                reorderBuffers.push(currentReorderBuffer);
                currentReorderBuffer = null;
            } else {
                throw getErrorTypeMismatch("record-start");
            }
        } else if (top == Symbol.RECORD_END || top == Symbol.UNION_END) {
            if (in.getCurrentToken() == JsonToken.END_OBJECT) {
                in.nextToken();
                if (top == Symbol.RECORD_END) {
                    if (currentReorderBuffer != null && !currentReorderBuffer.savedFields.isEmpty()) {
                        throw getErrorTypeMismatch("Unknown fields: " + currentReorderBuffer.savedFields.keySet());
                    }

                    currentReorderBuffer = reorderBuffers.pop();
                }
            } else {
                throw getErrorTypeMismatch(top == Symbol.RECORD_END ? "record-end" : "union-end");
            }
        } else {
            throw new AvroTypeException("Unknown action symbol " + top);
        }
        return null;
    }

    private static List<JsonElement> getValueAsTree(JsonParser in) throws IOException {
        int level = 0;
        List<JsonElement> result = new ArrayList<>();
        do {
            JsonToken t = in.getCurrentToken();
            switch (t) {
                case START_OBJECT:
                case START_ARRAY:
                    level++;
                    result.add(new JsonElement(t));
                    break;
                case END_OBJECT:
                case END_ARRAY:
                    level--;
                    result.add(new JsonElement(t));
                    break;
                case FIELD_NAME:
                case VALUE_STRING:
                case VALUE_NUMBER_INT:
                case VALUE_NUMBER_FLOAT:
                case VALUE_TRUE:
                case VALUE_FALSE:
                case VALUE_NULL:
                    result.add(new JsonElement(t, in.getText()));
                    break;
            }
            in.nextToken();
        } while (level != 0);
        result.add(new JsonElement(null));
        return result;
    }

    private JsonParser makeParser(final List<JsonElement> elements) {
        return new JsonOptionalParser(elements);
    }

    private AvroTypeException getErrorTypeMismatch(String type) {
        return new AvroTypeException("Expected " + type + ". Got " + in.getCurrentToken());
    }

    private static final JsonElement NULL_JSON_ELEMENT = new JsonElement(null);

    private void injectDefaultValueIfAvailable(final JsonParser in, String fieldName) throws IOException {
        final Field field = findField(schema, fieldName);
        if (field == null)
            throw new AvroTypeException("Expected field name not found: " + fieldName);

        final Object defJsonValue = field.defaultVal() == null ? JsonProperties.NULL_VALUE : field.defaultVal();
        final JsonNode defVal = JacksonUtils.toJsonNode(defJsonValue);
        if (defVal == null)
            throw new AvroTypeException("Expected field name not found: " + fieldName);

        final List<JsonElement> result = new ArrayList<>(2);
        final JsonParser traverse = defVal.traverse();
        JsonToken nextToken;
        while ((nextToken = traverse.nextToken()) != null) {
            final JsonElement element = nextToken.isScalarValue()
                    ? new JsonElement(nextToken, traverse.getText())
                    : new JsonElement(nextToken);

            result.add(element);
        }

        result.add(NULL_JSON_ELEMENT);
        if (currentReorderBuffer == null)
            currentReorderBuffer = new ReorderBuffer();

        currentReorderBuffer.origParser = in;
        this.in = makeParser(result);
    }

    private static Field findField(Schema schema, String name) {
        if (schema.getField(name) != null)
            return schema.getField(name);

        return schema.getFields().stream()
                .map(Field::schema)
                .filter(s -> s.getType() != null)
                .map(s -> {
                    if (Schema.Type.UNION.equals(s.getType())) {
                        return s.getTypes().stream()
                                .filter(sub -> sub.getType().equals(Schema.Type.RECORD))
                                .map(sub -> findField(sub, name))
                                .filter(Objects::nonNull)
                                .findAny()
                                .orElse(null);
                    }

                    switch (s.getType()) {
                        case RECORD:
                            return findField(s, name);
                        case ARRAY:
                            return findField(s.getElementType(), name);
                        case MAP:
                            return findField(s.getValueType(), name);
                        default:
                            return null;
                    }
                })
                .filter(Objects::nonNull)
                .findAny()
                .orElse(null);
    }
}
