package com.kihz.utils;

import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class PacketUtils {

    /**
     * Update the title of an inventory using packets
     * @param p The player to update the inventory for
     * @param title The new title of the inventory
     */
    public static void updateInventoryTitle(Player p, String title) {
        sendPacket(p, new ClientboundOpenScreenPacket(NMSUtils.getNMSPlayer(p).containerMenu.containerId,
                NMSUtils.getNMSPlayer(p).containerMenu.getType(), Component.nullToEmpty(title)));
        p.updateInventory();
    }

    /**
     * Send a packet through NMS player connection
     * @param p The player to send to
     * @param packet The packet to send
     */
    public static void sendPacket(Player p, Packet<?> packet) {
        sendPacket(NMSUtils.getNMSPlayer(p).connection, packet);
    }

    /**
     * Send a packet through NMS player connection
     * @param connection The player connection
     * @param packet The packet to send
     */
    public static void sendPacket(ServerGamePacketListenerImpl connection, Packet<?> packet) {
        connection.send(packet);
    }

    /**
     * Send a packet using Minecraft's PlayerChunkMap
     * @param world The world to send from
     * @param entity The entity involved
     * @param packet The packet to send
     */
    public static void sendPacketToNearby(World world, Entity entity, Packet<?> packet) {
        NMSUtils.getNMSWorld(world).chunkSource.broadcast(NMSUtils.getNMSEntity(entity), packet);
    }

    /**
     * Send a packet using Minecraft's PlayerChunkMap including the entity
     * @param world The world to send from
     * @param entity The entity involved
     * @param packet The packet to send
     */
    public static void sendPacketToNearbyIncludingSelf(World world, Entity entity, Packet<?> packet) {
        NMSUtils.getNMSWorld(world).chunkSource.broadcastAndSend(NMSUtils.getNMSEntity(entity), packet);
    }

    /**
     * Destroy an entity for a given player
     * @param p The player to destroy the entity for
     * @param entityId The entity to remove
     */
    public static void sendDestroyEntityPacket(Player p, int entityId) {
        ClientboundRemoveEntitiesPacket packet = new ClientboundRemoveEntitiesPacket(entityId);
        PacketUtils.sendPacket(p, packet);
    }
}
