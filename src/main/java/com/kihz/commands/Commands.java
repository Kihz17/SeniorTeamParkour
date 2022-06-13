package com.kihz.commands;

import com.kihz.commands.player.CommandAddPermission;
import com.kihz.commands.player.CommandParkour;
import com.kihz.mechanics.Permissions;
import com.kihz.mechanics.system.GameMechanic;
import com.kihz.utils.Utils;
import lombok.Getter;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Commands extends GameMechanic {
    @Getter private static final List<Command> commands = new ArrayList<>();

    @Override
    public void onEnable() {
        registerCommands();
    }

    /**
     * Register all commands.
     */
    private static void registerCommands() {
        addCommand(new CommandParkour());
        addCommand(new CommandAddPermission());
    }

    /**
     * Register a command.
     * @param command The command to register.
     */
    public static void addCommand(Command command) {
        getCommands().add(command);
        Utils.tryRegisterListener(command);
    }

    /**
     * Gets a list of commands by their specified type.
     * @param type - The type of command to search for, null = any.
     * @return commands
     */
    public static List<Command> getCommands(CommandType type) {
        return getCommands().stream().filter(c -> c.getCommandType() == type || type == null).collect(Collectors.toList());
    }

    /**
     * Get a list of usable commands for a given sender.
     * @param sender The sender to get the commands for
     * @return usable
     */
    public static List<Command> getUsable(CommandSender sender) {
        return getCommands().stream().filter(c -> c.isAllowed(sender, false)).collect(Collectors.toList());
    }
}
