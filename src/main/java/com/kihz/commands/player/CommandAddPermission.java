package com.kihz.commands.player;

import com.kihz.mechanics.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

public class CommandAddPermission extends PlayerCommand {

    public CommandAddPermission() {
        super("<name> <permission>", "Give a permission to a player", null, "addperm");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if(!sender.isOp()) {
            sender.sendMessage(ChatColor.RED + "You must be opped to use this!");
            return;
        }

        Player p = Bukkit.getPlayer(args[0]);
        if(p == null)
            return;

        Permissions.addEntry(p, args[1]);
        sender.sendMessage(ChatColor.GREEN + "Permission '" + args[1] + "' added to player " + args[0] + "!");
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(args.length == 1)
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());

        if(args.length == 2)
            return Permissions.getPermissions();

        return null;
    }
}
