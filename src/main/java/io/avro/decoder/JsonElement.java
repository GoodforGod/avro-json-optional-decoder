package io.avro.decoder;

import com.fasterxml.jackson.core.JsonToken;

/**
 * Json Avro Element
 *
 * @author Anton Kurako (GoodforGod)
 * @since 22.10.2020
 */
public class JsonElement {

    public final JsonToken token;
    public final String value;

    public JsonElement(JsonToken t, String value) {
        this.token = t;
        this.value = value;
    }

    public JsonElement(JsonToken t) {
        this(t, null);
    }

    public JsonToken getToken() {
        return token;
    }

    public String getValue() {
        return value;
    }
}
