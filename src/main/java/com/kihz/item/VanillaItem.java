package com.kihz.item;

import org.bukkit.inventory.ItemStack;

public class VanillaItem extends ItemWrapper {
    public VanillaItem(ItemStack item) {
        super(item);
        this.needsRegen = false; // Since we are a vanilla item, there is no need to reset lore.
    }

    @Override
    public ItemStack getRawStack() {
        return getItem();
    }

    @Override
    public void updateItem() {

    }
}
