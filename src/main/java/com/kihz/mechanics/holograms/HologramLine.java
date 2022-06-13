package com.kihz.mechanics.holograms;

import com.kihz.utils.GeneralException;
import com.kihz.utils.PacketUtils;
import com.kihz.utils.Utils;
import com.kihz.utils.jsontools.Jsonable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.minecraft.core.Registry;
import net.minecraft.network.protocol.game.ClientboundAddMobPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.world.entity.EntityType;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.UUID;

@Getter @NoArgsConstructor
public abstract class HologramLine implements Jsonable {
    @Setter
    private double height;
    private boolean marker;

    public HologramLine(double height) {
        this(height, false);
    }

    public HologramLine(double height, boolean isMarker) {
        this.height = height;
        this.marker = isMarker;
    }

    /**
     * Show this hologram line to players
     */
    public abstract void show(Location location);

    /**
     * Update line for players
     */
    public abstract void update(Location location);

    /**
     * Hide this line
     */
    public abstract void hide();

    /**
     * Get this line's calue as a string
     * @return string
     */
    public abstract String getValueAsString();

    public abstract UUID getUUID();

    public abstract int getEntityId();

    /**
     * Get this line as a text line
     * @return line
     */
    public HologramTextLine getAsTextLine() {
        if(!isTextLine())
            throw new GeneralException("Tried to get hologram line as a text line");

        return (HologramTextLine) this;
    }

    /**
     * Is this line a text line?
     * @return isTextLine
     */
    public boolean isTextLine() {
        return this instanceof HologramTextLine;
    }

    /**
     * Get the generic display name for this line
     * @return name
     */
    public String getGenericName() {
        return isTextLine() ? "Text Line" : "";
    }
}
