package com.kihz.mechanics;

import com.kihz.Core;
import com.kihz.mechanics.system.GameMechanic;
import com.kihz.utils.jsontools.JsonSerializer;
import com.kihz.utils.jsontools.Jsonable;
import com.kihz.utils.jsontools.containers.JsonList;
import com.kihz.utils.jsontools.containers.JsonMap;
import com.kihz.utils.jsontools.containers.JsonSet;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.List;

public class Permissions extends GameMechanic {
    public static PermissionData permissionData = new PermissionData();

    @Override
    public void onEnable() { // Load permission data
        File permissionsFile = Core.getFile("permissions.json");
        if(permissionsFile.exists())
            permissionData = JsonSerializer.deserializeFromFile(PermissionData.class, permissionsFile);
    }

    @Override
    public void onDisable() {
        permissionData.saveToFile(Core.getCreateFile("permissions.json"));
    }

    /**
     * Get all permissions
     * @return permissions
     */
    public static List<String> getPermissions() {
        return permissionData.permissions.getData().stream().toList();
    }

    /**
     * Add a permission to the list of valid permissions
     * @param perm The permission to add
     */
    public static void addPermission(String perm) {
        permissionData.addPermission(perm);
    }

    /**
     * Add a command permission entry
     * @param player The player to add for
     * @param perm The permission
     */
    public static void addEntry(Player player, String perm) {
        permissionData.addEntry(player, perm);
    }

    /**
     * Check if a player has a specific permission
     * @param p The player to check
     * @param perm The permission to check
     * @return hasPermission
     */
    public static boolean hasPermission(Player p, String perm, boolean sendMessage) {
        boolean value = permissionData.hasPermission(p, perm);
        if(!value && sendMessage)
            p.sendMessage(ChatColor.RED + "You need permission '" + perm + "' to do this.");

        return value;
    }

    public static class PermissionData implements Jsonable {
        private final JsonSet<String> permissions = new JsonSet<>();
        private final JsonMap<PermissionSet> playerPermissions = new JsonMap<>();

        /**
         * Add a permission to the list
         * @param permission The permission to add
         */
        public void addPermission(String permission) {
            permissions.add(permission);
        }

        /**
         * Add a command permission entry
         * @param player The player to add for
         * @param perm The permission
         */
        public void addEntry(Player player, String perm) {
            String key = player.getUniqueId().toString();
            if(!playerPermissions.containsKey(key))
                playerPermissions.put(key, new PermissionSet());

            playerPermissions.get(key).add(perm);
        }

        /**
         * Check if a player has a specific permission
         * @param p The player to check
         * @param perm The permission to check
         * @return hasPermission
         */
        public boolean hasPermission(Player p, String perm) {
            String key = p.getUniqueId().toString();
            return playerPermissions.containsKey(key) && playerPermissions.get(key).contains(perm);
        }
    }

    public static class PermissionSet implements Jsonable {
        public JsonSet<String> permissions = new JsonSet<>();

        public void add(String perm) {
            permissions.add(perm);
        }

        public boolean contains(String perm) {
            return permissions.contains(perm);
        }
    }
}
