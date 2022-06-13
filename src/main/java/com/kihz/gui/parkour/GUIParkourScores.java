package com.kihz.gui.parkour;

import com.kihz.gui.PagedGUI;
import com.kihz.mechanics.parkour.Parkours;
import com.kihz.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

public class GUIParkourScores extends PagedGUI {
    private final List<Parkours.ParkourScore> scores;

    public GUIParkourScores(Player p, String title, List<Parkours.ParkourScore> scores) {
        super(p, title + " Scores");
        this.scores = scores;
    }

    @Override
    public void addItems() {
        for(Parkours.ParkourScore score : scores) {
            addItem(Material.LIME_WOOL, ChatColor.GREEN + "Score: "+ ChatColor.GRAY + Utils.DECIMAL_FORMAT.format(score.getScore()),
                    ChatColor.GREEN + "Date: " + ChatColor.GRAY + score.getDate());
        }

        super.addItems();
    }
}
