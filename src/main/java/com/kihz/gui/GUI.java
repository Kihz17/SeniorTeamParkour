package com.kihz.gui;

import com.kihz.Core;
import com.kihz.item.GUIItem;
import com.kihz.item.ItemManager;
import com.kihz.item.ItemWrapper;
import com.kihz.mechanics.Callbacks;
import com.kihz.utils.MetadataUtils;
import com.kihz.utils.PacketUtils;
import com.kihz.utils.Utils;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Getter
public abstract class GUI {
    private final Player player;
    private Inventory inventory;
    private final String title;
    protected Map<Integer, GUIItem> itemMap = new HashMap<>();
    @Setter
    private boolean allowStorage;
    @Setter private int slotIndex;
    private GUI previous;
    @Setter private boolean parent;
    protected boolean preventCloseHook;
    private int rowSpoof;

    public static int ROW_SIZE = 9;
    public static final int MAX_ROWS = 6;
    public static final int MAX_SLOT = 54;
    public static final String STOP_GUI_META = "cantOpenGUI";

    public static final List<InventoryAction> IGNORE = Arrays.asList(InventoryAction.COLLECT_TO_CURSOR,
            InventoryAction.DROP_ONE_SLOT, InventoryAction.DROP_ALL_SLOT, InventoryAction.NOTHING, InventoryAction.UNKNOWN);

    public GUI(Player player, String title) {
        this(player, title, 1);
    }

    public GUI(Player player, String title, int rows) {
        this.player = player;
        this.title = title;
        this.inventory = Bukkit.createInventory(null, rows * ROW_SIZE, Component.text(title));

        GUI last = GUIManager.getGUI(player);
        if(last != null && !this.getClass().equals(last.getClass())) {
            this.previous = last;
        } else if(last != null) {
            this.previous = last.getPrevious();
        }

        flagAsSub();
        Bukkit.getScheduler().runTask(Core.getInstance(), this::open);
    }

    /**
     * Creates a GUI.
     */
    public abstract void addItems();

    /**
     * Open this GUI.
     */
    public void open() {
        if(MetadataUtils.hasMetadata(getPlayer(), STOP_GUI_META)) {
            getPlayer().sendMessage(ChatColor.RED + "You cant open a GUI right now.");
            return;
        }

        Bukkit.getScheduler().runTask(Core.getInstance(), () -> {
            reconstruct();
            getPlayer().openInventory(getInventory());
            GUIManager.setGUI(getPlayer(), this);
            setParent(false);
        });
    }

    /**
     * Close this GUI.
     */
    public void close() {
        clear(); // Remove all items from the gui.
        Bukkit.getScheduler().runTask(Core.getInstance(), () -> getPlayer().closeInventory(InventoryCloseEvent.Reason.UNKNOWN));
    }

    /**
     * Reconstruct this GUI inventory.
     */
    public void reconstruct() {
        this.preventCloseHook = true;
        clear();
        addItems();

        // Remake the inventory with correct slots
        Inventory oldInventory = getInventory();
        int maxSlot = getMaxSlot();
        if (maxSlot >= getInventory().getSize() || getInventory().getSize() > ((maxSlot + 1) / ROW_SIZE) * ROW_SIZE)
            remakeInventory();

        showItems();
        addStorageItems();
        getPlayer().updateInventory();
        if(oldInventory.getViewers().contains(getPlayer()) && !oldInventory.equals(getInventory()))
            getPlayer().openInventory(getInventory());
        this.preventCloseHook = false;
    }

    /**
     * Clears this GUI
     */
    public void clear() {
        if(isAllowStorage()) {
            getItemMap().keySet().forEach(slot -> getInventory().setItem(slot, new ItemStack(Material.AIR))); // Clear all items that a player didn't put inside
        } else {
            getInventory().clear();
        }
        getItemMap().clear();
        setSlotIndex(0);
    }

