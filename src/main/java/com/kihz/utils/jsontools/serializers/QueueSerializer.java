package com.kihz.utils.jsontools.serializers;

import com.google.gson.JsonElement;
import com.kihz.utils.ReflectionUtil;
import com.kihz.utils.jsontools.containers.JsonQueue;

import java.lang.reflect.Field;

public class QueueSerializer extends Serializer<JsonQueue> {

    public QueueSerializer() {
        super(JsonQueue.class);
    }

    @Override
    public JsonQueue deserialize(JsonElement jsonElement, Class<JsonQueue> loadClass, Field field) {
        JsonQueue jsonQueue = new JsonQueue(ReflectionUtil.getGenericType(field));
        jsonQueue.load(jsonElement);
        return jsonQueue;
    }

    @Override
    public JsonElement serialize(JsonQueue value) {
        return value.save();
    }
}
