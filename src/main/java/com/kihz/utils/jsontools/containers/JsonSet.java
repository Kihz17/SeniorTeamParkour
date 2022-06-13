package com.kihz.utils.jsontools.containers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.kihz.utils.JsonUtils;
import com.kihz.utils.jsontools.JsonSerializer;
import com.kihz.utils.jsontools.Jsonable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Consumer;

@Getter @NoArgsConstructor
public class JsonSet<T> implements Jsonable, Iterable<T> {
    private final transient Set<T> data = new HashSet<>();
    @Setter
    private Class<T> typeClass;

    public JsonSet(Iterable<T> values) {
        values.forEach(getData()::add);
    }

    public JsonSet(Class<T> classType) {
        this.typeClass = classType;
    }

    @Override
    public void load(JsonElement array) {
        for (JsonElement element : array.getAsJsonArray()) {
            if (JsonUtils.isJsonNull(element))
                continue;

            T loadedValue = JsonSerializer.deserialize(getTypeClass(), element);
            if (loadedValue != null)
                getData().add(loadedValue);
        }
    }

    @Override
    public JsonElement save() {
        JsonArray array = new JsonArray();
        for (T value : getData()) {
            JsonElement savedValue = JsonSerializer.addClassNoParent(value, JsonSerializer.save(value));
            if (!JsonUtils.isJsonNull(savedValue)) // Don't check if default value, because lists need to save default values.
                array.add(savedValue);
        }

        return array;
    }

    @Override
    public Iterator<T> iterator() {
        return getData().iterator();
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        getData().forEach(action);
    }

    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder("[");
        for (T val : getData())
            ret.append(ret.length() > 1 ? ", " : "").append(val);
        return ret.append("]").toString();
    }

    /**
     * Add a value to the set
     * @param data the data to add
     */
    public boolean add(T data) {
        return getData().add(data);
    }

    /**
     * Remove an object from the set
     * @param data The object to remove
     * @return removed
     */
    public boolean remove(T data) {
        return getData().remove(data);
    }

    /**
     * Does the set contain a given object?
     * @param data The data to check for
     * @return contains
     */
    public boolean contains(T data) {
        return getData().contains(data);
    }
}