    /**
     * Handle clicks in the GUI
     * @param evt The click event
     */
    public void onClick(InventoryClickEvent evt) {
        InventoryAction action = evt.getAction();
        if(IGNORE.contains(action)) {
            evt.setCancelled(true);
            return;
        }

        Player p = (Player) evt.getWhoClicked();
        if(Callbacks.hasListener(p)) { // cancel if we have an active callback
            evt.setCancelled(true);
            return;
        }

        if(!isAllowStorage())
            evt.setCancelled(true);
        Inventory topInv = evt.getView().getTopInventory();
        boolean isTop = topInv.equals(evt.getClickedInventory());
        boolean isBottom = evt.getView().getBottomInventory().equals(evt.getClickedInventory());
        int slot = evt.getRawSlot();
        ItemStack itemToAdd = null;
        Consumer<ItemStack> itemConsumer = null;
        Supplier<ItemStack> itemSupplier = null;

        // This is pretty messy, but inventory events are messy by nature ¯\_(ツ)_/¯
        if(slot >= 0 && isTop) {
            GUIItem item = getItem(slot);
            if(item == null) {
                if(Utils.isHotbarEvent(evt)) { // Handle hotbar related stuff
                    itemToAdd = evt.getWhoClicked().getInventory().getItem(evt.getHotbarButton());
                    itemConsumer = i -> evt.getWhoClicked().getInventory().setItem(evt.getHotbarButton(), i);
                    itemSupplier = () -> new ItemStack(Material.AIR);
                } else if(action == InventoryAction.PLACE_ALL || action == InventoryAction.PLACE_ONE || action == InventoryAction.SWAP_WITH_CURSOR) { // Handle left/right clicks
                    itemToAdd = evt.getCursor().clone();
                    if(action == InventoryAction.PLACE_ONE)
                        itemToAdd.setAmount(1);

                    final ItemStack cursor = itemToAdd;
                    itemConsumer = player::setItemOnCursor;
                    itemSupplier = () -> Utils.useItem(evt.getCursor(), cursor.getAmount());
                }
            } else {
                item.onClick(evt);
            }
        } else if(evt.isShiftClick() && isBottom) {
            if(isAllowStorage() && topInv.firstEmpty() > -1) {
                slot = topInv.firstEmpty();
                itemToAdd = evt.getCurrentItem();
                itemConsumer = evt::setCurrentItem;
                itemSupplier = () -> new ItemStack(Material.AIR);
            } else {
                evt.setCancelled(true);
            }
        }

        if(!Utils.isAir(itemToAdd)) { // Try to add the item
            if(canDeposit(slot, itemToAdd)) {
                final Consumer<ItemStack> result = itemConsumer;
                final Supplier<ItemStack> defaultResult = itemSupplier;
                onDeposit(itemToAdd, evt, result, () -> {
                    evt.setCancelled(true);
                    result.accept(defaultResult.get());
                    reconstruct();
                });
                Bukkit.getScheduler().runTask(Core.getInstance(), p::updateInventory);
            } else {
                evt.setCancelled(true);
            }
        }
    }

    /**
     * Open the previous GUI, if possible.
     * Closes this GUI if not.
     */
    public void openPrevious() {
        if (getPrevious() != null) {
            getPrevious().open();
        } else {
            close();
        }
    }

    /**
     * Set a new title for this inventory
     * @param title the new title to set
     */
    public void setTitle(String title) {
        Bukkit.getScheduler().runTask(Core.getInstance(), () -> { // Need to run a tick to ensure this runs after the window is created
            if(GUIManager.getGUI(getPlayer()) == this)
                PacketUtils.updateInventoryTitle(getPlayer(), title);
        });
    }

    /**
     * Add an item to this GUI
     * @param material The type to add
     * @param name The name of the item
     * @param lore The lore on the item
     * @return item
     */
    protected GUIItem addItem(Material material, String name, String... lore) {
        return addItem(material, name, false, lore);
    }

