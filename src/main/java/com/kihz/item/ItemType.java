package com.kihz.item;

import com.kihz.utils.Utils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

@AllArgsConstructor @Getter
public enum ItemType {

    ;

    private final Function<ItemStack, ItemWrapper> loadMaker; // Used for loading an item from an existing ItemStack
    private final Supplier<ItemWrapper> simpleMaker; // Used only if we have a constructor with no args to provide us with the "base" item

    ItemType(Function<ItemStack, ItemWrapper> loadMaker) {
        this(loadMaker, null);
    }

    /**
     * Can we construct this item without any arguments?
     * @return isSimple
     */
    public boolean isSimple() {
        return simpleMaker != null;
    }

    /**
     * Try to create this item. If it isn't possible we will return null,
     * @return itemWrapper
     */
    @SuppressWarnings("unchecked")
    public <T extends ItemWrapper> T makeSimple() {
        Utils.verifyNotNull(simpleMaker, name() + " has no simple loader.");
        return (T) simpleMaker.get();
    }
}
