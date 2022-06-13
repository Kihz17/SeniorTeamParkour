package com.kihz.utils.jsontools.serializers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.kihz.item.GUIItem;
import com.kihz.utils.jsontools.JsonSerializer;
import org.bukkit.ChatColor;
import org.bukkit.Material;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.function.Consumer;

public class ArraySerializer extends Serializer<Object> {

    public ArraySerializer() {
        super(null);
    }

    @Override
    public Object deserialize(JsonElement jsonElement, Class<Object> loadClass, Field field) {
        Class<?> arrayType = loadClass.getComponentType(); // Get the type of our array
        JsonArray jsonArray = jsonElement.getAsJsonArray();
        int length = jsonArray.size();
        int i = 0;
        Object array = Array.newInstance(arrayType, length); // Create a new array with the type of our loadClass and size of the jsonArray

        for(JsonElement e : jsonArray) // Replace each element in the new array with the JsonElement's deserialized data
            Array.set(array, i++, JsonSerializer.deserialize(arrayType, e));

        return array;
    }

    @Override
    public JsonElement serialize(Object value) {
        JsonArray jsonArray = new JsonArray();

        int length = Array.getLength(value);
        for(int i = 0; i < length; i++) {
            Object e = Array.get(value, i);
            jsonArray.add(JsonSerializer.addClassNoParent(e, JsonSerializer.save(e)));
        }

        return jsonArray;
    }

    @Override
    public boolean canApplyTo(Class<?> clazz) {
        return super.canApplyTo(clazz) || clazz.isArray();
    }

    @Override
    public void editItem(GUIItem item, Object value, Consumer<Object> setter) {
        item.setIcon(Material.MINECART)
                .addLore("Size: " + ChatColor.YELLOW + Array.getLength(value));
    }
}
