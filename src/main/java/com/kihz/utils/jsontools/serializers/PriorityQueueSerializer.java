package com.kihz.utils.jsontools.serializers;

import com.google.gson.JsonElement;
import com.kihz.utils.ReflectionUtil;
import com.kihz.utils.jsontools.containers.JsonPriorityQueue;

import java.lang.reflect.Field;

public class PriorityQueueSerializer extends Serializer<JsonPriorityQueue> {

    public PriorityQueueSerializer() {
        super(JsonPriorityQueue.class);
    }

    @Override
    public JsonPriorityQueue deserialize(JsonElement jsonElement, Class<JsonPriorityQueue> loadClass, Field field) {
        JsonPriorityQueue jsonPriorityQueue = new JsonPriorityQueue(ReflectionUtil.getGenericType(field));
        jsonPriorityQueue.load(jsonElement);
        return jsonPriorityQueue;
    }

    @Override
    public JsonElement serialize(JsonPriorityQueue value) {
        return value.save();
    }
}