package io.avro.decoder;

import com.fasterxml.jackson.core.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Objects;

/**
 * Jackson Json Parser
 *
 * @author Anton Kurako (GoodforGod)
 * @since 22.10.2020
 */
public class JsonOptionalParser extends JsonParser {

    private int pos = 0;
    private final List<JsonElement> elements;

    public JsonOptionalParser(List<JsonElement> elements) {
        this.elements = elements;
    }

    @Override
    public ObjectCodec getCodec() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setCodec(ObjectCodec c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() {
        throw new UnsupportedOperationException();
    }

    @Override
    public JsonToken nextToken() {
        pos++;
        return elements.get(pos).token;
    }

    @Override
    public JsonParser skipChildren() {
        JsonToken tkn = elements.get(pos).token;
        int level = (tkn == JsonToken.START_ARRAY || tkn == JsonToken.END_ARRAY) ? 1 : 0;
        while (level > 0) {
            switch (elements.get(++pos).token) {
                case START_ARRAY:
                case START_OBJECT:
                    level++;
                    break;
                case END_ARRAY:
                case END_OBJECT:
                    level--;
                    break;
            }
        }
        return this;
    }

    @Override
    public boolean isClosed() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getCurrentName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public JsonStreamContext getParsingContext() {
        throw new UnsupportedOperationException();
    }

    @Override
    public JsonLocation getTokenLocation() {
        throw new UnsupportedOperationException();
    }

    @Override
    public JsonLocation getCurrentLocation() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getText() {
        return elements.get(pos).value;
    }

    @Override
    public char[] getTextCharacters() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getTextLength() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getTextOffset() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Number getNumberValue() {
        throw new UnsupportedOperationException();
    }

    @Override
    public NumberType getNumberType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getIntValue() {
        return Integer.parseInt(getText());
    }

    @Override
    public long getLongValue() {
        return Long.parseLong(getText());
    }

    @Override
    public BigInteger getBigIntegerValue() {
        return new BigInteger(getText());
    }

    @Override
    public float getFloatValue() {
        return Float.parseFloat(getText());
    }

    @Override
    public double getDoubleValue() {
        return Double.parseDouble(getText());
    }

    @Override
    public BigDecimal getDecimalValue() {
        return new BigDecimal(getText());
    }

    @Override
    public byte[] getBinaryValue(Base64Variant b64variant) {
        throw new UnsupportedOperationException();
    }

    @Override
    public JsonToken getCurrentToken() {
        return elements.get(pos).token;
    }

    @Override
    public Version version() {
        return null;
    }

    @Override
    public JsonToken nextValue() throws IOException {
        return null;
    }

    @Override
    public int getCurrentTokenId() {
        final JsonToken t = getCurrentToken();
        return (t == null) ? JsonTokenId.ID_NO_TOKEN : t.id();
    }

    @Override
    public boolean hasCurrentToken() {
        return elements.size() - 1 > pos;
    }

    @Override
    public boolean hasTokenId(int id) {
        final JsonToken t = getCurrentToken();
        if (t == null)
            return (JsonTokenId.ID_NO_TOKEN == id);

        return t.id() == id;
    }

    @Override
    public boolean hasToken(JsonToken t) {
        return Objects.equals(getCurrentToken(), t);
    }

    @Override
    public void clearCurrentToken() {
        if (getCurrentToken() != null) {
            JsonElement element = elements.get(pos);
            elements.set(pos, new JsonElement(element.token, element.value));
        }
    }

    @Override
    public JsonToken getLastClearedToken() {
        return elements.get(elements.size() - 1).token;
    }

    @Override
    public void overrideCurrentName(String name) {

    }

    @Override
    public boolean hasTextCharacters() {
        final JsonToken currentToken = getCurrentToken();
        if (currentToken == JsonToken.VALUE_STRING)
            return true;
        if (currentToken == JsonToken.FIELD_NAME)
            return !currentToken.isScalarValue();
        return false;
    }

    @Override
    public String getValueAsString(String defaultValue) {
        final JsonToken currentToken = getCurrentToken();
        if (currentToken == null || !currentToken.isScalarValue())
            return defaultValue;

        switch (currentToken) {
            case FIELD_NAME:
                return getCurrentName();
            case VALUE_NULL:
                return defaultValue;
            case VALUE_STRING:
            default:
                return getText();
        }
    }
}
