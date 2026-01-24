package com.bteconosur.core;

import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.util.ConsoleLogger;

public class ProjectManager {

    private static ProjectManager instance;

    private final YamlConfiguration lang;
    
    public ProjectManager() {
        lang = ConfigHandler.getInstance().getLang();

        ConsoleLogger.info(lang.getString("project-manager-initializing"));
    }
    
    public void shutdown() {
        ConsoleLogger.info(lang.getString("project-manager-shutting-down"));
        if (instance != null) {
            instance = null;
        }
    }

    public static ProjectManager getInstance() {
        if (instance == null) {
            instance = new ProjectManager();
        }
        return instance;
    }
    
}
