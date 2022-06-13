package com.kihz.gui.parkour;

import com.kihz.gui.GUI;
import com.kihz.mechanics.parkour.Parkours;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public class GUIParkourStats extends GUI {
    private final Map<String, List<Parkours.ParkourScore>> stats;

    public GUIParkourStats(Player player, Map<String, List<Parkours.ParkourScore>> stats) {
        super(player, "Parkour Stats");
        this.stats = stats;
    }

    @Override
    public void addItems() {
        for(String parkour : stats.keySet()) {
            addItem(Material.PAPER, ChatColor.GREEN + "Parkour: " + parkour,
                    ChatColor.GRAY + "Click here to view scores").anyClick(ce -> {
                new GUIParkourScores(ce.getPlayer(), parkour, stats.get(parkour));
            });
        }
    }
}
