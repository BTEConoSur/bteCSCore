package com.bteconosur.core;

import com.bteconosur.core.command.btecs.BTECSCommand;
import com.bteconosur.core.listener.PlayerJoinListener;
import com.bteconosur.core.util.ConsoleLogger;
import com.bteconosur.core.util.PluginRegistry;
import com.bteconosur.db.DBManager;
import com.bteconosur.discord.DiscordManager;
import com.bteconosur.world.WorldManager;
import com.bteconosur.world.listener.BannedListeners;
import com.bteconosur.world.listener.BuildingListeners;

import org.bukkit.plugin.java.JavaPlugin;

public final class BTEConoSur extends JavaPlugin {
    private static BTEConoSur instance;

    private static ConsoleLogger consoleLogger;
    private static DBManager dbManager;
    private static DiscordManager discordManager;
    private static WorldManager worldManager;

    @Override
    public void onEnable() {
        // Guardar instancia del plugin
        instance = this;

        consoleLogger = new ConsoleLogger();
        dbManager = new DBManager();
        discordManager = new DiscordManager();
        worldManager = new WorldManager();

        getServer().getPluginManager().registerEvents(new BuildingListeners(worldManager, dbManager), this);
        getServer().getPluginManager().registerEvents(new BannedListeners(), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(dbManager), this);
            
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

        if (worldManager != null) {
            worldManager.shutdown();
            worldManager = null;
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
