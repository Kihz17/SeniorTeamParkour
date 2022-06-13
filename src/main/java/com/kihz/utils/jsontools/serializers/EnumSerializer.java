package com.kihz.utils.jsontools.serializers;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.kihz.gui.data.GUIEnumSelector;
import com.kihz.item.GUIItem;
import com.kihz.utils.Utils;
import org.bukkit.Material;

import java.lang.reflect.Field;
import java.util.function.Consumer;

public class EnumSerializer extends Serializer<Enum> {

    public EnumSerializer() {
        super(Enum.class);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Enum deserialize(JsonElement jsonElement, Class<Enum> loadClass, Field field) {
        return Utils.getEnum(jsonElement.getAsString(), loadClass);
    }

    @Override
    public JsonElement serialize(Enum value) {
        return new JsonPrimitive(value.name());
    }

    @Override
    public boolean canApplyTo(Class<?> clazz) {
        return super.canApplyTo(clazz) | clazz.equals(Boolean.TYPE);
    }

    @Override
    public void editItem(GUIItem item, Object value, Consumer<Object> setter, Class<?> type) {
        item.leftClick(ce -> new GUIEnumSelector(ce.getPlayer(), (Enum[]) type.getEnumConstants(), setter), "Set Value")
                .setIcon(Material.GOLD_BLOCK);
    }
}
