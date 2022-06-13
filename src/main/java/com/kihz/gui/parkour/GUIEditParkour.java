package com.kihz.gui.parkour;

import com.kihz.gui.GUI;
import com.kihz.mechanics.parkour.Parkour;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class GUIEditParkour extends GUI {
    private Parkour parkour;

    public GUIEditParkour(Player player, Parkour parkour) {
        super(player, parkour.getName());
        this.parkour = parkour;
    }

    @Override
    public void addItems() {
        if(parkour.getStartNode() != null) {
            addItem(Material.LIME_WOOL, ChatColor.GREEN + "Start Node",
                    ChatColor.GRAY + "Left-Click to edit",
                    ChatColor.GRAY + "Right-Click to teleport to this node").leftClick(ce -> {
                        new GUIEditParkourNode(ce.getPlayer(), parkour.getStartNode());
            }).rightClick(ce -> {
                ce.getPlayer().teleport(parkour.getStartNode().getLocation());
            });
        }

        if(parkour.getEndNode() != null) {
            addItem(Material.RED_WOOL, ChatColor.GREEN + "End Node",
                    ChatColor.GRAY + "Left-Click to edit",
                    ChatColor.GRAY + "Right-Click to teleport to this node").leftClick(ce -> {
                new GUIEditParkourNode(ce.getPlayer(), parkour.getEndNode());
            }).rightClick(ce -> {
                ce.getPlayer().teleport(parkour.getEndNode().getLocation());
            });
        }

        int i = 1;
        for(Parkour.ParkourNode node : parkour.getCheckpoints()) {
            addItem(Material.GLOWSTONE_DUST, ChatColor.GREEN + "Checkpoint #" + i,
                    ChatColor.GRAY + "Left-Click to edit",
                    ChatColor.GRAY + "Right-Click to teleport to this node").leftClick(ce -> {
                new GUIEditParkourNode(ce.getPlayer(), node);
            }).rightClick(ce -> {
                ce.getPlayer().teleport(node.getLocation());
            });

            i++;
        }
    }
}
