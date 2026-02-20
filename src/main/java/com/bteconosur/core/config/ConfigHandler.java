package com.bteconosur.core.config;

import org.bukkit.configuration.file.YamlConfiguration;

public class ConfigHandler {
    private static ConfigHandler instance;
    private final ConfigFile config = new ConfigFile("config.yml");
    private final ConfigFile data = new ConfigFile("data.yml");
    private final ConfigFile gui = new ConfigFile("gui.yml");
    private final ConfigFile embedColors = new ConfigFile("embed-colors.yml");
    private final ConfigFile secret = new ConfigFile("secret.yml");

    public ConfigHandler() {
        registerConfig();
        LanguageHandler.initialize();
    }

    private void registerConfig() {
        config.register();
        data.register();
        gui.register();
        embedColors.register();
        secret.register();
    }

    public YamlConfiguration getConfig() {
        return config.getFileConfiguration();
    }

    public YamlConfiguration getEmbedColors() {
        return embedColors.getFileConfiguration();
    }

    public YamlConfiguration getData() {
        return data.getFileConfiguration();
    }

    public YamlConfiguration getGui() {
        return gui.getFileConfiguration();
    }

    public YamlConfiguration getSecret() {
        return secret.getFileConfiguration();
    }

    public void save() {
        config.save();
        data.save();
        gui.save();
        embedColors.save();
        secret.save();
        LanguageHandler.save();
    }

    public void reload() {
        config.reload();
        data.reload();
        gui.reload();
        embedColors.reload();
        secret.reload();
        LanguageHandler.reload();
    }

    /**
     * Get the instance of the ConfigHandler
     * @return ConfigHandler instance
     */
    public static ConfigHandler getInstance() {
        if (instance == null) {
            instance = new ConfigHandler();
        }
        return instance;
    }

}
