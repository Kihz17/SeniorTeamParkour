package com.kihz.events;

import com.kihz.item.ItemWrapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@AllArgsConstructor @Getter
public class ItemGenerateEvent extends Event {
    private ItemWrapper itemWrapper;

    @Getter
    private static final HandlerList handlerList = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return getHandlerList();
    }
}
