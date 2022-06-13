package com.kihz.utils;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.core.BlockPos;
import net.minecraft.network.Connection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.scores.PlayerTeam;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_18_R2.CraftServer;
import org.bukkit.craftbukkit.v1_18_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_18_R2.block.CraftBlock;
import org.bukkit.craftbukkit.v1_18_R2.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_18_R2.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_18_R2.scoreboard.CraftScoreboard;
import org.bukkit.craftbukkit.v1_18_R2.util.CraftMagicNumbers;
import org.bukkit.entity.Creature;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

public class NMSUtils {
    private static final String HANDLE_FIELD = "handle";

    public static final Class<CraftPlayer> CRAFT_PLAYER_CLASS = CraftPlayer.class;
    public static final Class<Level> WORLD_CLASS = Level.class;
    public static final Class<ServerLevel> WORLD_SERVER_CLASS = ServerLevel.class;
    public static final Class<CraftItemStack> CRAFT_ITEM_STACK_CLASS = CraftItemStack.class;
    public static final Class<Entity> ENTITY_CLASS = Entity.class;
    public static final Class<Player> ENTITY_HUMAN_CLASS = Player.class;
    public static final Class<CraftBlock> CRAFT_BLOCK_CLASS = CraftBlock.class;

    public static final int META_ENTITY_DATA_INDEX = 0;
    public static final int META_AIR_TICKS_INDEX = 1;
    public static final int META_CUSTOM_NAME_INDEX = 2;
    public static final int META_CUSTOM_NAME_VISIBLE_INDEX = 3;
    public static final int META_SILENT_INDEX = 4;
    public static final int META_HAS_NO_GRAVITY_INDEX = 5;
    public static final int META_POSE_INDEX = 6;
    public static final int META_TICKS_IN_SNOW_INDEX = 7;
    public static final int PLAYER_SKIN_OVERLAY_DATA = 17;

    /**
     * Return a Bukkit world as a Craft world.
     * @param world The world to cast.
     * @return craftWorld
     */
    public static CraftWorld getCraftWorld(org.bukkit.World world) {
        return (CraftWorld) world;
    }

    /**
     * Return a Bukkit world as an NMSWorld.
     * @param world The world to cast.
     * @return nmsWorld
     */
    public static ServerLevel getNMSWorld(org.bukkit.World world) {
        return getCraftWorld(world).getHandle();
    }

    /**
     * Return the bukkit server as a craft server
     * @return craftServer
     */
    public static CraftServer getCraftServer() {
        return (CraftServer) Bukkit.getServer();
    }

    /**
     * Get the vanilla server object.
     * Suppress deprecation for now, because I'd rather use this than Reflection.
     * @return serverObject
     */
    @SuppressWarnings("deprecation")
    public static MinecraftServer getServer() {
        return MinecraftServer.getServer();
    }

    /**
     * Get the vanilla server object from a world server.
     * @return serverObject
     */
    public static MinecraftServer getServer(org.bukkit.World world) {
        return getNMSWorld(world).getServer();
    }

    /**
     * Get the CraftPlayer object for this player.
     * @param player - The player to get the craft instance of.
     * @return craftPlayer
     */
    public static CraftPlayer getCraftPlayer(org.bukkit.entity.Player player) {
        return (CraftPlayer) player;
    }

    /**
     * Get the NMS player object of a bukkit player.
     * @param player - The bukkit player to get the NMS instance of.
     * @return nmsPlayer
     */
    public static ServerPlayer getNMSPlayer(org.bukkit.entity.Player player) {
        return getCraftPlayer(player).getHandle();
    }

    /**
     * Get a block as a craft block
     * @param block The block to get as craft blokc
     * @return craftBlock
     */
    public static CraftBlock getCraftBlock(Block block) {
        return (CraftBlock) block;
    }

    /**
     * Get a block as NMS
     * @param block The block to get as NMS
     * @return nmsBlock
     */
    public static BlockState getNMSBlock(Block block) {
        return getCraftBlock(block).getNMS();
    }

