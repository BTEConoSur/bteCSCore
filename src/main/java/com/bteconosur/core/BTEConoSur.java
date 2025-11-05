package com.bteconosur.core;

import com.bteconosur.core.command.btecs.BTECSCommand;
import com.bteconosur.core.utils.ConsoleLogger;
import com.bteconosur.core.utils.PluginRegistry;
import com.bteconosur.db.DBManager;
import com.bteconosur.discord.DiscordManager;

import org.bukkit.plugin.java.JavaPlugin;

public final class BTEConoSur extends JavaPlugin {
    private static BTEConoSur instance;

    private static ConsoleLogger consoleLogger;
    private static DBManager dbManager;
    private static DiscordManager discordManager;

    @Override
    public void onEnable() {
        // Guardar instancia del plugin
        instance = this;

        consoleLogger = new ConsoleLogger();

        dbManager = new DBManager();

        discordManager = new DiscordManager();

        // Registro de comandos
        PluginRegistry.registerCommand(new BTECSCommand());
        consoleLogger.info("El Plugin se ha activado.");
    }

    @Override
    public void onDisable() {
        if (dbManager != null) {
            dbManager.shutdown();
            dbManager = null;
        }

        if (discordManager != null) {
            discordManager.shutdown();
            discordManager = null;
        }
          
        consoleLogger.info("El Plugin se ha desactivado.");
    }

    public static BTEConoSur getInstance() {
        return instance;
    }

    public static ConsoleLogger getConsoleLogger() {
        return consoleLogger;
    }
}
