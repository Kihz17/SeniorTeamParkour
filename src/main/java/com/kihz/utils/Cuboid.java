package com.kihz.utils;

import com.kihz.Core;
import com.kihz.utils.jsontools.Jsonable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

@NoArgsConstructor @Getter @Setter
public class Cuboid implements Jsonable {
    private int xMin;
    private int yMin;
    private int zMin;
    private int xMax;
    private int yMax = 256;
    private int zMax;

    @Setter private transient World world = Bukkit.getWorlds().get(0);

    public Cuboid(Location pos1, Location pos2) {
        this(pos1.getBlockX(), pos1.getBlockY(), pos1.getBlockZ(), pos2.getBlockX(), pos2.getBlockY(), pos2.getBlockZ());
    }

    public Cuboid(Point pos1, Point pos2) {
        this(pos1.getX(), 0, pos1.getZ(), pos2.getX(), 256, pos2.getZ());
    }

    public Cuboid(int x1, int y1, int z1, int x2, int y2, int z2) {
        this.xMin = Math.min(x1, x2);
        this.yMin = Math.min(y1, y2);
        this.zMin = Math.min(z1, z2);
        this.xMax = Math.max(x1, x2);
        this.yMax = Math.max(y1, y2);
        this.zMax = Math.max(z1, z2);
    }

    /**
     * Get the min point for this cuboid (does not include y)
     * @return minPoint
     */
    public Point getMinPoint() {
        return new Point(getXMin(), getZMin());
    }

    /**
     * Get the max point for this cuboid (does not include y)
     * @return maxPoint
     */
    public Point getMaxPoint() {
        return new Point(getXMax(), getZMax());
    }

    /**
     * Is a given location inside this cuboid?
     * @param loc The location to check
     * @return containsLocation
     */
    public boolean containsLocation(Location loc) {
        boolean sameWorld = loc.getWorld() != null && loc.getWorld().equals(getWorld());
        return sameWorld && Utils.isInCuboid(loc, xMin - 1, yMin - 1, zMin - 1, xMax + 1, yMax + 1, zMax + 1);
    }
}