package com.kihz.gui.data;

import com.kihz.item.GUIItem;
import com.kihz.utils.jsontools.containers.JsonList;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

public class GUIListEditor<T> extends GUIJsonEditor {
    private JsonList<T> list;

    public GUIListEditor(Player p, JsonList<T> list) {
        super(p);
        this.list = list;
    }

    public GUIListEditor(Player p, JsonList<T> list, Runnable onClose) {
        super(p, onClose);
        this.list = list;
    }

    @Override
    public void addFields() {
        for (int i = 0; i < list.size(); i++) {
            final int index = i;
            Object o = list.get(i);
            addItem("Element " + i, o.getClass(), o, val -> list.set(index, (T) val));
        }

        toRight(2);
        if (list.getTypeClass() != null) {
            addItem(Material.WHITE_WOOL, ChatColor.GREEN + "Add Element", "Click here to add an element to this list.")
                    .leftClick(ce -> makeDefaultValue(list.getTypeClass(), v -> {
                        list.add(v);
                        reconstruct();
                    }));
        } else {
            addItem(Material.WHITE_WOOL, ChatColor.RED + "Can't Add", "Elements can only be added after the data is loaded.");
        }
    }

    @Override
    protected GUIItem addItem(String name, Class<?> clazz, Object value, Consumer<Object> setter) {
        GUIItem guiItem = super.addItem(name, clazz, value, setter);
        guiItem.clearListener(GUIItem.GUIClickType.RIGHT).rightClick(ce -> list.remove((T) value), "Remove Value").reconstruct().setIcon(Material.PAPER);
        return guiItem;
    }
}

