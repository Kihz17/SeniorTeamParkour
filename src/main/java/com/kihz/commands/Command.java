package com.kihz.commands;

import com.google.common.collect.Lists;
import com.kihz.Constants;
import com.kihz.Core;
import com.kihz.utils.Utils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Getter
public abstract class Command extends org.bukkit.command.Command implements TabCompleter {
    private final CommandType commandType;
    private final String usage;
    private final String helpMessage;

    @Setter private String lastAlias; // Used to get the last alias we used
    @Setter private String[] lastArgs;

    private static final List<String> SENDERS = Arrays.asList("CommandSender", "Player", "ConsoleCommandSender", "TerminalConsoleCommandSender");

    public Command(CommandType type, String usage, String help, String permission, String... alias) {
        super(alias[0]);
        setAliases(Arrays.stream(alias).toList());

        if(permission != null)
            setPermission(permission);

        this.commandType = type;
        this.usage = usage;
        this.helpMessage = help;

        Bukkit.getCommandMap().register(Constants.PLUGIN_NAME.toLowerCase(), this);
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if(args.length < getMinArgs()) { // Not enough arguments
            sender.sendMessage(ChatColor.RED + getUsageMessage());
            return false;
        }

        // Try to execute while catching exceptions
        try {
            if(!isAllowed(sender, true))
                return false;

            onCommand(sender, args);
            return true;
        } catch (NumberFormatException nfe) {
            sender.sendMessage(ChatColor.RED + "Invalid number '" + Utils.getInput(nfe) + "'.");
            return false;
        } catch (IllegalArgumentException iae) {
            Matcher mClassPath = Pattern.compile("No enum constant (.+)").matcher(iae.getLocalizedMessage());

            if (!mClassPath.find())
                throw iae;

            String classPath = mClassPath.group(1);
            String[] split = classPath.split("\\.");

            String input = split[split.length - 1];
            for (String s : args)
                if (s.equalsIgnoreCase(input))
                    input = s; //Many enums use toUpperCase(), we'd rather display the input the user put in.

            sender.sendMessage(ChatColor.RED + input + " is not a valid " + split[split.length - 2] + ".");
            return false;
        } catch (ClassCastException cce) {
            Matcher mCast = Pattern.compile("(.+) cannot be cast to (.+)").matcher(cce.getLocalizedMessage());
            if (!mCast.find())
                throw cce;

            String castFrom = mCast.group(1).split(";")[0];
            String castTo = mCast.group(2).split(";")[0];
            castTo = castTo.substring(castTo.lastIndexOf(".") + 1); // Remove the path.

            if (SENDERS.stream().anyMatch(castFrom::endsWith)) { // Only handle if the class casted was the command executor.
                sender.sendMessage(ChatColor.RED + "You must be a " + castTo.toLowerCase() + " to run this command.");
            } else {
                throw cce;
            }

            return false;
        }
    }

    @Override
    public java.util.List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws CommandException, IllegalArgumentException {
        List<String> completions;

        try {
            completions = onTabComplete(sender, this, alias, args);
        } catch (Throwable ex) {
            StringBuilder message = new StringBuilder();
            message.append("Unhandled exception during tab completion for command '/").append(alias).append(' ');
            for (String arg : args)
                message.append(arg).append(' ');

            message.deleteCharAt(message.length() - 1).append("' in plugin ").append(Core.getInstance().getDescription().getFullName());
            throw new CommandException(message.toString(), ex);
        }

        if (completions == null)
            return super.tabComplete(sender, alias, args);

        return completions;
    }

        /**
         * Is the sender allowed to use this command?
         * @param sender The CommandSender to check
         * @param showMessage Do we show a failure message?
         * @return
         */
    public abstract boolean isAllowed(CommandSender sender, boolean showMessage);

    /**
     * This method will run the command logic specified by the command.
     * @param sender The sender of the command.
     * @param args The passes arguments.
     */
    public abstract void onCommand(CommandSender sender, String[] args);

    /**
     * Send a formatted usage message to the CommandSender.
     * @param sender The sender to send the message to.
     */
    protected void showUsage(CommandSender sender) {
        sender.sendMessage(ChatColor.RED + getUsageMessage());
    }

    /**
     * Return a formatted usage message. (used for when a command is typed in wrong)
     *
     * @return usageMessage
     */
    public String getUsageMessage() {
        return "Usage: " + getCommandPrefix() + getLastAlias() + " " + getUsage();
    }

    /**
     * Return the prefix of this command (usually a '/')
     *
     * @return comandPrefix
     */
    public String getCommandPrefix() {
        return getCommandType().getPrefix();
    }

    /**
     * Returns the minimum amount of arguments required for a usage string.
     * @return minimumArguments
     */
    public int getMinArgs() {
        return (int) Arrays.stream(getUsage().split(" ")).filter(s -> s.startsWith("<") && s.endsWith(">")).count();
    }

    /**
     * Returns the alias that should be shown to the player.
     * @return lastAlias
     */
    protected String getLastAlias() {
        return lastAlias != null ? lastAlias : getName();
    }
}
