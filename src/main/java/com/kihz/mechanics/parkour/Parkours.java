package com.kihz.mechanics.parkour;

import com.kihz.Constants;
import com.kihz.Core;
import com.kihz.mechanics.holograms.HologramLine;
import com.kihz.mechanics.system.GameMechanic;
import com.kihz.utils.jsontools.JsonSerializer;
import com.kihz.utils.jsontools.Jsonable;
import com.mojang.datafixers.kinds.Const;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;

public class Parkours extends GameMechanic {
    private static final Map<String, Parkour> parkours = new HashMap<>();
    private static final Map<Player, Parkour> activeParkours = new HashMap<>();

    private static Connection SQL_CONNECTION = null;
    private static final String SQL_URL = "jdbc:mysql://localhost:3306/parkourscores";
    private static final String SQL_USERNAME = "testuser";
    private static final String SQL_PASSWORD = "password";

    private static final File PARKOUR_FOLDER = Core.makeFolder("parkours");

    @Override
    public void onEnable() {
        Bukkit.getScheduler().runTaskTimer(Core.getInstance(), () -> {
            try {
                if(SQL_CONNECTION == null || SQL_CONNECTION.isClosed()) {
                    Core.logInfo("Attempting to connect to SQL server...");
                    SQL_CONNECTION = DriverManager.getConnection(SQL_URL, SQL_USERNAME, SQL_PASSWORD);
                    Core.logInfo("Connected to SQL server!");
                }
            } catch (Exception e) {
                Core.logInfo("Failed to establish a connection with SQL server!");
                e.printStackTrace();
            }
        }, 0L, Constants.TPS * 5);


        // Load parkours
        File[] parkourFiles = PARKOUR_FOLDER.listFiles();
        if(parkourFiles != null) {
            Arrays.stream(parkourFiles)
                    .filter(file -> file.getName().endsWith(".json"))
                    .forEach(file -> {
                        Parkour parkour = JsonSerializer.deserializeFromFile(Parkour.class, file);
                        parkours.put(parkour.getName(), parkour);
                    });
        }
    }

    @Override
    public void onDisable() {
        // Save parkours
        parkours.values().forEach(parkour -> parkour.saveToFile(new File(PARKOUR_FOLDER, parkour.getName() +  ".json")));
    }

    @Override
    public void onQuit(Player p) {
        Parkour parkour = activeParkours.remove(p);
        if(parkour != null)
            parkour.removePlayer(p);
    }

    @EventHandler
    public void onPlayerFly(PlayerMoveEvent evt) {
        if(!hasActiveParkour(evt.getPlayer()))
            return; // Not in a parkour, don't listen

        if(evt.getPlayer().isFlying()) {
            Parkour parkour = getActiveParkour(evt.getPlayer());
            parkour.movePlayerToStart(evt.getPlayer()); // TP back to start
            parkour.onStart(evt.getPlayer()); // Restart parkour timer
            evt.getPlayer().setFlying(false); // Stop flying
            evt.getPlayer().sendMessage(ChatColor.RED + "You cannot fly during a parkour!");
        }
    }

    /**
     * Add a parkour to be tracked
     * @param parkour The parkour to add
     */
    public static void addParkour(Parkour parkour) {
        parkours.put(parkour.getName(), parkour);
    }

    /**
     * Check if there is a parkour by a given name
     * @param name The name of the parkour
     * @return contains
     */
    public static Parkour getParkourByName(String name) {
        return parkours.get(name);
    }

    /**
     * Check if there is a parkour by a given name
     * @param name The name of the parkour
     * @return contains
     */
    public static boolean containsParkour(String name) {
        return parkours.containsKey(name);
    }

    /**
     * Set this player to participate in a parkour
     * @param p The player
     * @param parkour The parkour
     */
    public static void setActiveParkour(Player p, Parkour parkour) {
        activeParkours.put(p, parkour);
        parkour.onStart(p);
    }

    /**
     * Check if a given player is participating in a parkour
     * @param p The player to check
     * @return hasParkour
     */
    public static boolean hasActiveParkour(Player p) {
        return activeParkours.containsKey(p);
    }

    /**
     * Get a player's active parkour
     * @param p The player to check
     * @return parkour
     */
    public static Parkour getActiveParkour(Player p) {
        return activeParkours.get(p);
    }

