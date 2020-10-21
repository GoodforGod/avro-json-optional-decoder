package io.avro.decoder;

import org.codehaus.jackson.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

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
}
