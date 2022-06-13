package com.kihz.mechanics.holograms;

import com.kihz.Core;
import com.kihz.utils.NMSUtils;
import com.kihz.utils.PacketUtils;
import com.kihz.utils.Utils;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundAddMobPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.Level;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;

@Getter @NoArgsConstructor
public class HologramTextLine extends HologramLine {
    private String text;

    private transient ArmorStand armorStand;

    public HologramTextLine(String text) {
        this(0.24D, text, false);
    }

    public HologramTextLine(String text, boolean marker) {
        this(0.24D, text, marker);
    }

    public HologramTextLine(double height, String text, boolean marker) {
        super(height, marker);
        this.text = text;
    }

    @Override
    public UUID getUUID() {
        return armorStand.getUUID();
    }

    @Override
    public int getEntityId() {
        return armorStand.getId();
    }

    @Override
    public void show(Location location) {
        if(armorStand != null) // Already there
            return;

        Level nmsWorld = NMSUtils.getNMSWorld(location.getWorld());
        ArmorStand armorStand = new ArmorStand(EntityType.ARMOR_STAND, nmsWorld);
        armorStand.setCustomNameVisible(true);
        armorStand.setCustomName(Component.nullToEmpty(text));
        armorStand.setInvisible(true);
        armorStand.setNoGravity(true);
        armorStand.noPhysics = true;
        armorStand.setPos(location.getX(), location.getY(), location.getZ());
        nmsWorld.addFreshEntity(armorStand);
        this.armorStand = armorStand;
    }

    @Override
    public void update(Location location) {
        if(armorStand == null) { // Not there, spawn in
            show(location);
            return;
        }

        armorStand.setCustomName(Component.nullToEmpty(text));
        armorStand.setPos(location.getX(), location.getY(), location.getZ());
    }

    @Override
    public void hide() {
        if(armorStand != null)
            armorStand.discard();
    }

    @Override
    public String getValueAsString() {
        return getText();
    }

    /**
     * Set this lines text and refresh the parent hologram
     * @param text The text to set
     */
    public void setText(String text) {
        this.text = text;
    }
}
