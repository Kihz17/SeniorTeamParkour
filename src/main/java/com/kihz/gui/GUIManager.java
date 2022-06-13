package com.kihz.gui;

import com.kihz.mechanics.system.GameMechanic;
import com.kihz.utils.Utils;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GUIManager extends GameMechanic {

    private static final Map<HumanEntity, GUI> guiMap = new HashMap<>();

    @Override
    public void onDisable() {
        new ArrayList<>(guiMap.keySet()).forEach(HumanEntity::closeInventory);
    }

    @EventHandler
    public void onClick(InventoryClickEvent evt) {
        if(guiMap.containsKey(evt.getWhoClicked()))
            guiMap.get(evt.getWhoClicked()).onClick(evt);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent evt) {
        GUI gui = guiMap.get(evt.getPlayer());
        if(gui != null && !gui.isPreventCloseHook() && !gui.isParent()) {
            guiMap.remove(evt.getPlayer());
            gui.onClose();
        }
    }

    @EventHandler // Prevents exploits
    public void onCursorClose(InventoryCloseEvent evt) {
        Utils.clearCursor((Player) evt.getPlayer());
    }

    @EventHandler
    public void onDrag(InventoryDragEvent evt) { // Stop dragging in GUIs
        if(guiMap.containsKey(evt.getWhoClicked()))
            evt.setCancelled(true);
    }

    /**
     * Get the active GUI for a given player.
     * @param player The player to get the GUI for
     * @return gui
     */
    public static GUI getGUI(Player player) {
        return guiMap.get(player);
    }

    /**
     * Add the gui the given player is currently viewing in the map
     * @param player The player to add
     * @param gui The gui to add
     */
    public static void setGUI(Player player, GUI gui) {
        guiMap.put(player, gui);
    }

    /**
     * Open a GUI type for a given player
     * @param p The player to open the GUI for
     * @param type The gui type to open
     */
    public static void openGUI(Player p, GUIType type) {
        type.construct(p);
    }

    /**
     * Get a list of GUIs of a certain type
     * @param clazz The gui instance
     * @return players
     */
    public static <T extends GUI> List<GUI> getGUIs(Class<T> clazz) {
        return guiMap.values().stream()
                .filter(clazz::isInstance)
                .collect(Collectors.toList());
    }
}
