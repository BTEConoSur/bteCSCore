package com.bteconosur.core;

import java.io.Console;
import java.io.IOException;

import com.bteconosur.core.command.btecs.BTECSCommand;
import com.bteconosur.core.utils.ConsoleLogger;
import com.bteconosur.core.utils.PluginRegistry;
import org.bukkit.plugin.java.JavaPlugin;

import com.bteconosur.core.config.ConfigFile;

public final class BTEConoSur extends JavaPlugin {
    private static BTEConoSur instance;

    private static ConsoleLogger consoleLogger;

    @Override
    public void onEnable() {
        // Guardar instancia del plugin
        instance = this;

        consoleLogger = new ConsoleLogger();

        // Registro de comandos
        PluginRegistry.registerCommand(new BTECSCommand());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static BTEConoSur getInstance() {
        return instance;
    }

    public static ConsoleLogger getConsoleLogger() {
        return consoleLogger;
    }
}
