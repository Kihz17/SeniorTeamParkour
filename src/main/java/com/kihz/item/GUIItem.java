package com.kihz.item;

import com.kihz.gui.GUI;
import com.kihz.gui.GUIManager;
import com.kihz.gui.GUIType;
import com.kihz.utils.Utils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

@Getter
public class GUIItem extends DisplayItem {
    private Map<GUIClickType, List<Consumer<GUIClickEvent>>> listeners = new HashMap<>();

    public GUIItem(ItemWrapper iw, ItemStack item) {
        super(iw, item);
    }

    public GUIItem(ItemStack item) {
        super(item);
    }

    /**
     * Called when this item is clicked
     * @param evt The bukkit click event
     */
    public void onClick(InventoryClickEvent evt) {
        GUIClickEvent clickEvent = new GUIClickEvent((Player) evt.getWhoClicked(), evt.getCurrentItem(), evt);
        for(GUIClickType type : GUIClickType.values()) {
            if(!type.checkClickType(evt.getClick()))
                continue;
            List<Consumer<GUIClickEvent>> handlers = listeners.getOrDefault(type, new ArrayList<>());
            handlers.forEach(l -> l.accept(clickEvent));
        }
        evt.setCancelled(true);
    }

    /**
     * Listen for a left click
     * @param evt The behaviour when the item is left clicked
     * @return this
     */
    public GUIItem leftClick(Consumer<GUIClickEvent> evt) {
        return onClick(GUIClickType.LEFT, evt);
    }

    /**
     * Listen for a left click and add action to the lore.
     * @param evt The behaviour when the item is left clicked
     * @return this
     */
    public GUIItem leftClick(Consumer<GUIClickEvent> evt, String action) {
        leftClick(evt);
        return addLoreAction(GUIClickType.LEFT, action);
    }

    /**
     * Listen for a right click
     * @param evt The behaviour when the item is right clicked
     * @return this
     */
    public GUIItem rightClick(Consumer<GUIClickEvent> evt) {
        return onClick(GUIClickType.RIGHT, evt);
    }

    /**
     * Listen for a right click and add action to the lore.
     * @param evt The behaviour when the item is right clicked
     * @return this
     */
    public GUIItem rightClick(Consumer<GUIClickEvent> evt, String action) {
        rightClick(evt);
        return addLoreAction(GUIClickType.RIGHT, action);
    }

    /**
     * Listen for a middle click
     * @param evt The behaviour when the item is middle clicked
     * @return this
     */
    public GUIItem middleClick(Consumer<GUIClickEvent> evt) {
        return onClick(GUIClickType.MIDDLE, evt);
    }

    /**
     * Listen for a shift click
     * @param evt The behaviour when the item is shift clicked
     * @return this
     */
    public GUIItem shiftClick(Consumer<GUIClickEvent> evt) {
        return onClick(GUIClickType.SHIFT, evt);
    }

    /**
     * Listen for an any click
     * @param evt The behaviour when the item is any clicked
     * @return this
     */
    public GUIItem anyClick(Consumer<GUIClickEvent> evt) {
        return onClick(GUIClickType.ANY, evt);
    }

    /**
     * Clear a given listener type for this item
     * @param type The listener type to clear
     * @return this
     */
    public GUIItem clearListener(GUIClickType type) {
        getListeners().getOrDefault(type, new ArrayList<>()).clear();
        return this;
    }

    /**
     * Reconstruct the GUI if we have one since we want to update this item
     * @return this
     */
    public GUIItem reconstruct() {
        return anyClick(ce -> {
            if (ce.getGUI() != null)
                ce.getGUI().reconstruct();
        });
    }

    /**
     * Opens a given gui when this item is clicked
     * @param type The gui to open
     * @param clickType The click type we are looking for
     * @return this
     */
    public GUIItem opens(GUIType type, GUIClickType clickType) {
        onClick(clickType, e -> GUIManager.openGUI(e.getPlayer(), type));
        return this;
    }

    /**
     * Does this GUI item have a listener for a given click type?
     * @param type The click type to check for
     * @return hasListener
     */
    public boolean hasListener(GUIClickType type) {
        return listeners.containsKey(type) && !listeners.get(type).isEmpty();
    }

    /**
     * Listens for a given click type.
     * @param clickType The click type to listen for
     * @param evt The click event to add
     * @return this
     */
    private GUIItem onClick(GUIClickType clickType, Consumer<GUIClickEvent> evt) {
        listeners.computeIfAbsent(clickType, ce -> new ArrayList<>()).add(evt);
        return this;
    }

    /**
     * Add click action to the lore
     * @param type The click type
     * @param action The action
     * @return this
     */
    private GUIItem addLoreAction(GUIClickType type, String action) {
        addLoreAction(Utils.capitalize(type.name()), action);
        return this;
    }

    /**
     * A list of possible click types for a GUI item
     */
    @AllArgsConstructor
    public enum GUIClickType {
        LEFT(ClickType::isLeftClick),
        RIGHT(ClickType::isRightClick),
        MIDDLE(ct -> ct == ClickType.MIDDLE),
        SHIFT(ClickType::isShiftClick),
        ANY(ct -> true);

        private final Function<ClickType, Boolean> clickTypeCheck;

        /**
         * Check if the given type apply to this GUI item click
         * @param type The click type to check
         * @return isType
         */
        public boolean checkClickType(ClickType type) {
            return clickTypeCheck.apply(type);
        }
    }

    /**
     * Called when a click happens on a GUI item
     */
    @AllArgsConstructor @Getter
    public class GUIClickEvent {
        private Player player;
        private ItemStack clickedItem;
        private InventoryClickEvent event;

        /**
         * Returns the active GUI for the player in this click event
         * @return gui
         */
        public GUI getGUI() {
            return GUIManager.getGUI(getPlayer());
        }

    }
}
