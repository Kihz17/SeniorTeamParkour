package com.kihz.utils.jsontools.serializers;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.kihz.item.GUIItem;

import java.lang.reflect.Field;
import java.util.function.Consumer;

public class BooleanSerializer extends Serializer<Boolean> {

    public BooleanSerializer() {
        super(Boolean.class);
    }

    @Override
    public Boolean deserialize(JsonElement jsonElement, Class<Boolean> loadClass, Field field) {
        return jsonElement.getAsBoolean();
    }

    @Override
    public JsonElement serialize(Boolean value) {
        return new JsonPrimitive(value);
    }

    @Override
    public boolean canApplyTo(Class<?> clazz) {
        return super.canApplyTo(clazz) || clazz.equals(Boolean.TYPE);
    }

    @Override
    public void editItem(GUIItem item, Object value, Consumer<Object> setter) {
        item.leftClick(ce -> setter.accept(!((Boolean) value)), "Toggle");
    }
}
