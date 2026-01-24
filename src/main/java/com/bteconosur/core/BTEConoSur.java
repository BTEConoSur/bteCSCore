package com.bteconosur.core;

import com.bteconosur.core.chat.GlobalChatService;
import com.bteconosur.core.chat.ChatUtil;
import com.bteconosur.core.command.btecs.BTECSCommand;
import com.bteconosur.core.command.chat.ChatCommand;
import com.bteconosur.core.command.config.GeneralConfigCommand;
import com.bteconosur.core.command.config.LinkCommand;
import com.bteconosur.core.command.config.NicknameCommand;
import com.bteconosur.core.command.config.PromoteCommand;
import com.bteconosur.core.command.config.UnlinkCommand;
import com.bteconosur.core.command.crud.CrudCommand;
import com.bteconosur.core.command.manager.ManagerCommand;
import com.bteconosur.core.command.pais.PaisPrefixCommand;
import com.bteconosur.core.command.project.ProjectCommand;
import com.bteconosur.core.command.reviewer.ReviewerCommand;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.listener.ChatListener;
import com.bteconosur.core.listener.PlayerJoinListener;
import com.bteconosur.core.listener.PlayerLeaveListener;
import com.bteconosur.core.util.ConsoleLogger;
import com.bteconosur.core.util.DiscordLogger;
import com.bteconosur.core.util.HeadDBUtil;
import com.bteconosur.core.util.PluginRegistry;
import com.bteconosur.db.DBManager;
import com.bteconosur.db.PermissionManager;
import com.bteconosur.db.registry.InteractionRegistry;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.db.registry.ProyectoRegistry;
import com.bteconosur.db.registry.RangoUsuarioRegistry;
import com.bteconosur.db.registry.TipoUsuarioRegistry;
import com.bteconosur.discord.DiscordManager;
import com.bteconosur.discord.command.DsCommandManager;
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

    private static DiscordManager discordManager;
    private static DsCommandManager dsCommandManager; 
    private static DBManager dbManager;
    private static WorldManager worldManager;
    private static PermissionManager permissionManager;
    private static ProjectManager projectManager;

    private static PlayerRegistry playerRegistry;
    private static ProyectoRegistry proyectoRegistry;
    private static TipoUsuarioRegistry tipoUsuarioRegistry;
    private static RangoUsuarioRegistry rangoUsuarioRegistry;
    private static InteractionRegistry interactionRegistry;

    private static MultiverseCoreApi multiverseCoreApi;
    private static WorldEditPlugin worldEditPlugin;
    private static LuckPerms luckPermsApi;

    private static YamlConfiguration config;
    private static YamlConfiguration lang;

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

        ConfigHandler configHandler = ConfigHandler.getInstance();
        config = configHandler.getConfig();
        lang = configHandler.getLang();


        discordManager = DiscordManager.getInstance();
        if (discordManager.getJda() != null) DiscordLogger.toggleStaffConsoleLog();
        ConsoleLogger.debug(lang.getString("debug-mode-enabled"));

        dbManager = DBManager.getInstance();
        dsCommandManager = DsCommandManager.getInstance();
        worldManager = WorldManager.getInstance();
        projectManager = ProjectManager.getInstance();
        

        playerRegistry = PlayerRegistry.getInstance();
        proyectoRegistry = ProyectoRegistry.getInstance();
        tipoUsuarioRegistry = TipoUsuarioRegistry.getInstance();
        rangoUsuarioRegistry = RangoUsuarioRegistry.getInstance();
        interactionRegistry = InteractionRegistry.getInstance();

        permissionManager = PermissionManager.getInstance();
        

        getServer().getPluginManager().registerEvents(new BuildingListeners(), this);
        getServer().getPluginManager().registerEvents(new BannedListeners(), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerLeaveListener(), this);
        getServer().getPluginManager().registerEvents(new ChatListener(), this);
        getServer().getPluginManager().registerEvents(new MovingListeners(), this);
        getServer().getPluginManager().registerEvents(new HeadDBUtil(), this);
            
        // Registro de comandos
        PluginRegistry.registerCommand(new BTECSCommand());
        PluginRegistry.registerCommand(new GeneralConfigCommand());
        PluginRegistry.registerCommand(new ChatCommand());
        PluginRegistry.registerCommand(new PaisPrefixCommand());
        PluginRegistry.registerCommand(new PromoteCommand());
        PluginRegistry.registerCommand(new ProjectCommand());
        PluginRegistry.registerCommand(new CrudCommand());
        PluginRegistry.registerCommand(new ManagerCommand());
        PluginRegistry.registerCommand(new ReviewerCommand());
        PluginRegistry.registerCommand(new LinkCommand());
        PluginRegistry.registerCommand(new UnlinkCommand());
        PluginRegistry.registerCommand(new NicknameCommand());
        ConsoleLogger.info("El Plugin se ha activado.");
        
        if (config.getBoolean("discord-server-start-stop")) GlobalChatService.broadcastEmbed(ChatUtil.getServerStarted());
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

        if (interactionRegistry != null) {
            interactionRegistry.shutdown();
            interactionRegistry = null;
        }

        if (projectManager != null) {
            projectManager.shutdown();
            projectManager = null;
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
            DiscordLogger.toggleStaffConsoleLog();
            if (config.getBoolean("discord-server-start-stop")) GlobalChatService.broadcastEmbed(ChatUtil.getServerStopped());
            discordManager.shutdown();
            discordManager = null;
        }

        luckPermsApi = null;
          
        ConsoleLogger.info("El Plugin se ha desactivado.");
    }

    public static BTEConoSur getInstance() {
        return instance;
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

// TODO: Revisar llamadas a config en constructores.
// TODO: Alias de comandos.
// TODO: Comando de borrar mensajes en chat global de discord.
// TODO: Tab
// TODO: Get