    /**
     * Get names of valid parkours
     * @return parkours
     */
    public static List<String> getParkourNames() {
        return parkours.keySet().stream().toList();
    }

    /**
     * Stop a player from participating in a parkour
     * @param player The player to stop
     */
    public static void stopActiveParkour(Player player) {
        activeParkours.remove(player);
    }

    /**
     * Delete a parkour
     * @param parkour The parkour to delete
     */
    public static void deleteParkour(Parkour parkour) {
        parkours.remove(parkour.getName()); // Unregister from map

        // Remove parkour from all active parkours
        Iterator<Player> it = activeParkours.keySet().iterator();
        while(it.hasNext()) {
            Parkour activeParkour = activeParkours.get(it.next());

            if(activeParkour.equals(parkour))
                it.remove(); // Remove from map if their active parkour is the one we are removing
        }

        // Remove parkour and all of its holograms
        parkour.onRemove();

        // Delete the save file
        File file = new File(PARKOUR_FOLDER, parkour.getName() + ".json");
        if(file.exists())
            file.delete();
    }

    /**
     * Get the parkour stats for a specific player
     * @param player The player to get the stats for
     */
    public static Map<String, List<ParkourScore>> getParkourStats(Player player) {
        try {
            Statement statement = SQL_CONNECTION.createStatement();
            ResultSet results = statement.executeQuery("SELECT parkourId, score, date FROM scores WHERE playerId = '" + player.getUniqueId() + "'");

            Map<String, List<ParkourScore>> parkourScores = new HashMap<>();

            while(results.next()) {
                String parkourName = results.getString("parkourId");
                double score = results.getDouble("score");
                Date date = results.getDate("date");
                parkourScores.computeIfAbsent(parkourName, k -> new ArrayList<>()).add(new ParkourScore(player.getUniqueId(), score, date));
            }

            // Sort scores
            parkourScores.values().forEach(list -> list.sort(new Comparator<ParkourScore>() {
                @Override
                public int compare(ParkourScore o1, ParkourScore o2) {
                    return Double.compare(o1.score, o2.score);
                }
            }));

            return parkourScores;
        } catch (Exception e) {
            Core.logInfo("Failed to retrieve parkour stats for player %s.", player.getName());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get a sorted list of scores for a specific parkour
     * @param parkourName The name of the parkour
     * @return scores
     */
    public static List<ParkourScore> getParkourTop(String parkourName) {
        try {
            Statement statement = SQL_CONNECTION.createStatement();
            ResultSet results = statement.executeQuery("SELECT playerId, score, date FROM scores WHERE parkourId = '" + parkourName + "'");

            List<ParkourScore> parkourScores = new ArrayList<>();

            while(results.next()) {
                UUID playerId = UUID.fromString(results.getString("playerId"));
                double score = results.getDouble("score");
                Date date = results.getDate("date");
                parkourScores.add(new ParkourScore(playerId, score, date));
            }

            // Sort scores
            parkourScores.sort(new Comparator<ParkourScore>() {
                @Override
                public int compare(ParkourScore o1, ParkourScore o2) {
                    return Double.compare(o1.score, o2.score);
                }
            });

            return parkourScores;
        } catch (Exception e) {
            Core.logInfo("Failed to get top leaderboards for parkour '%s'.", parkourName);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Log the finish time of a parkour
     * @param parkour The parkour that was completed
     * @param player The player that completed the parkour
     * @param seconds The seconds the player completed the parkour in
     */
    public static void logFinishTime(Parkour parkour, Player player, double seconds) {
        try {
            Statement statement = SQL_CONNECTION.createStatement();
            String sql = "INSERT INTO scores (playerId, parkourId, score, date) VALUES ('" +
                    player.getUniqueId() + "', '" + parkour.getName() + "', " + seconds + ", CURRENT_TIMESTAMP)";
            statement.executeUpdate(sql);

        } catch (Exception e) {
            Core.logInfo("Failed to send finish time to database for player %s.", player.getName());
            e.printStackTrace();
        }
    }

    @AllArgsConstructor @Getter
    public static class ParkourScore {
        private UUID playerId;
        private double score;
        private Date date;
    }
}
