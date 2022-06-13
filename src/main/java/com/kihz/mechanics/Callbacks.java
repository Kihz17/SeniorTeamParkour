package com.kihz.mechanics;

import com.kihz.Core;
import com.kihz.gui.GUI;
import com.kihz.gui.GUIManager;
import com.kihz.mechanics.system.GameMechanic;
import com.kihz.utils.Utils;
import lombok.AllArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class Callbacks extends GameMechanic {
    private static Map<Player, Map<ListenerType, Listener<?>>> listeners = new HashMap<>();

    public static String CANCEL_MESSAGE = ChatColor.RED.toString() + ChatColor.BOLD + "CANCELLED";
    private static final String CONFIRM_BUTTON_MESSAGE = "confirm";
    private static final String CANCEL_BUTTON_MESSAGE  = "cancel";

    /**
     * Prompt the player with confirmation of an action, and allows supplying a default fail message.
     * @param player
     * @param confirm
     * @param failMessage
     */
    public static void promptConfirm(Player player, Runnable confirm, String failMessage) {
        promptConfirm(player, confirm, () -> player.sendMessage(ChatColor.RED + failMessage + " - " + CANCEL_MESSAGE));
    }

    /**
     * Prompt the player for confirmation of an action.
     * @param player
     * @param confirm
     */
    public static void promptConfirm(Player player, Runnable confirm) {
        promptConfirm(player, confirm, () -> player.sendMessage(CANCEL_MESSAGE));
    }

    /**
     * Prompt the player a yes or no listener with clickable buttons.
     * @param player
     * @param confirm
     * @param cancel
     */
    public static void promptConfirm(Player player, Runnable confirm, Runnable cancel) {
        promptConfirm(player, confirm, cancel, "CONFIRM", "CANCEL");
    }

    /**
     * Prompt the player a yes / no listener with clickable buttons.
     * @param player
     * @param accept
     * @param deny
     * @param yes
     * @param no
     */
    public static void promptConfirm(Player player, Runnable accept, Runnable deny, String yes, String no) {
        Component yesComponent = Component.text("          [" + yes + "]", NamedTextColor.GREEN, TextDecoration.BOLD)
                .hoverEvent(Component.text("Click here to " + yes.toLowerCase() + ".", NamedTextColor.GREEN))
                .clickEvent(ClickEvent.runCommand(CONFIRM_BUTTON_MESSAGE));

        Component noComponent = Component.text("      [" + no + "]", NamedTextColor.RED, TextDecoration.BOLD)
                .hoverEvent(Component.text("Click here to " + no.toLowerCase() + ".", NamedTextColor.RED))
                .clickEvent(ClickEvent.runCommand(CANCEL_BUTTON_MESSAGE));

        player.sendMessage(yesComponent);
        player.sendMessage(noComponent);

        listenForChat(player, message -> {
            boolean success = message.equalsIgnoreCase(CONFIRM_BUTTON_MESSAGE) || message.equals("yes") || message.equalsIgnoreCase("y");
            if (success) {
                accept.run();
            } else {
                deny.run();
            }
        }, deny);
    }

    /**
     * Listen for a number.
     * @param player   - The player to prompt the number for.
     * @param listener - Handler for their response.
     */
    public static void listenForNumber(Player player, Consumer<Integer> listener) {
        listenForNumber(player, listener, () -> player.sendMessage(CANCEL_MESSAGE));
    }

    /**
     * Listen for a number.
     * @param player   - The player to apply the prompt for.
     * @param listener - The behavior for an integer response.
     * @param fail     - The handler for if this callback is cancelled.
     */
    public static void listenForNumber(Player player, Consumer<Integer> listener, Runnable fail) {
        listenForNumber(player, Integer.MIN_VALUE, Integer.MAX_VALUE, listener, fail);
    }

    /**
     * Listen for a number.
     * @param player  - The player to listen to.
     * @param min     - The minimum response value.
     * @param max     - The maximum response value.
     * @param handler - Handles a numeric response that fits the given specifications.
     */
    public static void listenForNumber(Player player, int min, int max, Consumer<Integer> handler) {
        listenForNumber(player, min, max, handler, () -> player.sendMessage(CANCEL_MESSAGE));
    }

    /**
     * Listen for a number.
     * @param player      - The player to listen to.
     * @param min         - The minimum response value.
     * @param max         - The maximum response value./
     * @param handler     - Handles a valid numeric response.
     * @param failMessage - The cancel message, for if
     */
    public static void listenForNumber(Player player, int min, int max, Consumer<Integer> handler, String failMessage) {
        listenForNumber(player, min, max, handler, () -> player.sendMessage(ChatColor.RED + failMessage + " - " + CANCEL_MESSAGE));
    }

    /**
     * Listen for a number.
     * @param player - The player to listen to.
     * @param min    - The minimum response value.
     * @param max    - The maximum response value.
     * @param handle - The handler for if a valid value is supplied.
     * @param fail   - The handler for if this is cancelled.
     */
    public static void listenForNumber(Player player, int min, int max, Consumer<Integer> handle, Runnable fail) {
        listenForChat(player, m -> {
            try {
                int num = Integer.parseInt(m);
                if (num > max || num < min) {
                    player.sendMessage(ChatColor.RED.toString() + num + " is not in range [" + min + "," + max + "].");
                    fail.run();
                    return;
                }

                handle.accept(num);
            } catch (NumberFormatException nfe) {
                if (!CANCEL_MESSAGE.equalsIgnoreCase(m) && !m.equalsIgnoreCase("c"))
                    player.sendMessage(ChatColor.RED + "'" + m + "' is not a valid number.");
                fail.run();
            }
        }, fail);
    }

    /**
     * Listens for the player's next chat message.
     * @param player
     * @param message
     */
    public static void listenForChat(Player player, Consumer<String> message) {
        listenForChat(player, message, "");
    }

    /**
     * Listen for a player's chat response, with a cancel message.
     * @param player
     * @param message
     * @param cancelMessage
     */
    public static void listenForChat(Player player, Consumer<String> message, String cancelMessage) {
        listenForChat(player, message, () -> player.sendMessage(ChatColor.RED + cancelMessage + " - " + CANCEL_MESSAGE));
    }

    /**
     * Listens for the next chat message the player sends. Handles Async scheduling.
     * @param player
     * @param cb
     * @param fail
     */
    public static void listenForChat(Player player, Consumer<String> cb, Runnable fail) {
        listen(player, ListenerType.CHAT, chat ->
                Bukkit.getScheduler().runTask(Core.getInstance(), () -> cb.accept(((String) chat).trim())), fail);
    }

    /**
     * Gets the next entity the player clicks.
     * @param player
     * @param click
     */
    public static void selectEntity(Player player, Consumer<Entity> click) {
        selectEntity(player, click, () -> player.sendMessage(CANCEL_MESSAGE));
    }

    /**
     * Get the next mob the player clicks.
     * @param player
     * @param click
     * @param fail
     */
    public static void selectEntity(Player player, Consumer<Entity> click, Runnable fail) {
        listen(player, ListenerType.ENTITY, click, fail);
    }

    /**
     * Apply a listener to the player. If one already exists of the same type, it will be cancelled.
     * @param player
     * @param type
     * @param callback
     * @param failCallback
     */
    @SuppressWarnings({"unchecked", "ConstantConditions"})
    private static <T> void listen(Player player, ListenerType type,  Consumer<T> callback, Runnable failCallback) {
        if (hasListener(player, type)) // Fail any existing listeners of this type.
            getListener(player, type).fail();

        GUI gui = GUIManager.getGUI(player);
        if (gui != null) // Don't allow the previous GUI to open, since we're giving input.
            gui.setParent(true);

        Bukkit.getScheduler().runTask(Core.getInstance(), () -> player.closeInventory()); // Close the player's open inventory.
        listeners.putIfAbsent(player, new HashMap<>());
        listeners.get(player).put(type, new Listener(o -> {
            if (callback != null)
                callback.accept((T) o);
            if (gui != null && !Utils.hasOpenInventory(player))
                gui.open();
        }, () -> {
            if (failCallback != null)
                failCallback.run();
            if (gui != null && !Utils.hasOpenInventory(player))
                gui.open();
        }));
    }

    /**
     * Fire a listener callback, and unregister the listener.
     * Returns whether or not a listener was accepted.
     *
     * @param player
     * @param type
     * @param obj
     */
    @SuppressWarnings("unchecked")
    public static boolean accept(Player player, ListenerType type, Object obj) {
        if (!hasListener(player, type))
            return false; // If there is no listener, or the player is sharding.

        ((Listener) listeners.get(player).remove(type)).accept(obj);
        return true;
    }

    /**
     * Cancel any listener of the given type.
     * @param player
     * @param type
     */
    public static void cancel(Player player, ListenerType type) {
        if (hasListener(player, type))
            listeners.get(player).remove(type).fail();
    }

    /**
     * Remove the listener of this type for the given player without calling the "fail" callback.
     * Used in circumstances where that could cause the callback to trigger after we've determined it shouldn't.
     * Such as: On fail -> kick player for afking, but if they don't respond it will kick them for another message,
     * but by kicking them it cancels the chat callback listener and will kick them for the wrong reason.
     *
     * @param player
     * @param type
     */
    public static void unsafeCancel(Player player, ListenerType type) {
        listeners.get(player).remove(type);
    }

    /**
     * Get any listener that's currently listening on the player, if any.
     * @param player
     * @param type
     * @return
     */
    private static Listener getListener(Player player, ListenerType type) {
        return listeners.get(player) != null ? listeners.get(player).get(type) : null;
    }

    /**
     * Does the player have any listeners?
     * @param player
     * @return hasListener
     */
    public static boolean hasListener(Player player) {
        for (ListenerType type : ListenerType.values())
            if (hasListener(player, type))
                return true;
        return false;
    }

    /**
     * Is this player currently being listened on?
     * @param player
     * @param type
     * @return
     */
    public static boolean hasListener(Player player, ListenerType type) {
        return getListener(player, type) != null;
    }

    public enum ListenerType {
        CHAT, // From a chat message
        ENTITY // From clicking on an entity.
    }

    @AllArgsConstructor
    private static class Listener<T> {
        private Consumer<T> success;
        private Runnable fail;

        /**
         * Calls on callback success
         */
        public void accept(T value) {
            success.accept(value);
        }

        /**
         * Calls on callback failure.
         */
        public void fail() {
            fail.run();
        }
    }

    @Override
    public void onQuit(Player player) {
        cancelCallbacks(player);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onInventoryClick(InventoryClickEvent evt) { // Prevent interacting with the inventory again. (Prevent dupes and other exploits.)
        boolean cancel = Utils.hasOpenInventory(evt.getWhoClicked()) && hasListener((Player) evt.getWhoClicked());
        if (cancel) {
            evt.setCancelled(true);
            Core.logInfo("%s may have tried to dupe. (%s)", evt.getWhoClicked().getName(), evt.getInventory().toString());
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onInventoryOpen(InventoryOpenEvent evt) { // Prevent inventory during callbacks.
        evt.setCancelled(hasListener((Player) evt.getPlayer()));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST) // Prevent commands during callbacks.
    public void onCommandPreProcess(PlayerCommandPreprocessEvent evt) {
        evt.setCancelled(hasListener(evt.getPlayer()));
    }

    // Callback handlers.
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent evt) {
        String message = evt.getMessage();
        boolean handle = accept(evt.getPlayer(), ListenerType.CHAT, evt.getMessage());
        if (!handle && (message.equalsIgnoreCase(CONFIRM_BUTTON_MESSAGE) || message.equalsIgnoreCase(CANCEL_BUTTON_MESSAGE))) {
            evt.getPlayer().sendMessage(ChatColor.GRAY + "You do not have an active prompt.");
            evt.setCancelled(true);
            return;
        }

        evt.setCancelled(handle); // Handles chat callbacks.
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onInteract(PlayerInteractEntityEvent evt) {
        evt.setCancelled(accept(evt.getPlayer(), ListenerType.ENTITY, evt.getRightClicked())); // Handles entity click callbacks.
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onInteract(PlayerInteractAtEntityEvent evt) {
        evt.setCancelled(accept(evt.getPlayer(), ListenerType.ENTITY, evt.getRightClicked())); // Handles entity click callbacks.
    }

    /**
     * Cancel all callbacks for a player.
     * @param player
     */
    public static void cancelCallbacks(Player player) {
        Arrays.stream(ListenerType.values()).forEach(type -> cancel(player, type)); // Cancel all listeners.
    }
}
