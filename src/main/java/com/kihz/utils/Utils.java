package com.kihz.utils;

import com.kihz.Core;
import com.kihz.mechanics.Restrictions;
import com.kihz.utils.jsontools.containers.JsonList;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.ServerPlayerConnection;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Utils {
    public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.##");
    public static final Random RANDOM = new Random();
    private static final Map<Class, Method> enumResolveMap = new ConcurrentHashMap<>();

    private static final Map<UUID, String> stringCache = new ConcurrentHashMap<>(); // Cache UUIDs for faster access
    private static final Function<UUID, String> UUID_TO_STRING_FUNCTION = UUID::toString;

    /**
     * Format a string safely.
     * @param str  The string to format.
     * @param args The arguments to format it with.
     * @return formattedString
     */
    public static String format(String str, Object... args) {
        if (args.length == 0)
            return str;

        try {
            return String.format(str, args);
        } catch (IllegalFormatException ife) {
            throw new GeneralException(ife, "Invalid string formatting for '" + str + "'.");
        }
    }

    /**
     * Format a string and apply color to the format, not the user input.
     * @param str  The string to format.
     * @param args The arguments to format it with.
     * @return formattedString
     */
    public static String formatColor(String str, Object... args) {
        return format(Utils.applyColor(str), args);
    }

    /**
     * Turn & color codes into the \247 color codes vanilla understands.
     * @param str The string to apply color to.
     * @return coloredString
     */
    public static String applyColor(String str) {
        return ChatColor.translateAlternateColorCodes('&', str);
    }

    /**
     * Create a file or directory.
     * @param file The file to create.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void createFile(File file) {
        if (file.exists())
            return;

        try {
            file.createNewFile();
        } catch (IOException ex) {
            throw new GeneralException(ex, "Failed to create " + file + ".");
        }
    }

    /**
     * Create a directory.
     * @param folder The directory to create.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static File createDirectory(File folder) {
        if (!folder.exists())
            folder.mkdirs();
        return folder;
    }

    /**
     * Verify a condition is true. If false, it will throw a GeneralException. This is safe to use, it won't allocate objects.
     * @param condition The condition to verify.
     * @param error     The error description.
     */
    @SuppressWarnings("ConstantConditions") // Explained below.
    public static void verify(boolean condition, String error) {
        assert condition; // Our IDE will not give warnings handled by this method if use assert. However, we tell the JVM to disable assert so our own custom exceptions are used at run-time.
        if (!condition)
            throw new GeneralException(error);
    }

    /**
     * Verify a condition is true. If false, it will throw a GeneralException.
     * Be careful about using this function, because it makes lots of objects.
     * @param condition The condition to verify.
     * @param error     The error description.
     * @param args      String format variables.
     */
    @SuppressWarnings("ConstantConditions") // Explained below.
    public static void verify(boolean condition, String error, Object... args) {
        assert condition; // Our IDE will not give warnings handled by this method if use assert. However, we tell the JVM to disable assert so our own custom exceptions are used at run-time.
        if (!condition)
            throw new GeneralException(Utils.format(error, args));
    }

    /**
     * Verify an object is not null. Otherwise, it will throw a GeneralException.
     * This is preferable to using var-args because it won't create an array object each time.
     * @param object The object to confirm is not null.
     * @param error  The error
     */
    @SuppressWarnings("ConstantConditions")
    public static void verifyNotNull(Object object, String error) {
        assert object != null; // Our IDE will not give warnings handled by this method if use assert. However, we tell the JVM to disable assert so our own custom exceptions are used at run-time.
        if (object == null)
            throw new GeneralException(error);
    }

    /**
     * Gets the simpleName of a class.
     * @param clazz The class to get the simple name of.
     * @return simpleName
     */
    public static String getSimpleName(Class<?> clazz) {
        verifyNotNull(clazz, "Cannot get simple name for null class.");
        return clazz.getSimpleName();
    }

    /**
     * Gets the simpleName of an object.
     * @param object The object to get the simple name of.
     * @return simpleName
     */
    public static String getSimpleName(Object object) {
        verifyNotNull(object, "Cannot get simple name of null object.");
        return getSimpleName(object.getClass());
    }

    /**
     * Shave the first element of any array.
     * @param array The array to shift.
     * @return shavedArray
     */
    public static <T> T[] shaveArray(T[] array) {
        return shaveArray(array, 1);
    }

    /**
     * Shave a given number of elements from an array.
     * @param array The array to shave.
     * @param firstKeep The first index to keep.
     * @return shavedArray
     */
    public static <T> T[] shaveArray(T[] array, int firstKeep) {
        return Arrays.copyOfRange(array, firstKeep, array.length);
    }

    /**
     * Remove duplicate entries from a list.
     * @param list The list to remove duplicates from.
     */
    public static <T> void removeDuplicates(List<T> list) {
        for (int i = 0; i < list.size(); i++)
            if (list.lastIndexOf(list.get(i)) != i)
                list.remove(i--);
    }

    /**
     * Try to register an object as a bukkit listener
     * Will do nothing if the object is not a listener
     * @param object The object to try and register.
     */
    public static void tryRegisterListener(Object object) {
        if(object instanceof Listener && !Listener.class.isAssignableFrom(object.getClass().getSuperclass()))
            Bukkit.getPluginManager().registerEvents((Listener) object, Core.getInstance());
    }

    /**
     * Gets the supplied input from a resulting NFE.
     * @param nfe The exception which created exception.
     * @return input
     */
    public static String getInput(NumberFormatException nfe) {
        return nfe.getLocalizedMessage().split(": ")[1].replaceAll("\"", "");
    }

    /**
     * Finds the amount of times a string occurs
     * @param search The string to search for
     * @param find The sub string to search for
     * @return count
     */
    public static int getCount(String search, String find) {
        int found = 0;
        for (int i = 0; i < search.length() - find.length() + 1; i++)
            if (search.substring(i, i + find.length()).equals(find))
                found++;
        return found;
    }

    /**
     * Capitalize every letter after a space.
     * @param str The sentence to capitalize.
     * @return capitalized
     */
    public static String capitalize(String str) {
        String[] split = str.replaceAll("_", " ").split(" ");
        List<String> out = Arrays.stream(split).filter(s -> s.length() > 0)
                .map(s -> s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase())
                .collect(Collectors.toList());
        return String.join(" ", out);
    }

    /**
     * Test if an InventoryClickEvent is a hotbar swap.
     * @param evt The event to test.
     * @return isHotbarEvent
     */
    public static boolean isHotbarEvent(InventoryClickEvent evt) {
        return evt.getAction() == InventoryAction.HOTBAR_SWAP || evt.getAction() == InventoryAction.HOTBAR_MOVE_AND_READD;
    }

    /**
     * Use a given amount of stacks for a gien item
     * @param item The item to edit
     * @param amount The amount to use
     * @return item
     */
    public static ItemStack useItem(ItemStack item, int amount) {
        item.setAmount(item.getAmount() - amount);
        if(item.getAmount() <= 0)
            item.setType(Material.AIR);
        return item;
    }

    /**
     * Is this item air?
     * @param itemStack The item to test.
     * @return isAir
     */
    public static boolean isAir(ItemStack itemStack) {
        return itemStack == null || itemStack.getType() == Material.AIR;
    }

    /**
     * If we are on the primary thread, run the task now. Else, wait.
     * @param runnable The task to run
     */
    public static void runSynchronous(Runnable runnable) {
        if(Bukkit.isPrimaryThread()) {
            runnable.run();
        } else {
            Bukkit.getScheduler().runTask(Core.getInstance(), runnable);
        }
    }

    /**
     * Gets an enum value from the given class. Returns null if not found.
     * @param value The enum's raw name.
     * @param clazz The enum's class.
     */
    public static <T extends Enum<T>> T getEnum(String value, Class<T> clazz) {
        return getEnum(value, clazz, null);
    }

    /**
     * Gets an enum value, falling back on a default value.
     * @param value        The enum's raw name.
     * @param defaultValue The default enum value, if value was null.
     */
    @SuppressWarnings("unchecked")
    public static <T extends Enum<T>> T getEnum(String value, T defaultValue) {
        return getEnum(value, (Class<T>) defaultValue.getClass(), defaultValue);
    }

    @SuppressWarnings({"unchecked", "EmptyCatchBlock"})
    public static <T extends Enum<T>> T getEnum(String enumName, Class<T> enumClass, T defaultValue) {
        verifyNotNull(enumClass, "Tried to get an enum value with a null enum class.");

        if (enumName != null && enumName.length() > 0) {
            try {
                Method resolveMethod = enumResolveMap.get(enumClass);
                if (resolveMethod == null)
                    enumResolveMap.put(enumClass, resolveMethod = enumClass.getMethod("valueOf", String.class));

                return (T) resolveMethod.invoke(null, enumName);
            } catch (Exception e) {

            }
        }

        if (defaultValue == null) // Such as if an enum was removed.
            Core.logInfo("Unknown enum value '%s' in %s.", enumName, getSimpleName(enumClass));
        return defaultValue;
    }

    /**
     * Get a random element from an array.
     * @param arr The array to get a random element from.
     * @return element
     */
    public static <T> T randElement(T[] arr) {
        return arr.length == 0 ? null : arr[nextInt(arr.length)];
    }

    /**
     * Get a random element from a list.
     * @param list The list to get a random element from.
     * @return element
     */
    public static <T> T randElement(List<T> list) {
        return randElement(list, null);
    }

    /**
     * Get a random element from a list.
     * @param list The list to get a random element from.
     * @return element
     */
    public static <T> T randElement(JsonList<T> list) {
        return randElement(list.getValues());
    }

    /**
     * Get a random element from a list.
     * @param list     The collection to get a random element from.
     * @param fallback The fallback value to return if there are no values.
     * @return element
     */
    public static <T> T randElement(List<T> list, T fallback) {
        return list.isEmpty() ? fallback : list.get(nextInt(list.size()));
    }

    /**
     * Get a random element from a list.
     * @param collection The collection to get a random element from.
     * @return element
     */
    public static <T> T randElement(Collection<T> collection) {
        return randElement(collection, null);
    }

    /**
     * Get the Random object.
     * @return random
     */
    public static Random getRandom() {
        return Bukkit.isPrimaryThread() ? RANDOM : ThreadLocalRandom.current();
    }

    /**
     * Get a random number between the given range.  The min and max parameters are inclusive.
     * @param min The minimum wanted number.
     * @param max The maximum wanted number.
     * @return int
     */
    public static int randInt(int min, int max) {
        if (min >= max)
            return max;
        return max + min > 0 ? nextInt(max - min + 1) + min : randInt(0, max - min) + min;
    }

    /**
     * Generate a random float between a float range.
     * @param min The minimum wanted float.
     * @param max The maximum wanted float.
     * @return point
     */
    public static float randFloat(float min, float max) {
        return getRandom().nextFloat() * (max - min) + min;
    }

    /**
     * A random double between 0 and 1.
     * @return randomDouble
     */
    public static double randDouble() {
        return getRandom().nextDouble();
    }

    /**
     * Generate a random number between two doubles.
     * @param min The minimum wanted double.
     * @param max The maximum wanted double.
     * @return rand
     */
    public static double randDouble(double min, double max) {
        return min == max ? min : ThreadLocalRandom.current().nextDouble(min, max);
    }

    /**
     * Generate a random number between two longs.
     * @param min The minimum wanted long.
     * @param max The maximum wanted long.
     * @return rand
     */
    public static long randLong(long min, long max) {
        return min == max ? min : ThreadLocalRandom.current().nextLong(min, max);
    }

    /**
     * Gets a random number between 0 and max - 1
     * @param max The maximum value + 1. For instance, nextInt(3) would give values in the range [0,3].
     * @return rand
     */
    public static int nextInt(int max) {
        return getRandom().nextInt(max);
    }

    /**
     * Check if a random chance succeeds.
     * @param chance 1 / chance = the % chance.
     * @return success
     */
    public static boolean randChance(int chance) {
        return chance <= 1 || nextInt(chance) == 0;
    }

    /**
     * Random chance based on a double value.
     * The closer the input is to zero, the lower the chance. The higher, the higher.
     * @param chance The chance between 0 and 1.
     * @return randChance
     */
    public static boolean randChance(double chance) {
        return nextDouble() <= chance;
    }

    /**
     * Get a random true/false value.
     * @return randomBool
     */
    public static boolean nextBool() {
        return randChance(2);
    }

    /**
     * Performs Random#nextDouble.
     * @return randomDouble
     */
    public static double nextDouble() {
        return getRandom().nextDouble();
    }

    /**
     * Performs Random#nextLong.
     * @return randomLong
     */
    public static double nextLong() {
        return getRandom().nextLong();
    }

    /**
     * Get a random element from a list.
     * @param collection The collection to get a random element from.
     * @param fallback   The fallback value to return if there are no values.
     * @return element
     */
    public static <T> T randElement(Collection<T> collection, T fallback) {
        if (collection.size() == 0)
            return fallback;

        int index = nextInt(collection.size());
        Iterator<T> iterator = collection.iterator();
        while (iterator.hasNext() && index-- > 0)
            iterator.next();
        return iterator.next();
    }

    /**
     * Add an item to the player's inventory. If the inventory is full it will drop on the ground.
     * @param p The player to give the item to
     * @param item The item to give the player
     */
    public static void giveItem(Player p, ItemStack item) {
        giveItem(p, item, false);
    }

    /**
     * Add an item to the player's inventory. If the inventory is full it will drop on the ground.
     * @param player The player to give the item to
     * @param itemStack The item to give the player
     */
    public static void giveItem(Player player, ItemStack itemStack, boolean callEvent) {
        if (Utils.isAir(itemStack))
            return; // Can't give empty item.

        Inventory inv = player.getInventory();
        if (inv.firstEmpty() > -1) { // There is an open stack, we can add it with bukkit.
            inv.addItem(itemStack);
            return;
        }

        for (int i = 0; i < inv.getSize(); i++) { // Add the possible items to the player's inventory.
            ItemStack item = inv.getItem(i);
            if (Utils.isAir(item) || !item.isSimilar(itemStack))
                continue;

            int deposit = Math.min(item.getMaxStackSize(), item.getAmount() + itemStack.getAmount()) - item.getAmount();
            itemStack.setAmount(itemStack.getAmount() - deposit);
            item.setAmount(item.getAmount() + deposit);
        }

        if (itemStack.getAmount() > 0) {
            Restrictions.whitelistItemDrop(player.getWorld().dropItem(player.getLocation(), itemStack), player);
            player.sendMessage(ChatColor.RED + "Your inventory was full, so you dropped the item.");
        }
    }

    /**
     * This will clear a player's cursor, and give them the item back in their inventory.
     * This will prevent certain exploits that involve inventories.
     * @param p The player to fix the cursor for
     */
    public static void clearCursor(Player p) {
        ItemStack item = p.getItemOnCursor();
        if(isAir(item))
            return;

        p.setItemOnCursor(new ItemStack(Material.AIR));
        giveItem(p, item);
    }

    /**
     * Does the given player have an open inventory that isn't their own inventory?
     * @param humanEntity The player to check
     * @return hasOpenInventory
     */
    public static boolean hasOpenInventory(HumanEntity humanEntity) {
        return !isPlayerInventory(humanEntity.getOpenInventory().getType());
    }

    /**
     * Is this inventory type a player's inventory?
     * Player Inventories fall under type CRAFTING for some reason. No need to add a check for InventoryType.PLAYER
     * @param type The inventory type to check
     * @return isPlayerInventory
     */
    public static boolean isPlayerInventory(InventoryType type) {
        return type == InventoryType.CREATIVE || type == InventoryType.CRAFTING;
    }

    /**
     * Limit a string to a given amount fo characters
     * @param s The string to limit
     * @param maxSize The max size of the string
     * @return string
     */
    public static String cutString(String s, int maxSize) {
        return s != null ? s.length() <= maxSize ? s : s.substring(0, maxSize - 3) + "..." : null;
    }

    /**
     * Gets the first key whose indexed value matches the passed value.
     * @param map The map to search keys from.
     * @param value The value to search with.
     * @return indexKey
     */
    public static <K, V> K getKey(Map<K, V> map, V value) {
        for (K key : map.keySet())
            if (value.equals(map.get(key)))
                return key;
        return null;
    }

    /**
     * Convert a UUID to a String, cache the value if we haven't already.
     * @param uuid The uuid to convert
     * @return uuidString
     */
    public static String uuidTostring(UUID uuid) {
        return stringCache.computeIfAbsent(uuid, UUID_TO_STRING_FUNCTION);
    }

    /**
     * Return the display name of the given itemstack.
     * @param item The item to get the name of
     * @return itemName
     */
    public static String getItemName(ItemStack item) {
        if(isAir(item))
            return "Nothing";

        if(item.hasItemMeta() && item.getItemMeta().hasDisplayName())
            return ((TextComponent)item.getItemMeta().displayName()).content();

        return capitalize(item.getType().name().replaceAll("_", " "));
    }

    /**
     * Returns a better looking location string.
     * @param loc The location to transfter to a string.
     * @return string
     */
    public static String getCleanLocationString(Location loc) {
        return "[" + (loc != null ? (loc.getWorld() != null ? loc.getWorld().getName() : null)
                + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ() : "null") + "]";
    }

    /**
     * Generate a random entity ID
     * @return id
     */
    public static int generateEntityID() {
        return net.minecraft.world.entity.Entity.nextEntityId();
    }

    /**
     * Check if a given location is within a cuboid space
     * @param loc The location to check
     * @param x1 Min x
     * @param y1 Min y
     * @param z1 Min z
     * @param x2 Max x
     * @param y2 Max y
     * @param z2 Max z
     * @return isInCuboid
     */
    public static boolean isInCuboid(Location loc, int x1, int y1, int z1, int x2, int y2, int z2) {
        return Math.min(x2, Math.max(x1, loc.getX())) == loc.getX()
                && Math.min(y2, Math.max(y1, loc.getY())) == loc.getY()
                && Math.min(z2, Math.max(z1, loc.getZ())) == loc.getZ();
    }

    /**
     * Send a packet to players that can see the given entity
     * @param packets The packet to send
     * @param entity The entity
     */
    public static void sendPacketToTrackedEntities(Entity entity, Packet<?>... packets) {
        ServerLevel nmsWorld = NMSUtils.getNMSWorld(entity.getWorld());
        ChunkMap.TrackedEntity trackedEntity = nmsWorld.chunkSource.chunkMap.entityMap.get(entity.getEntityId());
        if(trackedEntity == null)
            return;

        // Send packet to entities that can see the entity
        for(ServerPlayerConnection conn : trackedEntity.seenBy)
                Arrays.stream(packets).forEach(conn::send);
    }
}
