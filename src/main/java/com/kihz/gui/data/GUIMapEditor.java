package com.kihz.gui.data;

import com.kihz.item.GUIItem;
import com.kihz.mechanics.Callbacks;
import com.kihz.utils.jsontools.containers.JsonMap;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

public class GUIMapEditor<T> extends GUIJsonEditor {
    private JsonMap<T> map;

    public GUIMapEditor(Player p, JsonMap<T> map) {
        super(p);
        this.map = map;
    }

    public GUIMapEditor(Player p, JsonMap<T> map, Runnable runnable) {
        super(p, runnable);
        this.map = map;
    }

    @Override
    public void addFields() {
        map.forEach((k, v) -> addItem(k, v.getClass(), v, val -> map.put(k, (T) val), k));
        toRight(2);
        if(map.getTypeClass() == null) {
            addItem(Material.WHITE_WOOL, ChatColor.RED + "Can't Add Element", "Elements can only be added after the data is loaded.");
            return;
        }

        addItem(Material.WHITE_WOOL, ChatColor.GREEN + "Add Element", "Click here to add an element to this map.")
                .anyClick(ce -> {
                    ce.getPlayer().sendMessage(ChatColor.GREEN + "Enter the key for the new element.");
                    Callbacks.listenForChat(ce.getPlayer(), message -> {
                        if(map.containsKey(message)) {
                            ce.getPlayer().sendMessage(ChatColor.RED + "That key already exists in this map.");
                            return;
                        }

                        makeDefaultValue(map.getTypeClass(), val -> map.put(message, val));
                        ce.getPlayer().sendMessage(ChatColor.GREEN + "Added element.");
                    });
                });
    }

    protected GUIItem addItem(String name, Class<?> clazz, Object value, Consumer<Object> setter, String key) {
        GUIItem guiItem = addItem(name, clazz, value, setter);
        guiItem.clearListener(GUIItem.GUIClickType.RIGHT).rightClick(ce -> map.remove(key), "Remove Value").reconstruct();
        guiItem.middleClick(ce -> {
            ce.getPlayer().sendMessage(ChatColor.GREEN + "What should the key '" + key + "' be changed to?");
            Callbacks.listenForChat(ce.getPlayer(), message -> map.put(message, map.remove(key)));
        }).addLoreAction("Middle", "Edit Key");
        return guiItem;
    }

}
