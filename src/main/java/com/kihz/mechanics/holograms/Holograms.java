package com.kihz.mechanics.holograms;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedEnumEntityUseAction;
import com.kihz.Core;
import com.kihz.mechanics.system.GameMechanic;
import com.kihz.utils.Utils;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Holograms extends GameMechanic {
    @Getter private static final Map<String, List<Hologram>> queuedHolograms = new HashMap<>();

    private static final Map<UUID, Hologram> holograms = new HashMap<>();

    public static final double DEFAULT_TEXT_HEIGHT = 0.24D;
    public static final double DEFAULT_ITEM_HEIGHT = 0.4D;

    @Override
    public void onEnable() {
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(Core.getInstance(), PacketType.Play.Client.USE_ENTITY) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                int entityId = event.getPacket().getIntegers().read(0);
                Hologram hologram = holograms
                        .values()
                        .stream()
                        .filter(h -> h.getLineFromEntityID(entityId) != null)
                        .findFirst()
                        .orElse(null);

                if(hologram == null)
                    return;

                WrappedEnumEntityUseAction action = event.getPacket().getEnumEntityUseActions().read(0);
                Player p = event.getPlayer();
                if(action.getAction() == EnumWrappers.EntityUseAction.ATTACK)
                    hologram.onInteract(p);
            }
        });
    }

    @Override
    public void onDisable() {
        holograms.values().forEach(Hologram::hide);
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent evt) { // Register queued holos on world load
        String worldName = evt.getWorld().getName();
        List<Hologram> holograms = getQueuedHolograms().get(worldName);
        if(holograms == null || holograms.isEmpty())
            return;

        holograms.forEach(holo -> {
            holo.setWorld(evt.getWorld());
            holograms.add(holo);
        });

        getQueuedHolograms().remove(worldName);
    }

    /**
     * Add a hologram to the world
     * @param name The name of the hologram
     * @param location The location of the hologram
     * @param persistent Is this hologram persistent?
     */
    public static Hologram addHologram(String name, Location location, boolean persistent, HologramLine... lines) {
        Hologram hologram = new Hologram(name, location);
        hologram.getLines().addAll(lines);

        holograms.put(hologram.getUuid(), hologram);
        hologram.show();

        return hologram;
    }

    /**
     * Register a hologram
     * @param hologram The hologram to register
     */
    public static void registerHologram(Hologram hologram) {
        holograms.put(hologram.getUuid(), hologram);
        hologram.show();
    }

    /**
     * Check if there is a hologram registered with a given name
     * @param id The id to check
     * @return contains
     */
    public static boolean containsHologram(UUID id) {
        return holograms.containsKey(id);
    }

    /**
     * Get the hologram registered with a given name
     * @param id The id to check
     * @return hologram
     */
    public static Hologram getHologram(UUID id) {
        return holograms.get(id);
    }

    /**
     * Remove a hologram from the world.
     * @param hologram The hologram to remove
     */
    public static void removeHologram(Hologram hologram) {
        holograms.remove(hologram.getUuid());
        hologram.hide();
    }

    /**
     * Add a text line to the given hologram
     * @param hologram The hologram to add to
     * @param text The text on the hologram
     * @param height The height of the line
     * @param marker Does this line have a hitbox?
     */
    public static void addTextLine(Hologram hologram, String text, double height, boolean marker) {
        HologramTextLine line = new HologramTextLine(height, text, marker);
        hologram.getLines().add(line);
        Location loc = hologram.getLocation().clone();
        loc.setY(getLineHeight(hologram));
        line.show(loc);
    }

    /**
     * Add a text line to the given hologram
     * @param hologram The hologram to add to
     * @param text The text on the hologram
     * @param marker Does this line have a hitbox?
     */
    public static void addTextLine(Hologram hologram, String text, boolean marker) {
        addTextLine(hologram, text, DEFAULT_TEXT_HEIGHT, marker);
    }

    /**
     * Add a text line to the given hologram
     * @param hologram The hologram to add to
     * @param text The text on the hologram
     */
    public static void addTextLine(Hologram hologram, String text) {
        addTextLine(hologram, text, false);
    }

    /**
     * Remove a line from a given hologram
     * @param hologram The hologram to remove from
     * @param index The index of the line to remove
     */
    public static void removeLine(Hologram hologram, int index) {
        HologramLine line = hologram.getLines().remove(index);
        line.hide();
        hologram.refresh();
    }

    /**
     * Edit an existing text line for the given hologram
     * @param hologram The hologram to edit
     * @param index The index of the line
     * @param newText The new display text of the line
     */
    public static void editTextLine(Hologram hologram, int index, String newText) {
        Utils.verify(index >= 0 && index < hologram.getLines().size(), "Index was our of range for Hologram edit line.");
        Utils.verify(hologram.getLines().get(index).isTextLine(), "Line at index %d is not a text line!", index);
        hologram.getLines().get(index).getAsTextLine().setText(newText);
        hologram.refresh();
    }

    /**
     * Get the line height of the last line of a hologram
     * @param hologram The hologram
     * @return height
     */
    private static double getLineHeight(Hologram hologram) {
        double y = hologram.getLocation().getY();
        for(int i = 0; i < hologram.getLines().size(); i++) {
            HologramLine line = hologram.getLines().get(i);
            y += line.getHeight();
            if(i != 0)
                y += 0.02D;
        }
        return y;
    }
}
