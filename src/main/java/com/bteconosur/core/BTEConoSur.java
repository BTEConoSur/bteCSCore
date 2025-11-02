package com.bteconosur.core;

import java.io.Console;
import java.io.IOException;

import com.bteconosur.core.command.btecs.BTECSCommand;
import com.bteconosur.core.utils.ConsoleLogger;
import com.bteconosur.core.utils.PluginRegistry;
import com.bteconosur.db.DBManager;
import com.bteconosur.db.DatabaseTester;

import org.bukkit.plugin.java.JavaPlugin;

import com.bteconosur.core.config.ConfigFile;

public final class BTEConoSur extends JavaPlugin {
    private static BTEConoSur instance;

    private static ConsoleLogger consoleLogger;
    private static DBManager dbManager;

    @Override
    public void onEnable() {
        // Guardar instancia del plugin
        instance = this;

        consoleLogger = new ConsoleLogger();

        dbManager = new DBManager();

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
          
        consoleLogger.info("El Plugin se ha desactivado.");
    }

    public static BTEConoSur getInstance() {
        return instance;
    }

    public static ConsoleLogger getConsoleLogger() {
        return consoleLogger;
    }
}
