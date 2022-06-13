package com.kihz.gui.parkour;

import com.kihz.gui.PagedGUI;
import com.kihz.item.ItemManager;
import com.kihz.mechanics.parkour.Parkours;
import com.kihz.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class GUIParkourTop extends PagedGUI {
    private List<Parkours.ParkourScore> scores;

    public GUIParkourTop(Player p, List<Parkours.ParkourScore> scores) {
        super(p, "Parkour Scores", 1);
        this.scores = scores;
    }

    @Override
    public void addItems() {
        for(int i = 0; i < scores.size(); i++) {
            Parkours.ParkourScore score = scores.get(i);

            String playerName = Bukkit.getOfflinePlayer(score.getPlayerId()).getName();

            ItemStack playerHead = ItemManager.makeSkull(score.getPlayerId(),
                    ChatColor.GREEN + "#" + (i + 1) + " " + playerName,
                    ChatColor.GREEN + "Score: " + ChatColor.GRAY + Utils.DECIMAL_FORMAT.format(score.getScore()),
                    ChatColor.GREEN + "Date: " + ChatColor.GRAY + score.getDate());

            addItem(playerHead);
        }

        super.addItems();
    }
}
