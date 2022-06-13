package com.kihz.gui.data;

import com.kihz.Core;
import com.kihz.gui.GUI;
import com.kihz.item.GUIItem;
import com.kihz.utils.GeneralException;
import com.kihz.utils.Utils;
import com.kihz.utils.jsontools.JsonSerializer;
import com.kihz.utils.jsontools.Jsonable;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Getter
public class GUIJsonEditor extends GUI {
    private Jsonable data;
    private BiConsumer<Player, Jsonable> onFinish;
    private Runnable onClose;

    public GUIJsonEditor(Player p) {
        super(p, "JSON Editor");
    }

    public GUIJsonEditor(Player p, Runnable onClose) {
        this(p);
        this.onClose = onClose;
    }

    public GUIJsonEditor(Player p, Jsonable data) {
        super(p, "JSON Editor");
        this.data = data;
        this.onFinish = null;
        this.onClose = null;
    }

    public GUIJsonEditor(Player p, Jsonable data, BiConsumer<Player, Jsonable> onFinish) {
        this(p, data, onFinish, null);
    }


    public GUIJsonEditor(Player p, Jsonable data, BiConsumer<Player, Jsonable> onFinish, Runnable onClose) {
        this(p);
        this.data = data;
        this.onFinish = onFinish;
        this.onClose = onClose;
    }

    @Override
    public void addItems() {
        addFields();
        addBackButton();
    }

    @Override
    public void onClose() {
        if(onClose != null)
            onClose.run();
    }

    protected void addFields() {
        JsonSerializer.getFields(getData()).forEach(f -> {
            Object obj = null;
            try {
                obj = f.get(getData());
            } catch (Exception e) {
                e.printStackTrace();
                Core.logInfo("Failed to get field %s in JSON GUI", f.getName());
            }

            Consumer<Object> setter = val -> {
                try {
                    f.set(getData(), val);
                    Core.logInfo("%s updated field %s to '%s'.", getPlayer().getName(), f.getName(), val);
                } catch (Exception e) {
                    throw new GeneralException(e, "Failed to set " + f.getName() + " to " + val + ".");
                }
            };
            addItem(Utils.capitalize(f.getName()), f.getType(), obj, setter);
        });
    }

    /**
     * Add a JSON GUI item to this GUI
     * @param name The name of the item
     * @param type The type class of the item
     * @param value The value of the item
     * @param setter The setter to change the value
     * @return item
     */
    protected GUIItem addItem(String name, Class<?> type, Object value, Consumer<Object> setter) {
        GUIItem guiItem = addItem(Material.WHITE_WOOL, ChatColor.YELLOW + name);

        // Display current value
        String s = value != null ? value.toString() : "null";
        guiItem.addLore("Value: " + ChatColor.YELLOW + Utils.cutString(s, 50), "");

        JsonSerializer.getHandler(type).editItem(guiItem, value, setter, type);

        if(guiItem.getItem().getType() == Material.WHITE_WOOL) {
            boolean green = value != null && (!(value instanceof Boolean) || ((Boolean) value));
            guiItem.setIcon(green ? Material.LIME_WOOL : Material.RED_WOOL);
        }

        if(!guiItem.hasListener(GUIItem.GUIClickType.LEFT) && value == null)
            guiItem.leftClick(ce -> makeDefaultValue(type, setter::accept));

        guiItem.anyClick(ce -> { // Update the changes when value is changed
            if (ce.getGUI() == this)
                reconstruct();
        });

        return guiItem;
    }

    /**
     * Makes a value from a given class
     * @param clazz The class of the value
     * @param callback Logic to run when we have the object
     */
    protected <T> void makeDefaultValue(Class<T> clazz, Consumer<T> callback) {
        JsonSerializer.makeDefaultValue(getPlayer(), clazz, callback);
    }

}
