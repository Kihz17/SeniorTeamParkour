package com.kihz.utils.jsontools.serializers;

import com.google.gson.JsonElement;
import com.kihz.item.GUIItem;
import com.kihz.utils.ReflectionUtil;
import com.kihz.utils.jsontools.JsonSerializer;
import com.kihz.utils.jsontools.Jsonable;
import org.bukkit.Material;

import java.lang.reflect.Field;
import java.util.function.Consumer;

public class JsonableSerializer  extends Serializer<Jsonable> {

    public JsonableSerializer() {
        super(Jsonable.class);
    }

    @Override
    public Jsonable deserialize(JsonElement jsonElement, Class<Jsonable> loadClass, Field field) {
        Jsonable jsonable = ReflectionUtil.construct(loadClass);
        jsonable.load(jsonElement);
        return jsonable;
    }

    @Override
    public JsonElement serialize(Jsonable value) {
        return value.save();
    }

    @Override
    public void editItem(GUIItem item, Object value, Consumer<Object> setter, Class<?> classType) {
        Jsonable jsonable = (Jsonable) value;
        if(jsonable == null) {
            item.leftClick(ce -> JsonSerializer.makeDefaultValue(ce.getPlayer(), classType, data -> {
                setter.accept(data);
                JsonSerializer.editJSON(ce.getPlayer(), (Jsonable) data);
            }), "Create Value");
        } else {
            item.leftClick(ce -> JsonSerializer.editJSON(ce.getPlayer(), jsonable));
        }
        item.setIcon(Material.SPAWNER);
        setNull(item, value, setter);
    }
}
