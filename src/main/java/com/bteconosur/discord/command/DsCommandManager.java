package com.bteconosur.discord.command;

import java.util.HashMap;
import java.util.Map;

import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.ConsoleLogger;

/**
 * Gestor de comandos de Discord del plugin.
 * Administra el registro y acceso a comandos slash, comandos de contexto de mensajes
 * y comandos de contexto de usuarios.
 */
public class DsCommandManager {

    private static DsCommandManager instance;

    private Map<String, DsCommand> commands = new HashMap<>();
    private Map<String, DsContextMessageCommand> contextMessageCommands = new HashMap<>();
    private Map<String, DsContextUserCommand> contextUserCommands = new HashMap<>();

    /**
     * Constructor del gestor de comandos de Discord.
     * Inicializa y registra todos los comandos del bot.
     */
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
        addCommand(new DsSchemCommand());
        DsHelpDiscordCommand.addHelpCommand(new DsSchemCommand());
        addCommand(new DsHelpCommand());
        DsHelpDiscordCommand.addHelpCommand(new DsHelpCommand());
        addCommand(new DsOnlineCommand());
        DsHelpDiscordCommand.addHelpCommand(new DsOnlineCommand());

        addCommand(new DsContextDeleteChatCommand());
        addCommand(new DsContextPlayerCommand());
    }

    /**
     * Añade y registra un comando slash en Discord.
     * 
     * @param command Comando a añadir
     */
    private void addCommand(DsCommand command) {
        commands.put(command.command, command);
        command.registerCommand();
    }

    /**
     * Añade y registra un comando de contexto de mensaje en Discord.
     * 
     * @param command Comando de contexto de mensaje a añadir
     */
    private void addCommand(DsContextMessageCommand command) {
        contextMessageCommands.put(command.command, command);
        command.registerCommand();
    }

    /**
     * Añade y registra un comando de contexto de usuario en Discord.
     * 
     * @param command Comando de contexto de usuario a añadir
     */
    private void addCommand(DsContextUserCommand command) {
        contextUserCommands.put(command.command, command);
        command.registerCommand();
    }

    /**
     * Obtiene un comando slash por su nombre.
     * 
     * @param commandName Nombre del comando
     * @return El comando encontrado, o null si no existe
     */
    public DsCommand getCommand(String commandName) {
        return commands.get(commandName);
    }

    /**
     * Obtiene un comando de contexto de mensaje por su nombre.
     * 
     * @param commandName Nombre del comando
     * @return El comando encontrado, o null si no existe
     */
    public DsContextMessageCommand getContextMessageCommand(String commandName) {
        return contextMessageCommands.get(commandName);
    }
    
    /**
     * Obtiene un comando de contexto de usuario por su nombre.
     * 
     * @param commandName Nombre del comando
     * @return El comando encontrado, o null si no existe
     */
    public DsContextUserCommand getContextUserCommand(String commandName) {
        return contextUserCommands.get(commandName);
    }

    /**
     * Obtiene el mapa de todos los comandos de contexto de mensaje.
     * 
     * @return Mapa de comandos de contexto de mensaje
     */
    public Map<String, DsContextMessageCommand> getContextMessageCommands() {
        return contextMessageCommands;
    }
    
    /**
     * Obtiene el mapa de todos los comandos slash.
     * 
     * @return Mapa de comandos slash
     */
    public Map<String, DsCommand> getCommands() {
        return commands;
    }

    /**
     * Cierra el gestor de comandos de Discord.
     */
    public void shutdown() {
        ConsoleLogger.info(LanguageHandler.getText("discord-command-manager-shutting-down"));
    }

    /**
     * Obtiene la instancia singleton del gestor de comandos de Discord.
     * 
     * @return La instancia única de DsCommandManager
     */
    public static DsCommandManager getInstance() {
        if (instance == null) {
            instance = new DsCommandManager();
        }
        return instance;
    }

}