    /**
     * Add an item to this GUI
     * @param material The type to add
     * @param name The name of the item
     * @param lore The lore on the item
     * @return item
     */
    protected GUIItem addItem(Material material, String name, boolean enchanted, String... lore) {
        return addItem(ItemManager.createItem(material, name, enchanted, lore));
    }

    /**
     * Add an item to this GUI
     * @param item The item to add
     * @return item
     */
    protected GUIItem addItem(ItemStack item) {
        return Utils.isAir(item) ? null : addItem(new GUIItem(item));
    }

    /**
     * Add an item to this GUI
     * @param item The item to add
     * @return item
     */
    protected GUIItem addItem(GUIItem item) {
        return addItem(item, false);
    }

    /**
     * Add an item to this GUI
     * @param iw The original item wrapper
     * @param item The item to add
     * @return item
     */
    protected GUIItem addItem(ItemWrapper iw, ItemStack item) {
        return addItem(new GUIItem(iw, item));
    }

    /**
     * Add an item to this GUI
     * @param item The item to add
     * @param enchanted Should the item be enchanted?
     * @return item
     */
    protected GUIItem addItem(GUIItem item, boolean enchanted) {
        if(enchanted)
            item.setEnchanted(true);
        boolean spoof = getRowSpoof() != 0;
        itemMap.put(getSlotIndex() + (spoof ? getRowSpoof() * ROW_SIZE - 1 : 0), item);
        if (!spoof) // if we're spoofing the row we don't need to increase the slot.
            nextSlot();
        return item;
    }

    /**
     * Skip one slot
     */
    protected void nextSlot() {
        skipSlots(1);
    }

    /**
     * Move the slot index forward for a given amount of skips.
     * @param skip The amount of slots to skip
     */
    protected void skipSlots(int skip) {
        this.slotIndex += skip;
    }

    /**
     * Fill a row with glass
     * @param glassColor The glass to fill with
     */
    protected void fillGlass(Material glassColor) {
        fillRow(ItemManager.createItem(glassColor, ""));
    }

    /**
     * Fill a row with a given item.
     * @param item the item to fill with
     */
    protected void fillRow(ItemStack item) {
        fill(item, (((getSlotIndex() / ROW_SIZE) + 1) * ROW_SIZE) - getSlotIndex());
    }

    /**
     * Fill a given number of slots with an item
     * @param type The item to fill with
     * @param slots The amount of slots to fill for
     */
    protected void fill(Material type, int slots) {
        for(int i = 0; i < slots; i++)
            addItem(ItemManager.createItem(type, ""));
    }

    /**
     * Fill a given number of slots with an item
     * @param item The item to fill with
     * @param slots The amount of slots to fill for
     */
    protected void fill(ItemStack item, int slots) {
        for(int i = 0; i < slots; i++)
            addItem(item);
    }

    /**
     * Add a button to return to the previous menu.
     */
    protected void addBackButtonRight() {
        toRight(1);
        addItem(Material.BARRIER, ChatColor.RED + "Click here to return the the previous menu.").anyClick(e -> openPrevious());
    }

    /**
     * Add a button to return to the previous menu.
     */
    protected void addBackButton() {
        addItem(Material.BARRIER, ChatColor.RED + "Click here to return the the previous menu.").anyClick(e -> openPrevious());
    }

    /**
     * Go to the bottom row of this GUI
     */
    protected void toBottom() {
        setSlotIndex(getInventory().getSize() - ROW_SIZE);
    }

    /**
     * Add a toggle button based off of state
     * @param state The state of the toggle
     * @param message The message of the toggle
     */
    protected GUIItem addToggleButton(boolean state, String message) {
        return addItem(!state ? Material.GRAY_DYE : Material.GREEN_DYE, ChatColor.GRAY + "Click here to " + ChatColor.BOLD.toString() +
                ChatColor.UNDERLINE + (state ? ChatColor.RED + "DISABLE " : ChatColor.GREEN + "ENABLE ") + ChatColor.GRAY + message);
    }

