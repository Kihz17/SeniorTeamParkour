package com.kihz;

import com.kihz.mechanics.system.GameMechanic;
import com.kihz.utils.Utils;
import com.kihz.mechanics.system.MechanicManager;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;


public class Core extends JavaPlugin {
    @Getter private static Core instance;
    @Getter private static boolean hasShutdown;
    @Getter private static Set<Player> onlineAsync = new CopyOnWriteArraySet<>();

    @Override
    public void onEnable() {
        instance = this;

        try {
            if(getFile().exists())
                makeFolder("");
        }
         catch (Exception e) {
            Core.logInfo("Failed to create plugin folder");
         }

        MechanicManager.registerMechanics();
    }

    /**
     * Handles mechanics on shutdown.
     */
    public static void onShutdown() {
        if (hasShutdown)
            return;

        hasShutdown = true;

        // We may need to delay this to ensure that players are removed
        logInfo("Unloading " + Constants.PLUGIN_NAME + " Game Mechanics...");
        MechanicManager.fireMechanicEvent(GameMechanic::onDisable, "onDisable");
        logInfo("All game mechanics have been unloaded.");
    }

    /**
     * Log information to the console.
     * @param message The message to log.
     */
    public static void logInfo(String message) {
        Bukkit.getLogger().info("[" + Constants.PLUGIN_NAME + "] " + message);
    }

    /**
     * Log information to the console.
     * @param message The message to log.
     * @param args    The arguments to format the message with.
     */
    public static void logInfo(String message, Object... args) {
        logInfo(Utils.formatColor(message, args));
    }

    /**
     * Creates a folder if it does not exist.
     * @param folder The name of the folder to make.
     */
    public static File makeFolder(String folder) {
        File dir = getFile(folder + "/");
        Utils.createDirectory(dir);
        return dir;
    }

    /**
     * Returns a File in the data storage folder with the given name.
     * @param name The name of the file to get.
     * @return File
     */
    public static File getFile(String name) {
        return new File(Core.getInstance().getDataFolder(), name);
    }

    /**
     * Returns a file in the data storage folder with the given name. Creates the file if it does not exist.
     * @param name The name of the file to get or create.
     * @return file
     */
    public static File getCreateFile(String name) {
        File file = getFile(name);
        Utils.createFile(file);
        return file;
    }

    /**
     * Gets a file from our spigot directory.
     * @param name The name of the file to get.
     * @return file
     */
    public static File getSpigotFile(String name) {
        return new File(name);
    }

    /**
     * Gets the server's root directory.
     * @return rootFolder
     */
    public static File getServerRoot() {
        return new File("./");
    }

    /**
     * Load a resource from the jar file.
     * @param fileName The name of the resource file to load.
     * @return resource
     */
    public static InputStream loadResource(String fileName) {
        return getInstance().getResource(fileName);
    }


    /**
     * Get this plugin's file location. This is a method because the super method is protected, while we want this to be public.
     * @return fileLocation
     */
    @NonNull
    public File getFile() {
        return super.getFile();
    }
}
