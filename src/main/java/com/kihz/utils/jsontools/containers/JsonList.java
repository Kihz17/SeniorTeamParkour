package com.kihz.utils.jsontools.containers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.kihz.utils.JsonUtils;
import com.kihz.utils.jsontools.JsonSerializer;
import com.kihz.utils.jsontools.Jsonable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

@Getter @NoArgsConstructor
public class JsonList<T> implements Jsonable, Iterable<T> {
    private transient List<T> values = new ArrayList<>();
    @Setter
    private Class<T> typeClass;

    public JsonList(Iterable<T> values) {
        values.forEach(getValues()::add);
    }

    public JsonList(Class<T> classType) {
        this.typeClass = classType;
    }

    /**
     * Load values from a json array.
     * @param array The array to load values from.
     */
    @Override
    public void load(JsonElement array) {
        for (JsonElement element : array.getAsJsonArray()) {
            if (JsonUtils.isJsonNull(element))
                continue;

            T loadedValue = JsonSerializer.deserialize(getTypeClass(), element);
            if (loadedValue != null)
                getValues().add(loadedValue);
        }
    }

    /**
     * Save the values of this into a JsonArray.
     * @return JsonArray
     */
    @Override
    public JsonElement save() {
        JsonArray array = new JsonArray();
        for (T value : getValues()) {
            JsonElement savedValue = JsonSerializer.addClassNoParent(value, JsonSerializer.save(value));
            if (!JsonUtils.isJsonNull(savedValue)) // Don't check if default value, because lists need to save default values.
                array.add(savedValue);
        }
        return array;
    }

    @Override
    public Iterator<T> iterator() {
        return getValues().iterator();
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        getValues().forEach(action);
    }

    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder("[");
        for (T val : getValues())
            ret.append(ret.length() > 1 ? ", " : "").append(val);
        return ret.append("]").toString();
    }

    /**
     * Set a value at the given index.
     * @param index The index to set the value at
     * @param value The value to set
     * @return replacedValue
     */
    public T set(int index, T value) {
        return getValues().set(index, value);
    }

    /**
     * Add a value to the list.
     * @param val The value to add.
     */
    public void add(T val) {
        getValues().add(val);
    }

    /**
     * Add a value to the list.
     * @param val The value to add.
     */
    public void add(int index, T val) {
        getValues().add(index, val);
    }

    /**
     * Add a collection of values to this list.
     * @param values The values to add.
     */
    public void addAll(Collection<T> values) {
        getValues().addAll(values);
    }

    /**
     * Add an array of values to this list.
     * @param values The values to add.
     */
    public void addAll(T[] values) {
        for (T value : values)
            add(value);
    }

    /**
     * Return the value at the given index.
     * @param index The index of the element to get.
     * @return value
     */
    public T get(int index) {
        return getValues().get(index);
    }

    /**
     * Get the first value. Fails if the first value is absent.
     * @return firstValue
     */
    public T getFirst() {
        return getValues().get(0);
    }

    /**
     * Gets the first value.
     * @return firstValue - Might be null.
     */
    public T getFirstSafe() {
        return getValueSafe(0);
    }

    /**
     * Is this list empty?
     * @return empty
     */
    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     * Remove a value from the list by its index.
     * @param index The index to remove.
     * @return removedValue - If there was one.
     */
    public T remove(int index) {
        return hasIndex(index) ? getValues().remove(index) : null;
    }

    /**
     * Remove a value from the list.
     * Returns whether or not the element was removed successfully.
     * @param val The value to remove.
     * @return wasRemoved - Was the value removed.
     */
    public boolean remove(T val) {
        return getValues().remove(val);
    }

    /**
     * Returns the size of the list.
     * @return size
     */
    public int size() {
        return getValues().size();
    }

    /**
     * Does this list contain the given value?
     * @param value The value to check if we contain.
     * @return contains
     */
    public boolean contains(T value) {
        return getValues().contains(value);
    }

    /**
     * Return the last element of this list, if possible.
     * @return lastElement
     */
    public T last() {
        return getValueSafe(size() - 1);
    }

    /**
     * Removes the last value.
     * @return lastValue
     */
    public T removeLast() {
        return remove(size() - 1);
    }

    /**
     * Is a given index without our range of values?
     * @param index The index to test.
     * @return hasIndex
     */
    public boolean hasIndex(int index) {
        return index >= 0 && size() > index;
    }

    /**
     * Returns the value at the given index if we have it, otherwise null.
     * @param index The index of the value to get.
     * @return value
     */
    public T getValueSafe(int index) {
        return hasIndex(index) ? get(index) : null;
    }

    /**
     * Clear the values.
     */
    public void clear() {
        getValues().clear();
    }

    /**
     * Get a java stream of the values.
     * @return stream
     */
    public Stream<T> stream() {
        return getValues().stream();
    }
}
