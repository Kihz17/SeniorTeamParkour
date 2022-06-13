package com.kihz.commands;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor @Getter
public enum CommandType {
    IN_GAME("/"),
    COMMAND_BLOCK("");

    private final String prefix;
}
