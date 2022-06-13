package com.kihz.utils.jsontools.containers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.kihz.utils.JsonUtils;
import com.kihz.utils.jsontools.JsonSerializer;
import com.kihz.utils.jsontools.Jsonable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.function.Predicate;
import java.util.stream.Stream;

@NoArgsConstructor @Getter
public class JsonPriorityQueue<T> implements Jsonable, Iterable<T> {
    private transient PriorityQueue<T> queue = new PriorityQueue<>();
    @Setter
    private Class<T> typeClass;

    public JsonPriorityQueue(PriorityQueue<T> queue) {
        queue.forEach(getQueue()::add);
    }

    public JsonPriorityQueue(Class<T> classType) {
        this.typeClass = classType;
    }

    @Override
    public void load(JsonElement array) {
        for (JsonElement element : array.getAsJsonArray()) {
            if (JsonUtils.isJsonNull(element))
                continue;

            T loadedValue = JsonSerializer.deserialize(getTypeClass(), element);
            if (loadedValue != null)
                getQueue().add(loadedValue);
        }
    }

    @Override
    public JsonElement save() {
        JsonArray array = new JsonArray();
        for (T value : getQueue()) {
            JsonElement savedValue = JsonSerializer.addClassNoParent(value, JsonSerializer.save(value));
            if (!JsonUtils.isJsonNull(savedValue))
                array.add(savedValue);
        }
        return array;
    }

    /**
     * Add an element to the queue
     * @param element The element to add
     */
    public void offer(T element) {
        getQueue().offer(element);
    }

    /**
     * Remove top element of the queue
     * @return element
     */
    public T poll() {
        return getQueue().poll();
    }

    /**
     * Get the top queued element of the queue
     * @return element
     */
    public T peek() {
        return getQueue().peek();
    }

    /**
     * Get the iterator for this queue
     * @return iterator
     */
    public Iterator<T> iterator() {
        return getQueue().iterator();
    }

    /**
     * Is the queue empty?
     * @return isEmpty
     */
    public boolean isEmpty() {
        return getQueue().isEmpty();
    }

    /**
     * Get a java stream of the values.
     * @return stream
     */
    public Stream<T> stream() {
        return getQueue().stream();
    }

    /**
     * Gets the sizeof this queue
     * @return size
     */
    public int size() {
        return getQueue().size();
    }

    /**
     * Remove an object from the queue
     * @param o The object to remove
     * @return removed
     */
    public boolean remove(T o) {
        return getQueue().remove(o);
    }

    /**
     * Remove a series of items if it adheres to the predicate
     * @param predicate The predicate to remove by
     * @return removed
     */
    public boolean removeIf(Predicate<? super T> predicate) {
        return getQueue().removeIf(predicate);
    }

    /**
     * Check if this queue contains a given object
     * @param object the object to check for
     * @return contains
     */
    public boolean contains(T object) {
        return getQueue().contains(object);
    }
}
