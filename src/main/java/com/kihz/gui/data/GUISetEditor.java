package com.kihz.gui.data;

import com.kihz.item.GUIItem;
import com.kihz.utils.jsontools.containers.JsonSet;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

public class GUISetEditor<T> extends GUIJsonEditor {

    private JsonSet<T> set;

    public GUISetEditor(Player p, JsonSet<T> set) {
        super(p);
        this.set = set;
    }

    public GUISetEditor(Player p, JsonSet<T> set, Runnable onClose) {
        super(p, onClose);
        this.set = set;
    }

    @Override
    public void addFields() {
        for(T o : set) {
            addItem(o.toString(), o.getClass(), o, val -> {
                set.remove(o);
                set.add((T) val);
            });
        }

        toRight(2);
        if(set.getTypeClass() != null) {
            addItem(Material.WHITE_WOOL, ChatColor.GREEN + "Add Element", "Click here to add an element to this list.")
                    .leftClick(ce -> makeDefaultValue(set.getTypeClass(), v -> {
                        set.add(v);
                        reconstruct();
                    }));
        } else {
            addItem(Material.WHITE_WOOL, ChatColor.RED + "Can't Add", "Elements can only be added after the data is loaded.");
        }
    }

    @Override
    protected GUIItem addItem(String name, Class<?> clazz, Object value, Consumer<Object> setter) {
        GUIItem guiItem = super.addItem(name, clazz, value, setter);
        guiItem.clearListener(GUIItem.GUIClickType.RIGHT).rightClick(ce -> set.remove((T) value), "Remove Value").reconstruct().setIcon(Material.PAPER);
        return guiItem;
    }
}