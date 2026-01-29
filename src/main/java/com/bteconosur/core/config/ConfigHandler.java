package com.bteconosur.core.config;

import org.bukkit.configuration.file.YamlConfiguration;

public class ConfigHandler {
    private static ConfigHandler instance;
    private final ConfigFile config = new ConfigFile("config.yml");
    private final ConfigFile lang = new ConfigFile("lang.yml");
    private final ConfigFile data = new ConfigFile("data.yml");
    private final ConfigFile worlds = new ConfigFile("worlds.yml");

    public ConfigHandler() {
        registerConfig();
    }

    private void registerConfig() {
        config.register();
        lang.register();
        worlds.register();
        data.register();
    }

    public YamlConfiguration getConfig() {
        return config.getFileConfiguration();
    }

    public YamlConfiguration getLang() {
        return lang.getFileConfiguration();
    }

    public YamlConfiguration getWorlds() {
        return worlds.getFileConfiguration();
    }

    public YamlConfiguration getData() {
        return data.getFileConfiguration();
    }

    public void save() {
        config.save();
        lang.save();
        worlds.save();
        data.save();
    }

    public void reload() {
        config.reload();
        lang.reload();
        worlds.reload();
        data.reload();
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
