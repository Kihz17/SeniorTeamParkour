package com.kihz.utils.jsontools.containers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.kihz.utils.Utils;
import com.kihz.utils.jsontools.JsonSerializer;
import com.kihz.utils.jsontools.Jsonable;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

@Getter
public class JsonMap<V> implements Jsonable {
    private transient Map<String, V> map;
    @Setter
    private Class<V> typeClass;

    public JsonMap() {
        this(new HashMap<>());
    }

    public JsonMap(Class<V> clazz) {
        this();
        this.typeClass = clazz;
    }

    public JsonMap(Map<String, V> map) {
        this.map = map;
    }

    @Override
    public void load(JsonElement je) {
        je.getAsJsonObject().entrySet().forEach(e ->
                put(e.getKey(), JsonSerializer.deserialize(getTypeClass(), e.getValue())));
    }

    @Override
    public JsonElement save() {
        JsonObject obj = new JsonObject();
        for (Map.Entry<String, V> entry : getMap().entrySet())
            obj.add(entry.getKey(), JsonSerializer.addClassNoParent(entry.getValue(), JsonSerializer.save(entry.getValue())));
        return obj;
    }

    @Override
    public String toString() {
        return Utils.getSimpleName(this) + " (" + size() + " Entries)";
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof JsonMap<?> && ((JsonMap<?>) other).getMap().equals(getMap());
    }

    /**
     * Save a value by its enum key.
     * @param enumKey The enum key to store this by.
     * @param value   The value to store.
     */
    public <E extends Enum<E>> void put(E enumKey, V value) {
        put(enumKey.name(), value);
    }

    /**
     * Save a value by its Object.toString() key.
     * @param object The object to use as the key.
     * @param value  The value to set.
     */
    public void put(Object object, V value) {
        put(object.toString(), value);
    }

    /**
     * Put a given value in the map from a given key
     * @param key The key to index
     * @param value The value to put
     */
    public void put(String key, V value) {
        if(value != null) {
            getMap().put(key, value);
        } else {
            getMap().remove(key);
        }
    }

    /**
     * Returns the size of this map.
     * @return size
     */
    public int size() {
        return getMap().size();
    }

    /**
     * Get the element with the specified key.
     * @param key The key the value is indexed by.
     * @return value
     */
    public V get(String key) {
        return getMap().get(key);
    }

    /**
     * Get a value by its enum key.
     * @param key The enum key to get the value of.
     * @return value
     */
    public <E extends Enum<E>> V getValue(E key) {
        return get(key.name());
    }

    /**
     * Get a value by its key if present, otherwise return a default value.
     * @param key      The key we want to get data for.
     * @param fallback The value to return if the key is not present.
     * @return value
     */
    public V getOrDefault(String key, V fallback) {
        return containsKey(key) ? get(key) : fallback;
    }

    /**
     * Get a value by its key, if it is not present return a fallback value.
     * @param key The enum key value to get.
     * @param fallback The value to return if the key was not present.
     * @return value
     */
    public <E extends Enum<E>> V getOrDefault(E key, V fallback) {
        return getValue(key) != null ? getValue(key) : fallback;
    }

    /**
     * Get the first key whose value matches the passed value.
     * @param value The value to get the key for.
     * @return key
     */
    public String getKey(V value) {
        return Utils.getKey(map, value);
    }

    /**
     * Does this map contain the listed key?
     * @param key The key to test for.
     * @return contains
     */
    public boolean containsKey(String key) {
        return getMap().containsKey(key);
    }

    /**
     * Does this map contain a value by an enum key?
     * @param key The enum key to check.
     * @return hasValue
     */
    public <E extends Enum<E>> boolean containsKey(E key) {
        return containsKey(key.name());
    }

    /**
     * Get a list of keys in this set.
     * @return keys
     */
    public Set<String> keySet() {
        return getMap().keySet();
    }

    /**
     * Get a set of values from this map.
     * @return values
     */
    public Collection<V> values() {
        return getMap().values();
    }

    /**
     * Remove an element from this object.
     * @param key The key to remove.
     * @return valueRemoved
     */
    public V remove(String key) {
        return getMap().remove(key);
    }

    /**
     * Remove a value by its enum key.
     * @param key The key to remove.
     * @return removedValue
     */
    public <E extends Enum<E>> V remove(E key) {
        return remove(key.name());
    }

    /**
     * Add a value, if a value with the given key is not already present.
     * @param key      The key to index the value by.
     * @param supplier The value to set.
     */
    public V computeIfAbsent(String key, Supplier<V> supplier) {
        V value = get(key);
        if (value == null)
            put(key, value = supplier.get());

        return value;
    }

    /**
     * Add a value, if a value with the given key is not already present.
     * @param enumKey  The enum key to index the value by.
     * @param supplier The value to set.
     */
    public <E extends Enum<E>> V computeIfAbsent(E enumKey, Supplier<V> supplier) {
        return computeIfAbsent(enumKey.name(), supplier);
    }

    /**
     * Run code on each element of this map.
     * @param action The behavior for all values.
     */
    public void forEach(BiConsumer<String, ? super V> action) {
        getMap().forEach(action);
    }

    /**
     * Get a value by an object's to string method.
     * @param obj The object to get the key for.
     * @return value
     */
    public V getValue(Object obj) {
        return get(obj.toString());
    }

    /**
     * Clear the contents of this map.
     */
    public void clear() {
        getMap().clear();
    }

    /**
     * Get the entry set of this map
     * @return entrySet
     */
    public Set<Map.Entry<String, V>> entrySet() {
        return getMap().entrySet();
    }

    /**
     * Is this map empty?
     * @return isEmpty
     */
    public boolean isEmpty() {
        return getMap().isEmpty();
    }

    /**
     * Put all entries into this map of anther map
     * @param map
     */
    public void putAll(JsonMap<V> map) {
        getMap().putAll(map.getMap());
    }
}
