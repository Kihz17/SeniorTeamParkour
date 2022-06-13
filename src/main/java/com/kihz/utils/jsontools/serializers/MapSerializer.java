package com.kihz.utils.jsontools.serializers;

import com.google.gson.JsonElement;
import com.kihz.gui.data.GUIMapEditor;
import com.kihz.item.GUIItem;
import com.kihz.utils.ReflectionUtil;
import com.kihz.utils.jsontools.containers.JsonMap;
import org.bukkit.Material;

import java.lang.reflect.Field;
import java.util.function.Consumer;

public class MapSerializer extends Serializer<JsonMap> {

    public MapSerializer() {
        super(JsonMap.class);
    }

    @Override
    public JsonMap deserialize(JsonElement jsonElement, Class<JsonMap> loadClass, Field field) {
        JsonMap jsonMap = new JsonMap(ReflectionUtil.getGenericType(field));
        jsonMap.load(jsonElement);
        return jsonMap;
    }

    @Override
    public JsonElement serialize(JsonMap value) {
        return value.save();
    }

    @Override
    public void editItem(GUIItem item, Object value, Consumer<Object> setter) {
        item.setIcon(Material.CHEST);
        item.leftClick(ce -> new GUIMapEditor<>(ce.getPlayer(), (JsonMap<?>) value), "Edit Values");
    }
}