    /**
     * Skips to the right edge of the current row.
     * @param items The amount of items from the right-side of the gui.
     */
    protected void toRight(int items) {
        int oldIndex = getSlotIndex();
        nextRow();
        setSlotIndex(getSlotIndex() - items);
        if (getSlotIndex() < oldIndex) {
            skipSlots(ROW_SIZE);
            toRight(items);
        }
    }

    /**
     * Center items in the GUI for a given number of items
     * @param size The number of items to center
     */
    protected void center(int size) {
        skipSlots(((ROW_SIZE / 2) - getRowCounter() - (size / 2)));
    }

    /**
     * Get the current row coord
     * @return rowCounter
     */
    protected int getRowCounter() {
        return getSlotIndex() % ROW_SIZE;
    }

    protected int getRowCount() {
        if(getSlotIndex() <= 8) {
            return 0;
        } else if(getSlotIndex() <= 17) {
            return 1;
        } else if(getSlotIndex() <= 26) {
            return 2;
        } else if(getSlotIndex() <= 35) {
            return 3;
        } else if(getSlotIndex() <= 44) {
            return 4;
        } else if(getSlotIndex() <= 53){
            return 5;
        } else {
            return 6;
        }
    }

    /**
     * Skips to the next row in the GUI.
     */
    protected void nextRow() {
        if (getSlotIndex() % 9 != 0)
            setSlotIndex((((getSlotIndex() - 1) / ROW_SIZE) + 1) * ROW_SIZE);
    }

    /**
     * Get the maximum inventory slot index used in this item Map.
     * @return maxSlot
     */
    protected int getMaxSlot() {
        return getItemMap().keySet().stream().mapToInt(Integer::intValue).max().orElse(0);
    }

    /**
     * Flags this gui as a sub gui.
     * onClose will not be called on the parent GUI when this one replaces it.
     */
    protected void flagAsSub() {
        if(getPrevious() != null)
            getPrevious().setParent(true);
    }

    /**
     * Remake the inventory with correct slots.
     */
    protected void remakeInventory() {
        makeInventory((int) Math.ceil((getMaxSlot() + 1) / 9D));
    }

    /**
     * Make a bukkit inventory for this GUI.
     * @param rows
     */
    protected void makeInventory(int rows) {
        this.inventory = Bukkit.createInventory(null, rows * ROW_SIZE, Component.text(getTitle()));
    }

    /**
     * Put the items in the GUI's inventory.
     */
    protected void showItems() {
        itemMap.entrySet().forEach(entry -> getInventory().setItem(entry.getKey(), entry.getValue().generateItem()));
    }

    /**
     * Temporarily increment the row.
     */
    protected void spoofRow() {
        this.rowSpoof++;
    }

    /**
     * Get the GUIItem at a given index.
     * @param index The index to get the item at
     * @return guiItem
     */
    protected GUIItem getItem(int index) {
        return itemMap.get(index);
    }

    /**
     * Can the given item be deposited into the given slot?
     * @param slot The slot to check
     * @param itemToAdd The item to check
     * @return canDeposit
     */
    protected boolean canDeposit(int slot, ItemStack itemToAdd) {
        return true;
    }

    /**
     * Play the default success click sound
     */
    protected void playClickSuccessSound() {
        getPlayer().playSound(getPlayer().getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.3F);
    }

    /**
     * Play the click fail sound
     */
    protected void playClickFailSound() {
        getPlayer().playSound(getPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0F, 0.5F);
    }

    /**
     * Called when an item is deposited into a GUI.
     */
    protected void onDeposit(ItemStack itemToAdd, InventoryClickEvent evt, Consumer<ItemStack> consumer, Runnable remove) {

    }

    /**
     * Called when this GUI is closed.
     */
    public void onClose() {

    }

    /**
     * Add non-GUI items to the inventory
     */
    public void addStorageItems() {

    }
}
