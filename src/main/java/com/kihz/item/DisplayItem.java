package com.kihz.item;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

@Getter
public class DisplayItem extends ItemWrapper {
    private ItemWrapper itemWrapper;

    public DisplayItem(ItemStack item) {
        this(new VanillaItem(item), item);
    }

    public DisplayItem(ItemWrapper iw, ItemStack itemStack) {
        super(itemStack.clone());
        this.needsRegen = false;
        this.itemWrapper = iw;
    }

    @Override
    public ItemStack getRawStack() {
        return getItem();
    }

    @Override
    public void updateItem() {

    }

    @Override // Prevents duplicate lines
    public ItemWrapper addLore(String... lore) {
        List<Component> lines = getLore();
        for (String line : lore) {
            Component lineToAdd = Component.text(line);

            for(int i = 0; i < lines.size(); i++)
                if(lineToAdd.equals(lines.get(i)))
                    lines.remove(i--);
        }

        getMeta().lore(lines);
        return super.addLore(lore);
    }

    /**
     * Set the display name of this item
     * @param name The name to set
     * @return this
     */
    public DisplayItem setDisplayName(String name) {
        getMeta().displayName(Component.text(name, NamedTextColor.WHITE));
        return this;
    }

    /**
     * Set the icon for this item.
     * @param icon The icon to set
     * @return this
     */
    public DisplayItem setIcon(Material icon) {
        getItem().setType(icon);
        return this;
    }

    /**
     * Flag this item as unbreakable
     * @return this
     */
    public DisplayItem setUnbreakable() {
        getMeta().setUnbreakable(true);
        return this;
    }

    /**
     * Generate a plain black glass pane
     * @return item
     */
    public static ItemStack generateNamelessItem(Material material) {
        return new DisplayItem(new ItemStack(material)).setDisplayName("").generateItem();
    }
}
