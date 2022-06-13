package com.kihz.gui;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.function.Function;

@AllArgsConstructor @Getter
public enum GUIType {
    ;

    private final Function<Player, GUI> maker;

    /**
     * Construct this GUIType for a given player.
     * @param p The player to construct the GUI for
     */
    public void construct(Player p) {
        getMaker().apply(p);
    }
}
