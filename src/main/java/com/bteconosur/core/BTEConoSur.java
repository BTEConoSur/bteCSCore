package com.bteconosur.core;

import com.bteconosur.core.command.btecs.BTECSCommand;
import com.bteconosur.core.listener.PlayerJoinListener;
import com.bteconosur.core.listener.PlayerLeaveListener;
import com.bteconosur.core.util.ConsoleLogger;
import com.bteconosur.core.util.PluginRegistry;
import com.bteconosur.db.DBManager;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.discord.DiscordManager;
import com.bteconosur.world.WorldManager;
import com.bteconosur.world.listener.BannedListeners;
import com.bteconosur.world.listener.BuildingListeners;
import com.bteconosur.world.listener.MovingListeners;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;

import org.bukkit.plugin.java.JavaPlugin;
import org.mvplugins.multiverse.core.MultiverseCoreApi;

public final class BTEConoSur extends JavaPlugin {
    private static BTEConoSur instance;

    private static ConsoleLogger consoleLogger;
    private static DiscordManager discordManager;
    private static DBManager dbManager;
    private static WorldManager worldManager;

    private static PlayerRegistry playerRegistry;;

    private static MultiverseCoreApi multiverseCoreApi;
    private static WorldEditPlugin worldEditPlugin;

    @Override
    public void onEnable() {
        // Guardar instancia del plugin
        instance = this;

        multiverseCoreApi = MultiverseCoreApi.get();
        worldEditPlugin = (WorldEditPlugin) this.getServer().getPluginManager().getPlugin("WorldEdit");

        consoleLogger = new ConsoleLogger();
        dbManager = new DBManager();
        discordManager = new DiscordManager();
        worldManager = new WorldManager();

        playerRegistry = PlayerRegistry.getInstance();

        getServer().getPluginManager().registerEvents(new BuildingListeners(worldManager), this);
        getServer().getPluginManager().registerEvents(new BannedListeners(), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerLeaveListener(), this);
        getServer().getPluginManager().registerEvents(new MovingListeners(worldManager), this);
            
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

        if (playerRegistry != null) {
            playerRegistry.shutdown();
            playerRegistry = null;
        }
          
        consoleLogger.info("El Plugin se ha desactivado.");
    }

    public static BTEConoSur getInstance() {
        return instance;
    }

    public static ConsoleLogger getConsoleLogger() {
        return consoleLogger;
    }

    public static MultiverseCoreApi getMultiverseCoreApi() {
        return multiverseCoreApi;
    }

    public static WorldEditPlugin getWorldEditPlugin() {
        return worldEditPlugin;
    }

    public static DiscordManager getDiscordManager() {
        return discordManager;
    }   

    public static WorldManager getWorldManager() {
        return worldManager;
    }
}

// TODO: Todos los managers con getInstance() statico
// TODO: Revisar llamadas a config en constructores.