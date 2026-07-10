package com.bteconosur.core.api;

import java.util.Map;

import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.api.controller.DivisionController;
import com.bteconosur.core.api.controller.PaisController;
import com.bteconosur.core.api.controller.PlayerController;
import com.bteconosur.core.api.controller.ProyectoController;
import com.bteconosur.core.api.controller.RangoUsuarioController;
import com.bteconosur.core.api.controller.TipoProyectoController;
import com.bteconosur.core.api.controller.TipoUsuarioController;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.ConsoleLogger;

import io.javalin.Javalin;

public class ApiManager {

    private Javalin javalinApp;

    private static ApiManager instance;
    private final YamlConfiguration config;

    public ApiManager() {
        ConfigHandler configHandler = ConfigHandler.getInstance();
        config = configHandler.getConfig();
        if (!config.getBoolean("api-manager-enabled")) return;
        ConsoleLogger.info(LanguageHandler.getText("api-manager-initializing"));

        javalinApp = Javalin.create(config -> {
            config.bundledPlugins.enableCors(cors -> cors.addRule(it -> it.anyHost()));
            config.routes.get("/api/status", ctx -> {
                ctx.json(Map.of("status", "ok"));
            });
            new TipoUsuarioController().registrar(config.routes);
            new RangoUsuarioController().registrar(config.routes);
            new PlayerController().registrar(config.routes);
            new PaisController().registrar(config.routes);
            new DivisionController().registrar(config.routes);
            new TipoProyectoController().registrar(config.routes);
            new ProyectoController().registrar(config.routes);

        }).start(7070);
    }

    public void shutdown() {
        ConsoleLogger.info(LanguageHandler.getText("web-manager-shutting-down"));
        if (javalinApp != null) {
            javalinApp.stop();
        }
        if (instance != null) {
            instance = null;
        }
    }

    public static ApiManager getInstance() {
        if (instance == null) {
            instance = new ApiManager();
        }
        return instance;
    }
}

