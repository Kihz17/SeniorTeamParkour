package com.kihz.utils.jsontools.serializers;

import com.google.gson.JsonElement;
import com.kihz.item.GUIItem;
import com.kihz.mechanics.Callbacks;
import com.kihz.utils.ReflectionUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;

import java.lang.reflect.Field;
import java.util.function.Consumer;
import java.util.function.Function;

public class CustomSerializer<T> extends Serializer<T> {
    private final Function<JsonElement, T> deserialize;
    private final Function<T, JsonElement> serialize;
    private final Function<String, T> parse;
    private final T fallbackValue;
    private final Material icon;

    public CustomSerializer(Class<T> clazz, Function<JsonElement, T> deserialize, Function<T, JsonElement> serialize, Function<String, T> parse, T fallbackValue, Material icon) {
        super(clazz);
        this.deserialize = deserialize;
        this.serialize = serialize;
        this.parse = parse;
        this.fallbackValue = fallbackValue;
        this.icon = icon;
    }

    @Override
    public T deserialize(JsonElement jsonElement, Class<T> loadClass, Field field) {
        return deserialize.apply(jsonElement);
    }

    @Override
    public JsonElement serialize(T value) {
        return serialize.apply(value);
    }

    @Override
    public boolean canApplyTo(Class<?> clazz) {
        return super.canApplyTo(clazz) || (clazz.isPrimitive() && ReflectionUtil.getPrimitive(getApplyTo()).isAssignableFrom(clazz));
    }

    @Override
    public void editItem(GUIItem item, Object value, Consumer<Object> setter) {
        if (parse != null) { // If there is loading behavior.
            item.leftClick(ce -> {
                ce.getPlayer().sendMessage(ChatColor.YELLOW + "Please enter the new value.");
                Callbacks.listenForChat(ce.getPlayer(), m -> {
                    try {
                        setter.accept(parse.apply(m));
                        ce.getPlayer().sendMessage(ChatColor.GREEN + "Value updated.");
                    } catch (Exception e) {
                        ce.getPlayer().sendMessage(ChatColor.RED + "Failed to parse '" + m + "'.");
                    }
                }, "Value Update");
            }, "Set Value");
        }

        item.rightClick(ce -> {
            setter.accept(fallbackValue);
            ce.getPlayer().sendMessage(ChatColor.GREEN + "Value removed.");
        }, "Remove Value");

        if (icon != null)
            item.setIcon(icon);
    }
}