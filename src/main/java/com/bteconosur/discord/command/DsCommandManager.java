package com.bteconosur.discord.command;

import java.util.HashMap;
import java.util.Map;

import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.ConsoleLogger;

public class DsCommandManager {

    private static DsCommandManager instance;

    private Map<String, DsCommand> commands = new HashMap<>();

    public DsCommandManager() {
        ConsoleLogger.info(LanguageHandler.getText("discord-command-manager-initializing"));

        addCommand(new TestCommand());
        addCommand(new Test2Command());
        addCommand(new DeleteDsCommand());
        addCommand(new DsExecCommand());
        addCommand(new DsLinkCommand());
    }

    private void addCommand(DsCommand command) {
        commands.put(command.command, command);
        command.registerCommand();
    }

    public DsCommand getCommand(String commandName) {
        return commands.get(commandName);
    }

    public void shutdown() {
        ConsoleLogger.info(LanguageHandler.getText("discord-command-manager-shutting-down"));
    }

    public static DsCommandManager getInstance() {
        if (instance == null) {
            instance = new DsCommandManager();
        }
        return instance;
    }

}
