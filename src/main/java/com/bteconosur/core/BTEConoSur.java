package com.bteconosur.core;

import com.bteconosur.core.chat.ChatService;
import com.bteconosur.core.chat.ChatUtil;
import com.bteconosur.core.command.btecs.BTECSCommand;
import com.bteconosur.core.command.config.GeneralConfigCommand;
import com.bteconosur.core.command.config.ManagerConfigCommand;
import com.bteconosur.core.command.config.ReviewerConfigCommand;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.listener.ChatListener;
import com.bteconosur.core.listener.PlayerJoinListener;
import com.bteconosur.core.listener.PlayerLeaveListener;
import com.bteconosur.core.util.ConsoleLogger;
import com.bteconosur.core.util.PluginRegistry;
import com.bteconosur.db.DBManager;
import com.bteconosur.db.PermissionManager;
import com.bteconosur.db.registry.DiscordInteractionRegistry;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.db.registry.ProyectoRegistry;
import com.bteconosur.db.registry.RangoUsuarioRegistry;
import com.bteconosur.db.registry.TipoUsuarioRegistry;
import com.bteconosur.discord.DiscordManager;
import com.bteconosur.discord.command.DsCommandManager;
import com.bteconosur.discord.util.MessageService;
import com.bteconosur.world.WorldManager;
import com.bteconosur.world.listener.BannedListeners;
import com.bteconosur.world.listener.BuildingListeners;
import com.bteconosur.world.listener.MovingListeners;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.mvplugins.multiverse.core.MultiverseCoreApi;

public final class BTEConoSur extends JavaPlugin {
    private static BTEConoSur instance;

    private static ConsoleLogger consoleLogger;
    private static DiscordManager discordManager;
    private static DsCommandManager dsCommandManager; 
    private static DBManager dbManager;
    private static WorldManager worldManager;
    private static PermissionManager permissionManager;

    private static PlayerRegistry playerRegistry;
    private static ProyectoRegistry proyectoRegistry;
    private static TipoUsuarioRegistry tipoUsuarioRegistry;
    private static RangoUsuarioRegistry rangoUsuarioRegistry;
    private static DiscordInteractionRegistry discordInteractionRegistry;

    private static MultiverseCoreApi multiverseCoreApi;
    private static WorldEditPlugin worldEditPlugin;
    private static LuckPerms luckPermsApi;

    private static YamlConfiguration config;

    @Override
    public void onEnable() {
        // Guardar instancia del plugin
        instance = this;

        try {
            multiverseCoreApi = MultiverseCoreApi.get();
        } catch (IllegalStateException ex) {
            PluginRegistry.disablePlugin("MultiverseCore API no cargada. Asegurar de que MultiverseCore esté instalado y habilitado.");
            return;
        }

        worldEditPlugin = (WorldEditPlugin) this.getServer().getPluginManager().getPlugin("WorldEdit");

        if (worldEditPlugin == null) {
            PluginRegistry.disablePlugin("WorldEdit no encontrado. Asegurar de que WorldEdit esté instalado y habilitado.");
            return;
        }

        try {
            luckPermsApi = LuckPermsProvider.get();
        } catch (IllegalStateException ex) {
            PluginRegistry.disablePlugin("Luckperms API no cargada. Asegurar de que LuckPerms esté instalado y habilitado.");
            return;
        }

        consoleLogger = new ConsoleLogger();
        dbManager = DBManager.getInstance();
        discordManager = DiscordManager.getInstance();
        dsCommandManager = DsCommandManager.getInstance();
        worldManager = new WorldManager(); //TODO: hacer singleton
        

        playerRegistry = PlayerRegistry.getInstance();
        proyectoRegistry = ProyectoRegistry.getInstance();
        tipoUsuarioRegistry = TipoUsuarioRegistry.getInstance();
        rangoUsuarioRegistry = RangoUsuarioRegistry.getInstance();
        discordInteractionRegistry = DiscordInteractionRegistry.getInstance();

        permissionManager = PermissionManager.getInstance();
        

        getServer().getPluginManager().registerEvents(new BuildingListeners(worldManager), this);
        getServer().getPluginManager().registerEvents(new BannedListeners(), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerLeaveListener(), this);
        getServer().getPluginManager().registerEvents(new ChatListener(), this);
        getServer().getPluginManager().registerEvents(new MovingListeners(worldManager), this);
            
        // Registro de comandos
        PluginRegistry.registerCommand(new BTECSCommand());
        PluginRegistry.registerCommand(new ManagerConfigCommand());
        PluginRegistry.registerCommand(new GeneralConfigCommand());
        PluginRegistry.registerCommand(new ReviewerConfigCommand());
        consoleLogger.info("El Plugin se ha activado.");
        
        config = ConfigHandler.getInstance().getConfig();
        if (config.getBoolean("discord-server-start-stop")) ChatService.broadcastEmbed(ChatUtil.getServerStarted());
    }

    @Override
    public void onDisable() {
        
        if (playerRegistry != null) {
            playerRegistry.shutdown();
            playerRegistry = null;
        }

        if (proyectoRegistry != null) {
            proyectoRegistry.shutdown();
            proyectoRegistry = null;
        }

        if (tipoUsuarioRegistry != null) {
            tipoUsuarioRegistry.shutdown();
            tipoUsuarioRegistry = null;
        }

        if (rangoUsuarioRegistry != null) {
            rangoUsuarioRegistry.shutdown();
            rangoUsuarioRegistry = null;
        }

        if (discordInteractionRegistry != null) {
            discordInteractionRegistry.shutdown();
            discordInteractionRegistry = null;
        }
        
        if (permissionManager != null) {
            permissionManager.shutdown();
            permissionManager = null;
        }

        if (dbManager != null) {
            dbManager.shutdown();
            dbManager = null;
        }

        if (worldManager != null) {
            worldManager.shutdown();
            worldManager = null;
        }

        if (dsCommandManager != null) {
            dsCommandManager.shutdown();
            dsCommandManager = null;
        }

        if (discordManager != null) {
            if (config.getBoolean("discord-server-start-stop")) ChatService.broadcastEmbed(ChatUtil.getServerStopped());
            discordManager.shutdown();
            discordManager = null;
        }

        luckPermsApi = null;
          
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

    public static LuckPerms getLuckPermsApi() {
        return luckPermsApi;
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