    /**
     * Get block data from material
     * @param material The material to get the block data for
     * @return blockData
     */
    public static CraftBlockData getCraftBlockData(org.bukkit.Material material) {
        return (CraftBlockData) material.createBlockData();
    }

    /**
     * Get NMS block data from a material
     * @param material The material to get the block data for
     * @return nmsBlockData
     */
    public static BlockState getBlockState(org.bukkit.Material material) {
        return getCraftBlockData(material).getState();
    }

    /**
     * Get NMS block data from a material
     * @param blockData The data to convert
     * @return nmsBlockData
     */
    public static CraftBlockData getCraftBlockState(BlockData blockData) {
        return (CraftBlockData) blockData;
    }

    /**
     * Get NMS block data from a material
     * @param blockData The data to convert
     * @return nmsBlockData
     */
    public static BlockState getNMSBlockState(BlockData blockData) {
        return getCraftBlockState(blockData).getState();
    }

    /**
     * Get a bukkit scoreboard as a craft scoreboard
     * @param scoreboard The scoreboard to cast
     * @return scoreboard
     */
    public static CraftScoreboard getCraftScoreboard(Scoreboard scoreboard) {
        return (CraftScoreboard) scoreboard;
    }

    /**
     * Get a bukkit scoreboard as an NMS scorebaord
     * @param scoreboard The scoreboard to cast
     * @return scoreboard
     */
    public static net.minecraft.world.scores.Scoreboard getNMSScoreboard(Scoreboard scoreboard) {
        return getCraftScoreboard(scoreboard).getHandle();
    }

    /**
     * Get an NMS team from a bukkit Team
     * @param team The team to convert
     * @return nmsTeam
     */
    public static PlayerTeam getNMSTeam(Team team) {
        return getNMSScoreboard(team.getScoreboard()).getPlayerTeam(team.getName());
    }

    /**
     * Get an NMS team from a bukkit Team
     * @param team The team to convert
     * @return nmsTeam
     */
    public static PlayerTeam getNMSTeam(String team) {
        return getNMSScoreboard(Bukkit.getScoreboardManager().getMainScoreboard()).getPlayerTeam(team);
    }

    /**
     * Get a player connection for a given player.
     * @param p The player to get the connection for.
     * @return playerConnection
     */
    public static ServerGamePacketListenerImpl getPlayerConnection(org.bukkit.entity.Player p) {
        return getCraftPlayer(p).getHandle().connection;
    }

    /**
     * Make an entity face a direction
     * @param entity The entity to change direction
     */
    public static void faceDirectionYaw(org.bukkit.entity.LivingEntity entity, double xDiff, double zDiff) {
        float rot = (float) Math.toDegrees((Math.atan2(-xDiff, zDiff) + (Math.PI * 2)) % (Math.PI * 2)); // Taken from Location#setDirection
        getNMSLiving(entity).setYHeadRot(rot);
        getNMSLiving(entity).yRot = rot;
        getNMSLiving(entity).yBodyRot = rot;
    }

    /**
     * Get a craft entity from a bukkit entity.
     * @param entity The entity to convert
     * @return craftEntity
     */
    public static CraftEntity getCraftEntity(org.bukkit.entity.Entity entity) {
        return (CraftEntity) entity;
    }

    /**
     * Get an NMS entity from its Bukkit entity.
     * @param entity The entity to convert
     * @return nmsEntity
     */
    public static Entity getNMSEntity(org.bukkit.entity.Entity entity) {
        return getCraftEntity(entity).getHandle();
    }

    /**
     * Get a bukkit living entity as NMS
     * @param entity The entity to convert
     * @return nmsLiving
     */
    public static LivingEntity getNMSLiving(org.bukkit.entity.LivingEntity entity) {
        return (LivingEntity) getNMSEntity(entity);
    }

