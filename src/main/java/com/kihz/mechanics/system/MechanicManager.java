package com.kihz.mechanics.system;

import com.kihz.Core;
import com.kihz.commands.Commands;
import com.kihz.events.MechanicRegisterEvent;
import com.kihz.gui.GUIManager;
import com.kihz.mechanics.Callbacks;
import com.kihz.mechanics.parkour.Parkours;
import com.kihz.mechanics.Permissions;
import com.kihz.mechanics.Restrictions;
import com.kihz.mechanics.holograms.Holograms;
import com.kihz.utils.ReflectionUtil;
import com.kihz.utils.Utils;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class MechanicManager implements Listener {
    @Getter
    private static List<GameMechanic> gameMechanics = new ArrayList<>();
    private static final Map<Class<? extends GameMechanic>, GameMechanic> INSTANCE_MAP = new HashMap<>();

    @EventHandler(priority = EventPriority.HIGH)
    public void onJoin(PlayerJoinEvent evt) {
        Core.getOnlineAsync().add(evt.getPlayer());
        fireMechanicEvent(GameMechanic::onJoin, evt.getPlayer(), "onJoin");
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent evt) {
        if(!Core.getOnlineAsync().remove(evt.getPlayer())) // Already handled the player leaving
            return;

        fireMechanicEvent(GameMechanic::onQuit, evt.getPlayer(), "onQuit");
        Bukkit.getPluginManager().callEvent(evt);
    }

    @EventHandler
    @SuppressWarnings("null")
    public void onPlayerKick(PlayerKickEvent evt) {
        if(!Core.getOnlineAsync().remove(evt.getPlayer())) // Already handled the player leaving
            return;

        fireMechanicEvent(GameMechanic::onQuit, evt.getPlayer(), "onQuit");
        Bukkit.getPluginManager().callEvent(evt);
    }

    /**
     * Register all mechanics
     */
    private static void registerDefault() {
        addMechanic(Permissions.class); // Important that this loads first because other mechanics may rely on permissions
        addMechanic(GUIManager.class);
        addMechanic(Holograms.class);
        addMechanic(Callbacks.class);
        addMechanic(Parkours.class);
        addMechanic(Restrictions.class);
        addMechanic(Commands.class);

        Bukkit.getPluginManager().callEvent(new MechanicRegisterEvent()); // Tell mechanics to register their sub-mechanics
    }

    @EventHandler
    public void onPluginDisable(PluginDisableEvent evt) {
        if(evt.getPlugin() == Core.getInstance()) {
            Core.logInfo("Shutting down Parkour plugin...");
            Core.onShutdown();
        }
    }

    /**
     * Register a game mechanic class.
     * @param mechanicClass The mechanic class to register.
     */
    public static void addMechanic(Class<? extends GameMechanic> mechanicClass) {
        addMechanic(ReflectionUtil.construct(mechanicClass));
    }

    /**
     * Register a game mechanic, if it can be registered on this build.
     * @param m The mechanic instance to add.
     */
    public static void addMechanic(GameMechanic m) {
        Utils.verify(getInstance(m.getClass()) == null, "%s is already registered!", m.getName());

        getGameMechanics().add(m);
        INSTANCE_MAP.put(m.getClass(), m);
        Bukkit.getPluginManager().registerEvents(m, Core.getInstance());
    }

    /**
     * Get a game mechanic's instance from its class.
     * @param mechanic The mechanic to get the instance of.
     * @return mechanicInstance
     */
    @SuppressWarnings("unchecked")
    public static <T extends GameMechanic> T getInstance(Class<T> mechanic) {
        return (T) INSTANCE_MAP.get(mechanic);
    }

    /**
     * Fire a mechanic event.
     * @param handle    The event to fire.
     * @param value     The value to pass to the event.
     * @param eventName The name of the event to display if there's an error.
     */
    public static <T> void fireMechanicEvent(BiConsumer<GameMechanic, T> handle, T value, String eventName) {
        fireMechanicEvent(m -> handle.accept(m, value), eventName);
    }

    /**
     * Fire a mechanic event.
     * @param handle    The event to handle.
     * @param eventName The name of the event to display if there's an error.
     */
    public static void fireMechanicEvent(Consumer<GameMechanic> handle, String eventName) {
        getGameMechanics().forEach(m -> {
            try {
                handle.accept(m);
            } catch (Exception e) {
                e.printStackTrace();
                Core.logInfo("Error while running %s in %s.", eventName, m.getName());
            }
        });
    }

    /**
     * Registers all game mechanics.
     */
    public static void registerMechanics() {
        // Clear all scoreboard data.
        Scoreboard sb = Bukkit.getScoreboardManager().getMainScoreboard();
        sb.getTeams().forEach(Team::unregister); // Remove all teams
        sb.getObjectives().forEach(Objective::unregister); // Remove all objectives.

        Core.logInfo("Registering Game Mechanics...");
        Bukkit.getPluginManager().registerEvents(new MechanicManager(), Core.getInstance());

        // Register all mechanics here, in order of startup:
        registerDefault();
        fireMechanicEvent(GameMechanic::onEnable, "onEnable");
        Core.logInfo("Registered %d game mechanics.", getGameMechanics().size());
    }
}
