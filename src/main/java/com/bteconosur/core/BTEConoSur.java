package com.bteconosur.core;

import java.io.IOException;

import com.bteconosur.core.command.btecs.BTECSCommand;
import com.bteconosur.core.utils.PluginRegistry;
import org.bukkit.plugin.java.JavaPlugin;

import com.bteconosur.core.config.ConfigFile;

public final class BTEConoSur extends JavaPlugin {
    private static BTEConoSur instance;

    @Override
    public void onEnable() {
        // Guardar instancia del plugin
        instance = this;

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
}
