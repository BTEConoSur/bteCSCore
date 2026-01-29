package com.bteconosur.core.command.btecs.test;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.util.DiscordLogger;
import com.bteconosur.db.registry.PaisRegistry;

public class TestDiscordLogger extends BaseCommand {

    public TestDiscordLogger() {
        super("discordlogger", "Comando de prueba para el logger de discord.", null, CommandMode.BOTH); //TODO: Probar dependencia de permisos
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        PaisRegistry paisRegistry = PaisRegistry.getInstance();
        DiscordLogger.countryLog("Este es un mensaje de prueba de lista.", paisRegistry.getList());
        
        DiscordLogger.globalLog("Este es un mensaje de prueba global.");
        DiscordLogger.staffLog("Este es un mensaje de prueba de staff.");
        //DiscordLogger.staffLog("Este es un mensaje de prueba de staff de un pais.", paisRegistry.getChile());

        DiscordLogger.notifyManagers("Este es un mensaje de prueba para managers.", paisRegistry.getArgentina());
        DiscordLogger.notifyReviewers("Este es un mensaje de prueba para reviewers.", paisRegistry.getArgentina());
        return true;
    }  

}
    