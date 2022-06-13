package com.kihz.utils.jsontools.serializers;

import com.google.gson.JsonElement;
import com.kihz.item.GUIItem;
import lombok.Getter;

import java.lang.reflect.Field;
import java.util.function.Consumer;

@Getter
public abstract class Serializer<T> {
    private final Class<? extends T> applyTo;

    public Serializer(Class<T> apply) {
        this.applyTo = apply;
    }

    /**
     * Deserialize json element into an object.
     * @param jsonElement The element to deserialize
     * @param loadClass The class to load the data into
     * @return value
     */
    public abstract T deserialize(JsonElement jsonElement, Class<T> loadClass, Field field);

    /**
     * Serialize a java object into a JsonElement
     * @param value The value to serialize
     * @return jsonElement
     */
    public abstract JsonElement serialize(T value);

    /**
     * Verify that the given class can be handled by the current handler.
     * @param clazz The class to test
     * @return canApply
     */
    public boolean canApplyTo(Class<?> clazz) {
        return getApplyTo() != null && getApplyTo().isAssignableFrom(clazz);
    }

    /**
     * Apply item editor data to an item
     * @param item The item being added
     * @param value The value to edit
     * @param setter A way to set the value
     */
    public void editItem(GUIItem item, Object value, Consumer<Object> setter, Class<?> classType) {
        editItem(item, value, setter);
    }

    /**
     * Apply item editor data to an item
     * @param item The item being added
     * @param value The value to edit
     * @param setter The setter
     */
    protected void editItem(GUIItem item, Object value, Consumer<Object> setter) {
        throw new UnsupportedOperationException("Must be implemented by " + getClass().getSimpleName());
    }

    /**
     * Set the value of a field to null
     * @param item The item we are clicking
     * @param value The value to set
     * @param setter The setter
     */
    protected void setNull(GUIItem item, Object value, Consumer<Object> setter) {
        if (value != null)
            item.rightClick(ce -> setter.accept(null)).addLoreAction("Right", "Remove Value");
    }
}