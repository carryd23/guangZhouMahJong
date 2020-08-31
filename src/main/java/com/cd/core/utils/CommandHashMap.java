package com.cd.core.utils;

import com.cd.core.pojo.Command;

import java.util.HashMap;

public class CommandHashMap extends HashMap<String, Command> {
    private volatile static CommandHashMap commands;

    private CommandHashMap() {}

    public static CommandHashMap newInstance() {
        if(commands == null) {
            synchronized (CommandHashMap.class) {
                if(commands == null) {
                    commands = new CommandHashMap();
                }
            }
        }
        return commands;
    }
}
