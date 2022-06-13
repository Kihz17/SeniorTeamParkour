package com.kihz.commands.player;

import com.kihz.commands.Command;
import com.kihz.commands.CommandType;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

@Getter
public abstract class PlayerCommand extends Command {

    public PlayerCommand(String usage, String help, String permission, String... alias) {
        super(CommandType.IN_GAME, usage, help, permission, alias);
    }

    @Override
    public boolean isAllowed(CommandSender sender, boolean showMessage) {
        if(getPermission() == null)
            return true; // This command has no permissions assigned to it

        boolean allowed = sender.hasPermission(getPermission());
        if(!allowed)
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");

        return allowed;
    }

}
