package com.kihz.utils.jsontools.serializers.bukkit;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.kihz.item.GUIItem;
import com.kihz.utils.Utils;
import com.kihz.utils.jsontools.serializers.Serializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;

import java.lang.reflect.Field;
import java.util.function.Consumer;

public class LocationSerializer extends Serializer<Location> {

    public LocationSerializer() {
        super(Location.class);
    }

    @Override
    public Location deserialize(JsonElement jsonElement, Class<Location> loadClass, Field field) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        Location loc = new Location(null, 0, 0, 0);
        loc.setX(jsonObject.get("x").getAsDouble());
        loc.setY(jsonObject.get("y").getAsDouble());
        loc.setZ(jsonObject.get("z").getAsDouble());
        loc.setPitch(jsonObject.get("pitch").getAsFloat());
        loc.setYaw(jsonObject.get("yaw").getAsFloat());
        if(jsonObject.has("world"))
            loc.setWorld(Bukkit.getWorld(jsonObject.get("world").getAsString()));
        return loc;
    }

    @Override
    public JsonElement serialize(Location value) {
        if (value == null)
            return null;

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("x", value.getX());
        jsonObject.addProperty("y", value.getY());
        jsonObject.addProperty("z", value.getZ());
        jsonObject.addProperty("pitch", value.getPitch());
        jsonObject.addProperty("yaw", value.getYaw());
        if(value.getWorld() != null)
            jsonObject.addProperty("world", value.getWorld().getName());
        return jsonObject;
    }

    @Override
    public void editItem(GUIItem item, Object value, Consumer<Object> setter) {
        if (value != null) {
            Location loc = ((Location) value).clone();
            item.leftClick(ce -> {
                if (!ce.getEvent().isShiftClick()) {
                    if (loc.getWorld() == null) // If there is no world set, it's probably the world the player is in.
                        loc.setWorld(ce.getPlayer().getWorld());
                    ce.getPlayer().teleport(loc);
                }
            }).addLore("Location: " + ChatColor.GOLD + Utils.getCleanLocationString(loc), "").addLoreAction("Left", "Teleport");
        }
        item.shiftClick(ce -> setter.accept(ce.getPlayer().getLocation()))
                .setIcon(Material.ELYTRA).addLoreAction("Shift", "Set Location");
    }
}
