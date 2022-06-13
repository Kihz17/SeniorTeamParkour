package com.kihz.mechanics.holograms;

import com.kihz.gui.GUI;
import com.kihz.mechanics.Callbacks;
import com.kihz.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GUIEditHologram extends GUI {
    private Hologram hologram;

    public GUIEditHologram(Player p, Hologram hologram) {
        super(p, "Edit Hologram");
        this.hologram = hologram;
    }

    @Override
    public void addItems() {
        for(int i = 0; i < hologram.getLines().size(); i++) {
            final int index = i;
            HologramLine line = hologram.getLines().get(i);

            Material itemType = Material.STRING;
            addItem(itemType, ChatColor.GREEN + "Line Type: " + line.getGenericName(), "Value: " + line.getValueAsString(), "Current Height: " +
                    Utils.DECIMAL_FORMAT.format(line.getHeight())).leftClick(ce -> {

                Player p = ce.getPlayer();
                p.sendMessage(ChatColor.GREEN + "Please enter the new value:");

                Callbacks.listenForChat(ce.getPlayer(), message -> {
                    Holograms.editTextLine(hologram, index, Utils.applyColor(message));
                });
                reconstruct();
            }).rightClick(ce -> {
                Player p = ce.getPlayer();
                if (ce.getEvent().isShiftClick()) {
                    p.sendMessage(ChatColor.GREEN + "Please enter the new height value (-5 - 5):");
                    Callbacks.listenForChat(p, message -> {
                        try {
                            line.setHeight(Double.parseDouble(message));
                            hologram.refresh();
                        } catch (Exception e) {
                            p.sendMessage(ChatColor.RED + "You message must be a number!");
                        }
                    });
                    reconstruct();
                    return;
                }

                Holograms.removeLine(hologram, ce.getEvent().getSlot());
                p.sendMessage(ChatColor.GREEN + "Line removed successfully!");
                reconstruct();
            }).addLore("", ChatColor.GRAY + "Left-click to edit", ChatColor.GRAY + "Right-click to remove", ChatColor.GRAY + "Shift right-click to edit line height");
        }

        toRight(2);
        addItem(Material.NAME_TAG, ChatColor.GREEN + "Add Text Hologram").anyClick(ce -> {
            Player p = ce.getPlayer();
            p.sendMessage(ChatColor.GREEN + "Please enter the message for the hologram.");
            Callbacks.listenForChat(p, message -> {
                Holograms.addTextLine(hologram, Utils.applyColor(message));
                p.sendMessage(ChatColor.GREEN + "Hologram added successfully!");
            });
        });
    }
}
