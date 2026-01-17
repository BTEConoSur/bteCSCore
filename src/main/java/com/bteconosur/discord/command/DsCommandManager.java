package com.bteconosur.discord.command;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.util.ConsoleLogger;

public class DsCommandManager {

    private static DsCommandManager instance;

    private final YamlConfiguration lang;

    private Map<String, DsCommand> commands = new HashMap<>();

    public DsCommandManager() {
        ConfigHandler configHandler = ConfigHandler.getInstance();
        lang = configHandler.getLang();

        ConsoleLogger.info(lang.getString("discord-command-manager-initializing"));

        addCommand(new TestCommand());
        addCommand(new Test2Command());
        addCommand(new DeleteDsCommand());
        addCommand(new DsExecCommand());
    }

    private void addCommand(DsCommand command) {
        commands.put(command.command, command);
        command.registerCommand();
    }

    public DsCommand getCommand(String commandName) {
        return commands.get(commandName);
    }

    public void shutdown() {
        ConsoleLogger.info(lang.getString("discord-command-manager-shutting-down"));
    }

    public static DsCommandManager getInstance() {
        if (instance == null) {
            instance = new DsCommandManager();
        }
        return instance;
    }

}
