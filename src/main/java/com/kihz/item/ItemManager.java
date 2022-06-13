package com.kihz.item;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ItemManager {
    public static final String ITEM_NBT_TAG = "tag";

    /**
     * Constructs an item into its correct type from a bukkit ItemStack.
     * @param item The item to load
     * @return item
     */
    @SuppressWarnings("unchecked")
    public static <T extends ItemWrapper> T constructItem(ItemStack item) {
        ItemType type = ItemWrapper.getType(item);
        return (T) (type != null ? type.getLoadMaker().apply(item) : new VanillaItem(item));
    }

    /**
     * Build an item stacj,
     * @param material The type of the item
     * @param meta The meta to set
     * @return item
     */
    public static ItemStack createItem(Material material, ItemMeta meta) {
        ItemStack item = new ItemStack(material);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Build an item stack,
     * @param material The type of the item
     * @param name The name of the item
     * @param lore The lore of the item
     * @return item
     */
    public static ItemStack createItem(Material material, String name, String... lore) {
        return createItem(material, name, false, lore);
    }

    /**
     * Build an item stacj,
     * @param material The type of the item
     * @param name The name of the item
     * @param lore The lore of the item
     * @return item
     */
    public static ItemStack createItem(Material material, String name, boolean enchanted, String... lore) {
        ItemStack itemStack = new ItemStack(material, 1);
        ItemMeta meta = itemStack.getItemMeta();
        if(enchanted)
            meta.addEnchant(ItemWrapper.ENCHANT, 1, true);
        for(ItemFlag flag : ItemFlag.values())
            meta.addItemFlags(flag);
        if (name != null)
            meta.displayName(Component.text(name, NamedTextColor.WHITE));
        List<Component> loreList = new ArrayList<>();
        for (String loreLine : lore)
            loreList.add(Component.text(loreLine, NamedTextColor.GRAY));
        meta.lore(loreList);
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    /**
     * Build an item stack,
     * @param item The item
     * @param name The name of the item
     * @param lore The lore of the item
     * @return item
     */
    public static ItemStack createItem(ItemStack item, String name, String... lore) {
        return createItem(item, name, false, lore);
    }

    /**
     * Build an item stack,
     * @param item The item
     * @param name The name of the item
     * @param lore The lore of the item
     * @return item
     */
    public static ItemStack createItem(ItemStack item, String name, boolean enchanted, String... lore) {
        ItemStack itemStack = item.clone();
        ItemMeta meta = itemStack.getItemMeta();
        if(enchanted)
            meta.addEnchant(ItemWrapper.ENCHANT, 1, true);

        for(ItemFlag flag : ItemFlag.values())
            meta.addItemFlags(flag);

        if (name != null)
            meta.displayName(Component.text(name, NamedTextColor.WHITE));

        List<Component> loreList = new ArrayList<>();
        for (String loreLine : lore)
            loreList.add(Component.text(loreLine, NamedTextColor.GRAY));

        meta.lore(loreList);
        itemStack.setItemMeta(meta);

        return itemStack;
    }

    /**
     * Creates a skull for a given player.
     * @param uuid The player we want
     * @return skull
     */
    public static ItemStack makeSkull(UUID uuid, String name, String... lore) {
        ItemStack skull = createItem(Material.PLAYER_HEAD, name, lore);
        ItemMeta meta = skull.getItemMeta();
        ((SkullMeta)meta).setOwningPlayer(Bukkit.getOfflinePlayer(uuid));
        skull.setItemMeta(meta);
        return skull;
    }
}
