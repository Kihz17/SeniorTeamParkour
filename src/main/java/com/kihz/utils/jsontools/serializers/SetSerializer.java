package com.kihz.utils.jsontools.serializers;


import com.google.gson.JsonElement;
import com.kihz.gui.data.GUISetEditor;
import com.kihz.item.GUIItem;
import com.kihz.utils.ReflectionUtil;
import com.kihz.utils.jsontools.containers.JsonSet;
import org.bukkit.Material;

import java.lang.reflect.Field;
import java.util.function.Consumer;

public class SetSerializer extends Serializer<JsonSet> {

    public SetSerializer() {
        super(JsonSet.class);
    }

    @Override
    public JsonSet deserialize(JsonElement jsonElement, Class<JsonSet> loadClass, Field field) {
        JsonSet jsonSet = new JsonSet(ReflectionUtil.getGenericType(field));
        jsonSet.load(jsonElement);
        return jsonSet;
    }

    @Override
    public JsonElement serialize(JsonSet value) {
        return value.save();
    }

    @Override
    public void editItem(GUIItem item, Object value, Consumer<Object> setter) {
        item.leftClick(ce -> new GUISetEditor<>(ce.getPlayer(), (JsonSet<?>) value), "Edit Values")
                .setIcon(Material.CHEST_MINECART);
    }
}
