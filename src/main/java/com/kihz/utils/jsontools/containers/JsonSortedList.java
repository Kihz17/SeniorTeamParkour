package com.kihz.utils.jsontools.containers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.kihz.utils.JsonUtils;
import com.kihz.utils.jsontools.JsonSerializer;
import com.kihz.utils.jsontools.Jsonable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Stream;

@NoArgsConstructor
@Getter
public class JsonSortedList<T extends Comparable<T>> implements Jsonable, Iterable<T> {
    private transient List<T> sortedList = new ArrayList<>();
    @Setter
    private Class<T> typeClass;

    public JsonSortedList(Iterable<T> values) {
        values.forEach(getSortedList()::add);
    }

    public JsonSortedList(JsonSortedList<T> set) {
        this.typeClass = set.getTypeClass();
        for (T t : set)
            getSortedList().add(t);
    }

    public JsonSortedList(Class<T> typeClass) {
        this.typeClass = typeClass;
    }

    @Override
    public void load(JsonElement array) {
        for (JsonElement element : array.getAsJsonArray()) {
            if (JsonUtils.isJsonNull(element))
                continue;

            T loadedValue = JsonSerializer.deserialize(getTypeClass(), element);
            if (loadedValue != null)
                getSortedList().add(loadedValue);
        }
    }

    @Override
    public JsonElement save() {
        JsonArray array = new JsonArray();
        for (T value : getSortedList()) {
            JsonElement savedValue = JsonSerializer.addClassNoParent(value, JsonSerializer.save(value));
            if (!JsonUtils.isJsonNull(savedValue))
                array.add(savedValue);
        }
        return array;
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return getSortedList().iterator();
    }

    /**
     * Add an element to the sorted set
     * @param element The element to add
     * @return added
     */
    public boolean add(T element) {
        boolean value = getSortedList().add(element);
        Collections.sort(getSortedList());
        return value;
    }

    /**
     * Add an element to the sorted set
     * @param element The element to add
     * @return added
     */
    public boolean add(T element, Comparator<T> comparator) {
        boolean value = getSortedList().add(element);
        getSortedList().sort(comparator);
        return value;
    }

    /**
     * Add an entire collection to this sorted set
     * @param collection The collection to add
     * @return success
     */
    public boolean addAll(Collection<? extends T> collection) {
        boolean value = getSortedList().addAll(collection);
        Collections.sort(getSortedList());
        return value;
    }

    /**
     * Add an entire collection to this sorted set
     * @param collection The collection to add
     * @return success
     */
    public boolean addAll(Collection<? extends T> collection, Comparator<T> comparator) {
        boolean value = getSortedList().addAll(collection);
        getSortedList().sort(comparator);
        return value;
    }

    /**
     * Get the first object in this set
     * @return first
     */
    public T first() {
        if(getSortedList().isEmpty())
            return null;

        return getSortedList().get(0);
    }

    /**
     * Get the last object in this set
     * @return last
     */
    public T last() {
        if(getSortedList().isEmpty())
            return null;

        return getSortedList().get(getSortedList().size() - 1);
    }

    /**
     * Get this size og this sorted set
     * @return size
     */
    public int size() {
        return getSortedList().size();
    }

    /**
     * Is the set empty
     * @return isEmpty
     */
    public boolean isEmpty() {
        return getSortedList().isEmpty();
    }

    /**
     * Remove an obejct from the set
     * @param object The object to remove
     * @return success
     */
    public boolean remove(T object) {
        return getSortedList().remove(object);
    }

    /**
     * Stream this sorted set
     * @return stream
     */
    public Stream<T> stream() {
        return getSortedList().stream();
    }

    /**
     * Check if this set contains a given object
     * @param o The object to check for
     * @return contains
     */
    public boolean contains(T o) {
        return getSortedList().contains(o);
    }

    /**
     * Get the index of an object
     * @param o The object to get the index of
     * @return index
     */
    public int indexOf(T o) {
        return getSortedList().indexOf(o);
    }

    /**
     * Get an object at the given index
     * @param index The index of the object
     * @return object
     */
    public T get(int index) {
        return getSortedList().get(index);
    }
}