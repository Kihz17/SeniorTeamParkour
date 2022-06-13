package com.kihz.commands.player;

import com.kihz.Core;
import com.kihz.gui.parkour.GUIEditParkour;
import com.kihz.gui.parkour.GUIParkourStats;
import com.kihz.gui.parkour.GUIParkourTop;
import com.kihz.mechanics.Permissions;
import com.kihz.mechanics.parkour.Parkour;
import com.kihz.mechanics.parkour.Parkours;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CommandParkour extends PlayerCommand {
    public static final String CREATE_PERM = "parkour.admin.create";
    public static final String CHECKPOINT_PERM = "parkour.admin.checkpoint";
    public static final String END_PERM = "parkour.admin.end";
    public static final String DELETE_PERM = "parkour.admin.delete";
    public static final String TELEPORT_PERM = "parkour.admin.teleport";
    public static final String INFO_PERM = "parkour.admin.info";
    public static final String TOP_PERM = "parkour.top";
    public static final String STATS_PERM = "parkour.stats";

    public CommandParkour() {
        super("<action> <name>", "Parkour command.", null, "parkour");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        Player p = (Player) sender;
        if(args[0].equals("create") && Permissions.hasPermission(p, CREATE_PERM, true)) {
            createParkour(sender, p.getLocation(), args[1]);
        }

        else if(args[0].equals("end") && Permissions.hasPermission(p, END_PERM, true)) {
            endParkour(sender, args[1]);
        }

        else if(args[0].equals("checkpoint") && Permissions.hasPermission(p, CHECKPOINT_PERM, true)) {
            addCheckpoint(sender, args[1], p.getLocation());
        }

        else if(args[0].equals("delete") && Permissions.hasPermission(p, DELETE_PERM, true)) {
            deleteParkour(sender, args[1], args.length >= 3 ? Integer.parseInt(args[2]) - 1 : -1);
        }

        else if(args[0].equals("teleport") && Permissions.hasPermission(p, TELEPORT_PERM, true)) {
            teleportToCheckpoint(p, args[1], args.length >= 3 ? Integer.parseInt(args[2]) - 1 : -1);
        }

        else if(args[0].equals("top") && Permissions.hasPermission(p, TOP_PERM, true)) {
            List<Parkours.ParkourScore> scores = Parkours.getParkourTop(args[1]);
            if(scores == null || scores.isEmpty()) {
                sender.sendMessage(ChatColor.RED + "There are no scores available for parkour '" + args[1] + "'.");
                return;
            }

            new GUIParkourTop(p, scores);
        }

        else if(args[0].equals("info") && Permissions.hasPermission(p, INFO_PERM, true)) {
            Parkour parkour = Parkours.getParkourByName(args[1]);
            if(parkour == null)
                return;

            new GUIEditParkour(p, parkour);
        }

        else if(args[0].equals("stats") && Permissions.hasPermission(p, STATS_PERM, true)) {
            Player player = Bukkit.getPlayer(args[1]);
            if(player == null) {
                sender.sendMessage(ChatColor.RED + "Player '" + args[1] + "' does not exist!");
                return;
            }

            // Get scores indexed by parkour name, with a sorted score list
            Map<String, List<Parkours.ParkourScore>> scores = Parkours.getParkourStats(player);
            if(scores == null || scores.isEmpty()) {
                sender.sendMessage(ChatColor.RED + "This player has no scores!");
                return;
            }

            new GUIParkourStats(p, scores);
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(args.length == 1) {
            return Arrays.asList("create", "end", "checkpoint", "delete", "teleport", "top", "info", "stats");
        }

        if(args.length == 2) {
            String action = args[0];

            if(action.equals("checkpoint") || action.equals("end") || action.equals("delete")
                    || action.equals("teleport") || action.equals("top") || action.equals("info")) {
                return Parkours.getParkourNames();
            }

            if(action.equals("stats"))
                return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
        }

        if(args.length == 3) {
            String action = args[0];
            if(action.equals("delete") || action.equals("teleport")) {
                String parkourName = args[1];
                Parkour parkour = Parkours.getParkourByName(parkourName);

                if(parkour != null) {
                    List<String> checkpoints = new ArrayList<>();
                    for(int i = 0; i < parkour.getCheckpoints().size(); i++)
                        checkpoints.add(Integer.toString(i + 1));

                    return checkpoints;
                }
            }
        }

        return null;
    }

    /**
     * Create a new parkour
     * @param sender The sender of the command
     * @param location The location of the parkour
     * @param name The name of the parkour
     */
    private void createParkour(CommandSender sender, Location location, String name) {
        if(Parkours.containsParkour(name)) {
            sender.sendMessage(ChatColor.RED + "Parkour with name '" + name + "' already exists!");
            return;
        }

        Parkour parkour = new Parkour(name, location);
        Parkours.addParkour(parkour);
        sender.sendMessage(ChatColor.GREEN + "Parkour '" + name + "' added!");
    }

    /**
     * Place the end for a given parkour
     * @param sender The sender of the command
     * @param name The name of the parkour
     */
    private void endParkour(CommandSender sender, String name) {
        if(!Parkours.containsParkour(name)) {
            sender.sendMessage(ChatColor.RED + "Parkour '" + name + "' does not exist!");
            return;
        }

        Player p = (Player) sender;
        Parkour parkour = Parkours.getParkourByName(name);
        Location loc = p.getLocation();
        if(parkour.getEndNode() != null) { // End already exists, move it
            parkour.getEndNode().setLocation(loc);
            parkour.getEndNode().getHologram().setLocation(loc);
            parkour.getEndNode().getHologram().refresh();
            sender.sendMessage(ChatColor.GREEN + "End updated!");
            return;
        }

        parkour.setEndNode(new Parkour.ParkourNode(parkour.getName(), p.getLocation(), Parkour.NodeType.END,
                ChatColor.GREEN + "Parkour '" + parkour.getName() + "' Finished",
                ChatColor.GREEN + "Parkour '" + parkour.getName() + "' End"));

        sender.sendMessage(ChatColor.GREEN + "End added!");
    }

    /**
     * Delete a parkour
     * @param sender The sender of the command
     * @param name The name of the parkour
     * @param checkpointToRemove The index of the checkpoint to remove
     */
    private void deleteParkour(CommandSender sender, String name, int checkpointToRemove) {
        if(!Parkours.containsParkour(name)) {
            sender.sendMessage(ChatColor.RED + "Parkour '" + name + "' does not exist!");
            return;
        }

        Parkour parkour = Parkours.getParkourByName(name);
        if(checkpointToRemove > -1) { // We should delete a checkpoint
            parkour.removeCheckpoint(checkpointToRemove);
            sender.sendMessage(ChatColor.GREEN + "Checkpoint deleted!");
            return;
        }

        // We are not deleting a checkpoint, we are deleting the entire parkour
        Parkours.deleteParkour(parkour);
        sender.sendMessage(ChatColor.GREEN + "Parkour '" + parkour.getName() + "' deleted!");
    }

    /**
     * Adda checkpoint to a parkour
     * @param sender The sender of the command
     * @param name The name of the parkour
     */
    private void addCheckpoint(CommandSender sender, String name, Location location) {
        if(!Parkours.containsParkour(name)) {
            sender.sendMessage(ChatColor.RED + "Parkour '" + name + "' does not exist!");
            return;
        }

        Parkour parkour = Parkours.getParkourByName(name);
        parkour.addCheckpoint(location);
        sender.sendMessage(ChatColor.GREEN + "Checkpoint added!");
    }

    /**
     * Teleport a player to the checkpoint in a parkour
     * @param sender The sender of the command
     * @param name The name of the parkour
     * @param checkpoint The checkpoint
     */
    private void teleportToCheckpoint(Player sender, String name, int checkpoint) {
        if(!Parkours.containsParkour(name)) {
            sender.sendMessage(ChatColor.RED + "Parkour '" + name + "' does not exist!");
            return;
        }

        Parkour parkour = Parkours.getParkourByName(name);
        if(checkpoint > -1) { // We should delete a checkpoint
            parkour.movePlayerToCheckpoint(sender, checkpoint);
            return;
        }

        parkour.movePlayerToStart(sender);
    }
}
