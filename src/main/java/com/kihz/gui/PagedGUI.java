package com.kihz.gui;

import com.kihz.Core;
import com.kihz.item.GUIItem;
import com.kihz.item.ItemManager;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PagedGUI extends GUI {
    @Getter private int currentPage = 1; // The page being shown the player currently
    private int writePage; // The page currently being written to
    private final String title;
    private final List<Map<Integer, GUIItem>> pages = new ArrayList<>();
    private int maxRows;

    private final GUIItem[] customOverlay = new GUIItem[9];
    private int overlayIndex = 0;
    private boolean overlay;

    private static final int MAX_ROWS = 5;

    public PagedGUI(Player p, String title) {
        this(p, title, MAX_ROWS + 1);
        this.maxRows = MAX_ROWS;
    }

    public PagedGUI(Player p, String title, int maxRows) {
        super(p, title, Math.max(maxRows, 2));
        this.title = title;
        this.maxRows = maxRows;
    }

    @Override
    public void addItems() {
        overlay = true;

        if(currentPage > 1) {
            overlayIndex = 0;
            addItem(Material.MAP, ChatColor.GRAY + "Previous Page", "Click here to go to the previous page.")
                    .anyClick(ce -> setPage(currentPage - 1));
        }

        // Fill with glass
        for(int i = overlayIndex; i < 9; i++)
            customOverlay[overlayIndex++] = new GUIItem(ItemManager.createItem(Material.BLACK_STAINED_GLASS_PANE, ""));

        if(currentPage < maxPages()) {
            overlayIndex--;
            addItem(Material.MAP, ChatColor.GRAY + "Next Page",
                    "Click here to go to the next page.").anyClick(ce -> setPage(currentPage + 1));
        }

        overlayIndex -= 8;
        addCustomOverlay();
        setTitle(this.title + " (" + Math.max(1, currentPage) + "/" + maxPages() + ")");
        overlay = false; // Done creating overlay
    }

    @Override
    public GUIItem addItem(GUIItem item) {
        if(pages.isEmpty())
            pages.add(new HashMap<>());

        if(overlay) {
            customOverlay[overlayIndex++] = item;
            return item;
        }

        if(getSlotIndex() >= maxRows * ROW_SIZE)
            newPage();

        pages.get(writePage).put(getSlotIndex(), item);

        nextSlot();
        return item;
    }

    @Override
    public void clear() {
        super.clear();
        pages.clear();
        writePage = 0;
    }

    @Override
    protected void showItems() {
        if(!pages.isEmpty()) {
            getPage().forEach((k, v) -> getInventory().setItem(k, v.generateItem()));
        }

        // Show overlay
        int index = 0;
        for(int i = getInventory().getSize() - 9; i < getInventory().getSize(); i++) {
            GUIItem item = customOverlay[index++];
            if(item == null)
                continue;

            getInventory().setItem(i, item.generateItem());
        }
    }

    @Override
    public void reconstruct() {
        this.preventCloseHook = true;
        clear();
        addItems();

        showItems();
        addStorageItems();
        getPlayer().updateInventory();

        this.preventCloseHook = false;
    }

    @Override
    public GUIItem getItem(int slot) {
        GUIItem item = getPage().get(slot);

        // Item is null, look for it in the overlay
        int overlayStartSlot = getInventory().getSize() - 9;
        if(item == null && slot >= overlayStartSlot) {
            int customOverlayIndex = slot - overlayStartSlot;
            item = customOverlay[customOverlayIndex];
        }

        return item;
    }

    @Override
    public final boolean isAllowStorage() {
        return false;
    }

    @Override
    public void skipSlots(int slots) {
        if(overlay) {
            overlayIndex = Math.max(0, Math.min(8, overlayIndex + slots));
            return;
        }

        super.skipSlots(slots);
    }

    /**
     * Get the max pages for this GUI
     * @return maxPages
     */
    public int maxPages() {
        return pages.size();
    }

    /**
     * Get the current page
     * @return page
     */
    public Map<Integer, GUIItem> getPage() {
        return pages.get(currentPage - 1);
    }

    /**
     * change the page the player is looking at.
     * @param page The new page
     */
    protected void setPage(int page) {
        currentPage = Math.max(1, Math.min(page, maxPages()));
        reconstruct();
        getPlayer().playSound(getPlayer().getLocation(), Sound.ENTITY_BAT_TAKEOFF, 2F, 1.4F);
    }

    protected void addCustomOverlay() {

    }

    /**
     * Write items to a new page.
     */
    protected void newPage() {
        writePage++;
        pages.add(new HashMap<>());
        setSlotIndex(0);
    }
}
