package com.kihz.mechanics.parkour;

import com.google.gson.JsonElement;
import com.kihz.Core;
import com.kihz.mechanics.holograms.Hologram;
import com.kihz.mechanics.holograms.HologramTextLine;
import com.kihz.mechanics.holograms.Holograms;
import com.kihz.utils.Cuboid;
import com.kihz.utils.Utils;
import com.kihz.utils.jsontools.Jsonable;
import com.kihz.utils.jsontools.containers.JsonList;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@NoArgsConstructor @Getter
public class Parkour implements Jsonable {
    private String name;
    private ParkourNode startNode;
    @Setter private ParkourNode endNode;
    private final JsonList<ParkourNode> checkpoints = new JsonList<>();

    private transient final Map<Player, Long> playerStartTimes = new HashMap<>();
    private transient final Map<Player, Integer> currentCheckpoints = new HashMap<>();

    public Parkour(String name, Location startLoc) {
        this.name = name;
        startNode = new ParkourNode(name, startLoc, NodeType.START,
                ChatColor.GREEN + "Parkour '" + name + "' started!",
                ChatColor.GREEN + "Start '" + name + "' Parkour");
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof Parkour parkour))
            return false;

        return parkour.getName().equals(getName());
    }

    @Override
    public void load(JsonElement jsonElement) {
        load0(jsonElement); // Load class fields

        // Register parkour holograms on load
        if(startNode != null)
            setupNode(startNode);

        if(endNode != null)
            setupNode(endNode);

        for(ParkourNode node : checkpoints)
            setupNode(node);
    }

    /**
     * Add a checkpoint to this parkour
     * @param location The location of the checkpoint
     */
    public void addCheckpoint(Location location) {
        ParkourNode node = new ParkourNode(name, location, NodeType.CHECKPOINT,
                ChatColor.GREEN + "Checkpoint activated!",
                ChatColor.GREEN + "Checkpoint #" + (checkpoints.size() + 1));
        checkpoints.add(node);
    }

    /**
     * Remove a checkpoint
     * @param index The index to remove at
     */
    public void removeCheckpoint(int index) {
        if(index < 0 || index >= checkpoints.size())
            return; // Out of bounds

        ParkourNode node = checkpoints.remove(index);
        Holograms.removeHologram(node.getHologram());

        // Shift active checkpoints down
        currentCheckpoints.keySet().stream()
                .filter(key -> currentCheckpoints.get(key) == index)
                .forEach(key -> currentCheckpoints.put(key, Math.max(0, index - 1)));
    }

    /**
     * Move a player to a checkpoint
     * @param player The player to move
     * @param checkpointIndex The checkpoint index
     */
    public void movePlayerToCheckpoint(Player player, int checkpointIndex) {
        if(checkpointIndex < 0 || checkpointIndex >= checkpoints.size())
            return; // Out of bounds

        player.teleport(checkpoints.get(checkpointIndex).getLocation());
    }

    /**
     * Move a player to the start of the parkour
     * @param player The player to move
     */
    public void movePlayerToStart(Player player) {
        player.teleport(startNode.getLocation());
    }

    /**
     * Called when a player starts this parkour
     * @param player The player
     */
    public void onStart(Player player) {
        playerStartTimes.put(player, System.currentTimeMillis());
    }

    /**
     * Called when this parkour is completed
     * @param player The player completing the parkour
     */
    public void onComplete(Player player) {
        if(!playerStartTimes.containsKey(player))
            return;

        long endTime = System.currentTimeMillis();
        long startTime = playerStartTimes.remove(player);

        double secondsElapsed = (endTime - startTime) / 1000.0D;
        Parkours.logFinishTime(this, player, secondsElapsed);
        currentCheckpoints.remove(player);
        Parkours.stopActiveParkour(player);
        player.sendMessage(ChatColor.GREEN + "Finished parkour in " + Utils.DECIMAL_FORMAT.format(secondsElapsed) + " seconds.");
    }

    /**
     * Called when this parkour is removed
     */
    public void onRemove() {
        if(startNode != null)
            Holograms.removeHologram(startNode.getHologram());

        if(endNode != null)
            Holograms.removeHologram(endNode.getHologram());

        for(ParkourNode node : checkpoints)
            Holograms.removeHologram(node.getHologram());

        currentCheckpoints.clear();
        checkpoints.clear();
    }

    /**
     * Removes a player form this parkour
     * @param p The player to remove
     */
    public void removePlayer(Player p) {
        currentCheckpoints.remove(p);
    }

    @NoArgsConstructor @Getter @Setter
    public static class ParkourNode implements Jsonable {
        private String parkourOwner;
        private Location location;
        private NodeType nodeType;
        private JsonList<String> displayLines = new JsonList<>();
        private String interactMessage;

        private transient UUID hologramID;

        public ParkourNode(String parkourOwner, Location location, NodeType nodeType, String interactMessage, String... lines) {
            this.parkourOwner = parkourOwner;
            this.location = location;
            this.nodeType = nodeType;
            this.interactMessage = interactMessage;
            this.displayLines.addAll(lines);
            setupNode(this);
        }

        /**
         * Get this node's hologram
         * @return
         */
        public Hologram getHologram() {
            return getOrCreateNodeHologram(this);
        }

        /**
         * Get this node's hologram key
         * @return
         */
        public String getHologramName() {
            return "Parkour" + parkourOwner + nodeType.name();
        }
    }

    public enum NodeType {
        START,
        END,
        CHECKPOINT
    }

    /**
     * Setup a parkour node
     * @param node The node to setup
     */
    public static void setupNode(ParkourNode node) {
        Hologram hologram = getOrCreateNodeHologram(node);

        switch (node.getNodeType()) {
            case START -> {
                // Update hologram's on interact
                hologram.setOnInteract(player -> {
                    Parkours.setActiveParkour(player, Parkours.getParkourByName(node.getParkourOwner()));
                    if(node.interactMessage != null)
                        player.sendMessage(node.interactMessage);
                });
            }
            case END -> {
                // Update hologram's on interact
                hologram.setOnInteract(player -> {
                    if(!Parkours.hasActiveParkour(player))
                        return; // Player doesn't have an active parkour

                    Parkour playerParkour = Parkours.getActiveParkour(player);
                    Parkour parkour = Parkours.getParkourByName(node.getParkourOwner());
                    if(!playerParkour.equals(parkour))
                        return; // Not the same parkour

                    parkour.onComplete(player);
                    if(node.interactMessage != null)
                        player.sendMessage(node.interactMessage);
                });
            }
            case CHECKPOINT -> {
                // Update hologram's on interact
                hologram.setOnInteract(player -> {
                    Parkour parkour = Parkours.getParkourByName(node.getParkourOwner());
                    if(parkour == null)
                        return; // Parkour doesn't exist

                    if(!Parkours.hasActiveParkour(player) || Parkours.getActiveParkour(player) != parkour)
                        return; // We aren't taking place in this parkour


                    // We don't have an active checkpoint in this parkour
                    if(!parkour.currentCheckpoints.containsKey(player)) {
                        if(parkour.checkpoints.get(0).equals(node)) { // We hit the first checkpoint, we are valid
                            parkour.currentCheckpoints.put(player, 0);

                            if(node.interactMessage != null)
                                player.sendMessage(node.interactMessage);
                        }

                        return;
                    }

                    // Find this node's index in the parkour
                    int checkpointIndex = parkour.checkpoints.getValues().indexOf(node);

                    // We are at the checkpoint before, activate
                    if(parkour.currentCheckpoints.get(player) == (checkpointIndex - 1)) {
                        parkour.currentCheckpoints.put(player, checkpointIndex);

                        if(node.interactMessage != null)
                            player.sendMessage(node.interactMessage);
                    }
                });
            }
        }
    }

    /**
     * Get or create a hologram for a parkour node
     * @param node The node
     * @return hologram
     */
    private static Hologram getOrCreateNodeHologram(ParkourNode node) {
        if(node.getHologramID() != null) {
            Hologram hologram = Holograms.getHologram(node.getHologramID()); // Get the hologram

            if (hologram == null)  // Hologram isn't registered yet
                hologram = createHologram(node);

            return hologram;
        }

        // ID doesn't exit, create a new one
        Hologram hologram = createHologram(node);
        node.setHologramID(hologram.getUuid());
        return hologram;
    }

    /**
     * Create a hologram for a parkour node
     * @param node The node to create for
     * @return hologram
     */
    private static Hologram createHologram(ParkourNode node) {
        HologramTextLine[] hologramLines = node.displayLines.stream()
                .map(HologramTextLine::new)
                .toList()
                .toArray(HologramTextLine[]::new);

        // Register hologram
        return Holograms.addHologram(node.getHologramName(), node.getLocation(), false, hologramLines);
    }
}
