package com.kihz.gui.data;

import com.kihz.gui.PagedGUI;
import com.kihz.mechanics.Callbacks;
import com.kihz.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.function.Consumer;
import java.util.stream.Stream;

public class GUIEnumSelector <T extends Enum<T>> extends PagedGUI {
    private T[] values;
    private Consumer<T> onChoose;

    public GUIEnumSelector(Player p, T[] vals, Consumer<T> onChoose) {
        super(p, "Select a Value:");
        this.values = vals;
        this.onChoose = onChoose;
    }

    @Override
    public void addItems() {
        for(T value : values) {
            String name = Utils.capitalize(value.name());
            addItem(Material.LAPIS_BLOCK, ChatColor.YELLOW + name + ": Click here to choose " + name + ".")
                    .anyClick(ce -> {
                        onChoose.accept(value);
                        openPrevious();
                    });
        }
        super.addItems();
    }

    @Override
    protected void addCustomOverlay() {
        center(1);
        addItem(Material.PAPER, ChatColor.GREEN + "Search", "", "Click here to enter a value.").leftClick(ce -> {
            ce.getPlayer().sendMessage(ChatColor.GREEN + "Enter the option you would like to choose.");
            Callbacks.listenForChat(ce.getPlayer(), message -> {
                String name = message.replaceAll(" ", "_");
                T value = Stream.of(values).filter(val -> val.name().equalsIgnoreCase(name)).findAny().orElse(null);
                if(value == null) {
                    ce.getPlayer().sendMessage(ChatColor.RED + "Unknown value.");
                    return;
                }

                onChoose.accept(value);
                openPrevious();
            });
        });
    }
}
