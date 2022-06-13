package com.kihz.mechanics.holograms;

import com.google.gson.JsonElement;
import com.kihz.utils.Cuboid;
import com.kihz.utils.jsontools.Jsonable;
import com.kihz.utils.jsontools.containers.JsonList;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.function.Consumer;

@NoArgsConstructor @Getter
public class Hologram implements Jsonable {
    private String name;
    private String worldName;
    @Setter private Location location;
    private JsonList<HologramLine> lines = new JsonList<>();

    private final transient UUID uuid = UUID.randomUUID();
    @Setter private transient Consumer<Player> onInteract;

    public Hologram(String name, Location location) {
        this.name = name;
        this.location = location;
        this.worldName = location.getWorld().getName();
    }

    public Hologram(Location source) {
        this.name = "unnamed";
        this.location = source;
        this.worldName = location.getWorld().getName();
    }

    public Hologram(String name, Location source, String worldName) {
        this.name = name;
        this.location = source;
        this.worldName = worldName;
    }

    public Hologram(Location source, String worldName) {
        this.name = "unnamed";
        this.location = source;
        this.worldName = worldName;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof Hologram holo))
            return false;

        return holo.getUuid().equals(getUuid());
    }

    @Override
    public int hashCode() {
        return this.uuid.hashCode();
    }

    @Override
    public void load(JsonElement element) {
        Jsonable.super.load(element);
        if(Bukkit.getWorld(getWorldName()) == null)
            Holograms.getQueuedHolograms().computeIfAbsent(getWorldName(), k -> new ArrayList<>()).add(this);
    }

    /**
     * Set this holograms world to a given world
     * @param world The world to set the hologram to
     */
    public void setWorld(World world) {
        getLocation().setWorld(world);
    }

    /**
     * Show this hologram
     */
    public void show() {
        double currentY = getLocation().getY();
        for(int i = 0; i < getLines().size(); i++) {
            HologramLine line = getLines().get(i);
            currentY += line.getHeight();
            if(i != 0)
                currentY += 0.02; // Default space between lines

            Location loc = getLocation().clone();
            loc.setY(currentY);
            line.show(loc);
        }
    }

    /**
     * Hide this hologram to all viewers
     */
    public void hide() {
        getLines().forEach(HologramLine::hide);
    }

    /**
     * Refresh this hologram for a given player
     * This will update position and new metadata
     * IT WILL NOT SPAWN NEW LINES
     */
    public void refresh() {
        double currentY = getLocation().getY();
        for(int i = 0; i < getLines().size(); i++) {
            HologramLine line = getLines().get(i);
            currentY += line.getHeight();
            if(i != 0)
                currentY += 0.02; // Default space between lines

            Location loc = getLocation().clone();
            loc.setY(currentY);
            line.update(loc);
        }
    }

    /**
     * Run behaviour when a player interacts with this hologram
     * @param p The player interacting
     */
    public void onInteract(Player p) {
        if(onInteract == null)
            return;

        onInteract.accept(p);
    }

    /**
     * Get a line from this hologram from an id
     * @param id The id to get the line from
     * @return line
     */
    public HologramLine getLineFromEntityID(int id) {
        return getLines().stream().filter(line -> line.getEntityId() == id).findFirst().orElse(null);
    }
}
