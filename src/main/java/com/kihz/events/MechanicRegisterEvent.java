package com.kihz.events;

import com.kihz.mechanics.system.GameMechanic;
import com.kihz.mechanics.system.MechanicManager;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.stream.Stream;

public class MechanicRegisterEvent extends Event {
    @Getter private static final HandlerList handlerList = new HandlerList();

    /**
     * Register a mechanic
     * @param mechanic The mechanic to register
     */
    public void register(Class<? extends GameMechanic> mechanic) {
        MechanicManager.addMechanic(mechanic);
    }

    /**
     * Register mechanics.
     * @param mechanics The mechanics to register
     */
    public void register(GameMechanic... mechanics) {
        Stream.of(mechanics).forEach(MechanicManager::addMechanic);
    }

    @Override
    public HandlerList getHandlers() {
        return getHandlerList();
    }
}
