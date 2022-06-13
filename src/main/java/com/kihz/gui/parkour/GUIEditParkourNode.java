package com.kihz.gui.parkour;

import com.kihz.gui.GUI;
import com.kihz.mechanics.Callbacks;
import com.kihz.mechanics.holograms.HologramLine;
import com.kihz.mechanics.holograms.HologramTextLine;
import com.kihz.mechanics.parkour.Parkour;
import com.kihz.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class GUIEditParkourNode extends GUI {
    private Parkour.ParkourNode node;

    public GUIEditParkourNode(Player player, Parkour.ParkourNode node) {
        super(player, "Edit Node");
        this.node = node;
    }

    @Override
    public void addItems() {
        addItem(Material.STRING, ChatColor.GREEN + "Interaction Message",
                ChatColor.GRAY + "Value: " + node.getInteractMessage(),
                ChatColor.GRAY + "Click here to edit.").anyClick(ce -> {
                    ce.getPlayer().sendMessage(ChatColor.GREEN + "Enter new message:");
            Callbacks.listenForChat(ce.getPlayer(), message -> node.setInteractMessage(Utils.applyColor(message)));
        });

        for(int i = 0; i < node.getDisplayLines().size(); i++) {
            final int index = i;
            addItem(Material.NAME_TAG, ChatColor.GREEN + "Display Line #" + (i + 1),
                    ChatColor.GRAY + "Value: " + node.getDisplayLines().get(i),
                    ChatColor.GRAY + "Height: " + node.getHologram().getLines().get(i).getHeight(),
                    ChatColor.GRAY + "Left-Click to edit.",
                    ChatColor.GRAY + "Right-Click to change height").leftClick(ce -> {
                ce.getPlayer().sendMessage(ChatColor.GREEN + "Enter new display:");
                Callbacks.listenForChat(ce.getPlayer(), message -> {
                    String colorParsedMessage = Utils.applyColor(message);
                    node.getDisplayLines().set(index, colorParsedMessage);

                    // Update hologram line
                    HologramTextLine line = node.getHologram().getLines().get(index).getAsTextLine();
                    line.setText(colorParsedMessage);

                    // Refresh the hologram
                    node.getHologram().refresh();

                    ce.getPlayer().sendMessage(ChatColor.GREEN + "Line updated!");
                });
            }).rightClick(ce -> {
                ce.getPlayer().sendMessage(ChatColor.GREEN + "Enter new height (decimal number):");
               Callbacks.listenForChat(ce.getPlayer(), message -> {
                   node.getHologram().getLines().get(index).setHeight(Double.parseDouble(message));
                   node.getHologram().refresh();
                   ce.getPlayer().sendMessage(ChatColor.GREEN + "Height updated!");
               });
            });

            i++;
        }
    }
}
