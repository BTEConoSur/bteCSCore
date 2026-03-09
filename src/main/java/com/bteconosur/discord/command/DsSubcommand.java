package com.bteconosur.discord.command;

import java.util.Collection;

import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.ConsoleLogger;

import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

/**
 * Clase base abstracta para subcomandos de Discord.
 * Los subcomandos son comandos anidados dentro de un comando principal.
 * Heredan de DsCommand pero se registran de manera diferente.
 */
public abstract class DsSubcommand extends DsCommand {

    private String parentCommand;

    /**
     * Constructor de un subcomando de Discord.
     * 
     * @param command Nombre del subcomando
     * @param description Descripción del subcomando
     * @param options Opciones del subcomando
     */
    public DsSubcommand(String command, String description, Collection<OptionData> options) {
        super(command, description, options, null, null);
    }

    /**
     * Genera los datos del subcomando para registrarlo en Discord.
     * 
     * @return Los datos del subcomando configurados
     */
    @SuppressWarnings("null")
    public SubcommandData geSubcommandData() {
        SubcommandData subcommandData = new SubcommandData(command, description);
        if (options != null && !options.isEmpty()) subcommandData.addOptions(options);
        return subcommandData;
    }

    /**
     * Obtiene el nombre del comando principal al que pertenece este subcomando.
     * 
     * @return El nombre del comando principal
     */
    public String getParentCommand() {
        return parentCommand;
    }

    /**
     * Establece el nombre del comando principal al que pertenece este subcomando.
     * 
     * @param parentCommand Nombre del comando principal
     */
    public void setParentCommand(String parentCommand) {
        this.parentCommand = parentCommand;
    }

    /**
     * Los subcomandos no se pueden registrar directamente.
     * Este método emite una advertencia si se intenta registrar un subcomando de forma independiente.
     */
    @Override
    public void registerCommand() {
        ConsoleLogger.warn(LanguageHandler.getText("ds-error.subcommand-cant-register").replace("%subcommand%", command)    );
    }

}