    /**
     * Get a creature as an NMS creature
     * @param creature The creature to cast
     * @return nmsCreature
     */
    public static PathfinderMob getNMSCreature(Creature creature) {
        return (PathfinderMob) getNMSEntity(creature);
    }

    /**
     * Get an NMS Insentient entity from a bukkit living entity.
     * @param le The living entity to get as Insentient
     * @return nmsInsentient
     */
    public static Mob getNMSInsentient(org.bukkit.entity.LivingEntity le) {
        return (Mob) getNMSEntity(le);
    }

    /**
     * GEt the chunk provider for a given world
     * @param world The world to get the chunk provider for
     * @return chunkProvider
     */
    public static ServerChunkCache getChunkProvider(org.bukkit.World world) {
        return getNMSWorld(world).chunkSource;
    }

    /**
     * Get the player chunk map for a given world
     * @param world The world to get the player chunk map for
     * @return playerChunkMap
     */
    public static ChunkMap getPlayerChunkMap(org.bukkit.World world) {
        return getChunkProvider(world).chunkMap;
    }

    /**
     * Get a map of track entities indexed by entity id
     * @param world The world to get the tracked entities from
     * @return entityTrackerMap
     */
    public static Int2ObjectMap<ChunkMap.TrackedEntity> getEntityTracker(org.bukkit.World world) {
        return getPlayerChunkMap(world).entityMap;
    }

    /**
     * Gets a list of all network connections.
     * Creates a new ArrayList<> because the original is not thread safe, and this is a low-usage method.
     * @return networkConnections
     */
    @SuppressWarnings("unchecked")
    public static List<Connection> getAllConnections() {
        List<Connection> newList = new ArrayList<>(getServer().getConnection().connections); // Get connected channels
        Queue<Connection> pending = getServer().getConnection().pending;
        pending.stream().forEach(newList::add); // Add pending to the list
        return newList;
    }

    /**
     * Convert a bukkit ItemStack to an NMS ItemStack
     * @param itemStack The bukkit item to convert
     * @return nmsItem
     */
    public static ItemStack asNMSCopy(org.bukkit.inventory.ItemStack itemStack) {
        return CraftItemStack.asNMSCopy(itemStack);
    }

    /**
     * Get the Craft item from an item stack
     * @param itemStack The item stack to convert
     * @return
     */
    public static CraftItemStack getCraftItem(org.bukkit.inventory.ItemStack itemStack) {
        return (CraftItemStack) itemStack;
    }

    /**
     * Get the NMS version of a bukkit ItemStack.
     * @param itemStack The itemstack to convert
     * @return nmsItem
     */
    public static ItemStack getNMSItem(org.bukkit.inventory.ItemStack itemStack) {
        if(!(itemStack instanceof CraftItemStack))
            return asNMSCopy(itemStack);

        ItemStack nmsItem = getCraftItem(itemStack).handle;
        Utils.verifyNotNull(nmsItem, "NMS Item was null!");
        return nmsItem;
    }

    /**
     * Move an existing block to a new position
     * @param block The block to move
     * @param x x coord
     * @param y y coord
     * @param z z coord
     */
    public static void moveBlock(Block block, int x, int y, int z) {
        if(!CRAFT_BLOCK_CLASS.isInstance(block))
            throw new GeneralException("Cannot convert %s into a %s!", Utils.getSimpleName(block), Utils.getSimpleName(CRAFT_BLOCK_CLASS));

        ReflectionUtil.setField(block, CRAFT_BLOCK_CLASS, "position", new BlockPos(x, y, z));
    }

    /**
     * Get an object allowing for unsafe conversions.
     * @return unsafeMagic
     */
    public static CraftMagicNumbers getUnsafe() {
        return (CraftMagicNumbers) CraftMagicNumbers.INSTANCE;
    }

    /**
     * Get the current tick of the NMS server.
     * Subtracts one so that onEnable tasks can use %
     * @return tick
     */
    public static int getCurrentTick() {
        return MinecraftServer.currentTick - 1;
    }

