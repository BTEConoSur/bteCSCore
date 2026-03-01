package com.bteconosur.discord.command;

import java.util.HashMap;
import java.util.Map;

import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.ConsoleLogger;

public class DsCommandManager {

    private static DsCommandManager instance;

    private Map<String, DsCommand> commands = new HashMap<>();
    private Map<String, DsContextMessageCommand> contextMessageCommands = new HashMap<>();
    private Map<String, DsContextUserCommand> contextUserCommands = new HashMap<>();

    public DsCommandManager() {
        ConsoleLogger.info(LanguageHandler.getText("discord-command-manager-initializing"));

        addCommand(new DeleteDsCommand());
        addCommand(new DsExecCommand());
        addCommand(new DsLinkCommand());
        DsHelpDiscordCommand.addHelpCommand(new DsLinkCommand());
        addCommand(new DsPlayerCommand());
        DsHelpDiscordCommand.addHelpCommand(new DsPlayerCommand());
        addCommand(new DsProyectoCommand());
        DsHelpDiscordCommand.addHelpCommand(new DsProyectoCommand());
        addCommand(new DsHelpCommand());
        DsHelpDiscordCommand.addHelpCommand(new DsHelpCommand());
        addCommand(new DsOnlineCommand());
        DsHelpDiscordCommand.addHelpCommand(new DsOnlineCommand());

        addCommand(new DsContextDeleteChatCommand());
        addCommand(new DsContextPlayerCommand());
    }

    private void addCommand(DsCommand command) {
        commands.put(command.command, command);
        command.registerCommand();
    }

    private void addCommand(DsContextMessageCommand command) {
        contextMessageCommands.put(command.command, command);
        command.registerCommand();
    }

    private void addCommand(DsContextUserCommand command) {
        contextUserCommands.put(command.command, command);
        command.registerCommand();
    }

    public DsCommand getCommand(String commandName) {
        return commands.get(commandName);
    }

    public DsContextMessageCommand getContextMessageCommand(String commandName) {
        return contextMessageCommands.get(commandName);
    }
    
    public DsContextUserCommand getContextUserCommand(String commandName) {
        return contextUserCommands.get(commandName);
    }

    public Map<String, DsContextMessageCommand> getContextMessageCommands() {
        return contextMessageCommands;
    }
    
    public Map<String, DsCommand> getCommands() {
        return commands;
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
