package com.kihz.utils;

import com.kihz.Constants;
import com.kihz.Core;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.metadata.Metadatable;

public class MetadataUtils {

    /**
     * Returns if a given object that implements Metadatable has the given metadata?
     * @param obj The object to check
     * @param key The metadata to check for
     * @return hasMetadata
     */
    public static boolean hasMetadata(Metadatable obj, String key) {
        return obj.hasMetadata(key);
    }

    /**
     * Get the metadata's value from an object.
     * @param metadatable The object to get the metadata for
     * @param key The key of the metadata
     * @return metadata
     */
    public static MetadataValue getMetadata(Metadatable metadatable, String key) {
        return hasMetadata(metadatable, key) ? metadatable.getMetadata(key).get(0) : null;
    }

    /**
     * Get the metadata value from a metadatable object.
     * @param metadatable The object with the metadata
     * @param key The key of the metadata
     * @return value
     */
    public static <T> T getValue(Metadatable metadatable, String key) {
        return (T) (hasMetadata(metadatable, key) ? getMetadata(metadatable, key).value() : null);
    }

    /**
     * Get the metadata value from a metadatable object with a fallback value.
     * @param metadatable The object with the metadata
     * @param key The key of the metadata
     * @return value
     */
    public static <T> T getValue(Metadatable metadatable, String key, T fallback) {
        if(!hasMetadata(metadatable, key))
            setMetadata(metadatable, key, fallback);
        return getValue(metadatable, key);
    }

    /**
     * Set a metadata value on an object.
     * @param metadatable The object to set the metadata of
     * @param key The key of the metadata
     * @param o The value to set
     * @return value
     */
    public static <T> T setMetadata(Metadatable metadatable, String key, T o) {
        T oldValue = getValue(metadatable, key);
        Object obj = o;
        if(obj instanceof MetadataValue)
            obj = ((MetadataValue)o).value();
        if(obj instanceof Enum<?>)
            obj = ((Enum<?>)o).name();
        metadatable.setMetadata(key, new FixedMetadataValue(Core.getInstance(), obj));
        return oldValue;
    }

    /**
     * Remove metadata off of a given object.
     * @param metadatable The object to remove the metadata from
     * @param key The key of the metadata
     * @return removedValue
     */
    public static <T> T removeMetadata(Metadatable metadatable, String key) {
        T value = getValue(metadatable, key);
        metadatable.removeMetadata(key, Core.getInstance());
        return value;
    }

    /**
     * Add a cooldown to a metadatable object.
     * @param metadatable The object to add a cooldown to
     * @param cd The cooldown key
     * @param ticks The duration in ticks
     */
    public static void setCooldown(Metadatable metadatable, String cd, int ticks) {
        metadatable.setMetadata(cd, new FixedMetadataValue(Core.getInstance(), System.currentTimeMillis() + ((long) ticks * Constants.TICK_MS)));
    }

    /**
     * Returns if the player has a given cooldown.
     * @param metadatable The object to check for
     * @param cd The cooldown key
     * @param tickDuration The duration of the coldown
     * @return hasCooldown
     */
    public static boolean updateCooldown(Metadatable metadatable, String cd, int tickDuration, String action) {
        boolean hasCD = warnCooldown(metadatable, cd, action);
        if(!hasCD)
            setCooldown(metadatable, cd, tickDuration);
        return hasCD;
    }

    /**
     * Returns if the player has a given cooldown. If not, it will silently add the cooldown.
     * @param metadatable The object to check for
     * @param cd The cooldown key
     * @param tickDuration The duration of the coldown
     * @return hasCooldown
     */
    public static boolean updateCooldownSilently(Metadatable metadatable, String cd, int tickDuration) {
        boolean hasCD = hasCooldown(metadatable, cd);
        if(!hasCD)
            setCooldown(metadatable, cd, tickDuration);
        return hasCD;
    }

    /**
     * Does the given metadatable object have an active cooldown
     * @param metadatable The object to check
     * @param cd The cooldown key
     * @return hasCooldown
     */
    public static boolean hasCooldown(Metadatable metadatable, String cd) {
        return metadatable.hasMetadata(cd) && metadatable.getMetadata(cd).get(0).asLong() > System.currentTimeMillis();
    }

    /**
     * Does the given metadatable object have an active cooldown. Sends a message to the player
     * @param metadatable The object to check
     * @param cd The cooldown key
     * @return hasCooldown
     */
    public static boolean warnCooldown(Metadatable metadatable, String cd, String action) {
        boolean has = hasCooldown(metadatable, cd);
        if(has && metadatable instanceof CommandSender)
            ((CommandSender)metadatable).sendMessage(ChatColor.RED + "You must wait " + ChatColor.UNDERLINE +
                    TimeUtils.formatTime(metadatable.getMetadata(cd).get(0).asLong() - System.currentTimeMillis(), TimeUtils.FormatType.MINIMAL) + ChatColor.RED +
                    " before " + action + ".");
        return has;
    }
}