    /**
     * Get the distance between a given entity and location.
     * We use this instead of #distance() because this is less resource intensive because we don't need a second location object.
     * @param entity The entity to check the distance for
     * @param location The location to check the distance for
     * @return distance
     */
    public static double getDistance(org.bukkit.entity.Entity entity, Location location) {
        return Math.sqrt(getDistanceSquared(entity, location));
    }

    /**
     * Get the distance between a given entity and location.
     * We use this instead of #distance() because this is less resource intensive because we don't need a second location object.
     * @param location1 The location to check the distance for
     * @param location2 The location to check the distance for
     * @return distance
     */
    public static double getDistance(Location location1, Location location2) {
        return Math.sqrt(getDistanceSquared(location1, location2));
    }

    /**
     * Get the distance between a given entity and location.
     * We use this instead of #distance() because this is less resource intensive because we don't need a second location object.
     * @param entity The entity to check the distance for
     * @param location The location to check the distance for
     * @return distance
     */
    public static double getDistanceSquared(org.bukkit.entity.Entity entity, Location location) {
        Entity nmsEntity = getNMSEntity(entity);
        double xDiff = nmsEntity.getX() - location.getX();
        double yDiff = nmsEntity.getY() - location.getY();
        double zDiff = nmsEntity.getZ() - location.getZ();
        return (xDiff * xDiff) + (yDiff * yDiff) + (zDiff * zDiff);
    }

    /**
     * Get the distance between a given entity and location.
     * We use this instead of #distance() because this is less resource intensive because we don't need a second location object.
     * @param location1 The location to check the distance for
     * @param location2 The location to check the distance for
     * @return distance
     */
    public static double getDistanceSquared(Location location1, Location location2) {
        double xDiff = location1.getX() - location2.getX();
        double yDiff = location1.getY() - location2.getY();
        double zDiff = location1.getZ() - location2.getZ();
        return (xDiff * xDiff) + (yDiff * yDiff) + (zDiff * zDiff);
    }

    /**
     * Get the distance between a given entity and location.
     * We use this instead of #distance() because this is less resource intensive because we don't need a second location object.
     * @param entity The entity to check the distance for
     * @param location The location to check the distance for
     * @return distance
     */
    public static double getDistanceNoY(org.bukkit.entity.Entity entity, Location location) {
        return Math.sqrt(getDistanceSquaredNoY(entity, location));
    }

    /**
     * Get the distance between a given entity and location.
     * We use this instead of #distance() because this is less resource intensive because we don't need a second location object.
     * @param entity The entity to check the distance for
     * @param location The location to check the distance for
     * @return distance
     */
    public static double getDistanceSquaredNoY(org.bukkit.entity.Entity entity, Location location) {
        Entity nmsEntity = getNMSEntity(entity);
        double xDiff = nmsEntity.getX() - location.getX();
        double zDiff = nmsEntity.getZ() - location.getZ();
        return (xDiff * xDiff) + (zDiff * zDiff);
    }

    /**
     * Convert a euler angle to its corresponding byte representation.
     * Useful for NMS protocol
     * @param euler The angle to convert
     * @return byte
     */
    public static byte convertEulerToByte(float euler) {
        return (byte) Mth.floor(euler * 256.0F / 360.0F);
    }

    /**
     * Get a list of players that can currently see this entity
     * @param entity The entity in question
     * @return seenBy
     */
    public static List<org.bukkit.entity.Player> getSeenBy(org.bukkit.entity.Entity entity) {
        return NMSUtils.getNMSWorld(entity.getWorld()).chunkSource.chunkMap.entityMap
                .get(entity.getEntityId())
                .seenBy
                .stream()
                .map(ServerPlayerConnection::getPlayer)
                .map(ServerPlayer::getBukkitEntity)
                .collect(Collectors.toList());
    }
}
