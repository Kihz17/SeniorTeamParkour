package com.kihz.mechanics.system;

import com.kihz.utils.Utils;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class GameMechanic implements Listener {

    /**
     * Calls when the server starts.
     */
    public void onEnable() {

    }

    /**
     * Calls when the server shuts down.
     */
    public void onDisable() {
        // NOTE: We should not put bukkit schedulers within this method because the logic inside will not be run.
    }

    /**
     * Calls when a player enters the world. (After authentication, ban checks, etc.)
     * @param player The player who joined.
     */
    public void onJoin(Player player) {

    }

    /**
     * Calls when a player disconnects.
     * We use this instead of listening for a bukkit event since multiple event can call.
     */
    public void onQuit(Player player) {

    }

    /**
     * Get the name of this mechanic.
     */
    public String getName() {
        return Utils.getSimpleName(this);
    }
}
