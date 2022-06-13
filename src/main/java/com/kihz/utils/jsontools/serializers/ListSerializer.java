package com.kihz.utils.jsontools.serializers;

import com.google.gson.JsonElement;
import com.kihz.gui.data.GUIListEditor;
import com.kihz.item.GUIItem;
import com.kihz.utils.ReflectionUtil;
import com.kihz.utils.jsontools.containers.JsonList;
import org.bukkit.Material;

import java.lang.reflect.Field;
import java.util.function.Consumer;

public class ListSerializer extends Serializer<JsonList> {

    public ListSerializer() {
        super(JsonList.class);
    }

    @Override
    public JsonList deserialize(JsonElement jsonElement, Class<JsonList> loadClass, Field field) {
        JsonList jsonList = new JsonList(ReflectionUtil.getGenericType(field));
        jsonList.load(jsonElement);
        return jsonList;
    }

    @Override
    public JsonElement serialize(JsonList value) {
        return value.save();
    }

    @Override
    public void editItem(GUIItem item, Object value, Consumer<Object> setter) {
        item.leftClick(ce -> new GUIListEditor<>(ce.getPlayer(), (JsonList<?>) value), "Edit Values")
                .setIcon(Material.MINECART);
    }
}
