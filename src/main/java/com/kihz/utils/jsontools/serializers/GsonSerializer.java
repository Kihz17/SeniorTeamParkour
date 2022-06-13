package com.kihz.utils.jsontools.serializers;

import com.google.gson.JsonElement;

import java.lang.reflect.Field;

public class GsonSerializer extends Serializer<JsonElement> {

    public GsonSerializer() {
        super(JsonElement.class);
    }

    @Override
    public JsonElement deserialize(JsonElement jsonElement, Class<JsonElement> loadClass, Field field) {
        return jsonElement;
    }

    @Override
    public JsonElement serialize(JsonElement value) {
        return value;
    }
}
