package com.kihz.utils;

import com.kihz.utils.jsontools.Jsonable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bukkit.Location;

@NoArgsConstructor @Getter @Setter
public class Point implements Jsonable {
    private int x;
    private int z;

    public Point(Location loc) {
        this(loc.getBlockX(), loc.getBlockZ());
    }

    public Point(int x, int z) {
        this.x = x;
        this.z = z;
    }

    @Override
    public int hashCode() {
        return (x * 31) + z;
    }

    @Override
    public boolean equals(Object object) {
        if(!(object instanceof Point))
            return false;
        Point point = (Point) object;
        return point.getX() == getX() && point.getZ() == getZ();
    }

    @Override
    @SuppressWarnings("MethodDoesntCallSuperMethod")
    public Point clone() {
        return new Point(getX(), getZ());
    }

    /**
     * Get the distance between this point and another point
     * @param point The point to get the distance between
     * @return distance
     */
    public double distance(Point point) {
        int xDistance = getX() - point.getX();
        int zDistance = getZ() - point.getZ();
        return Math.sqrt((xDistance * xDistance) + (zDistance * zDistance));
    }

}
