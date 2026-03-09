package com.bteconosur.core.config;

import org.bukkit.configuration.file.YamlConfiguration;

/**
 * Manejador centralizado de archivos de configuración del plugin.
 */
public class ConfigHandler {
    private static ConfigHandler instance;
    private final ConfigFile config = new ConfigFile("config.yml");
    private final ConfigFile data = new ConfigFile("data.yml");
    private final ConfigFile gui = new ConfigFile("gui.yml");
    private final ConfigFile embedColors = new ConfigFile("embed-colors.yml");
    private final ConfigFile secret = new ConfigFile("secret.yml");

    /**
     * Inicializa el manejador de configuración registrando todos los archivos.
     */
    public ConfigHandler() {
        registerConfig();
        LanguageHandler.initialize();
    }

    /**
     * Registra todos los archivos de configuración del plugin.
     */
    private void registerConfig() {
        config.register();
        data.register();
        gui.register();
        embedColors.register();
        secret.register();
    }

    /**
     * Obtiene la configuración principal del plugin.
     *
     * @return configuración YAML de {@code config.yml}.
     */
    public YamlConfiguration getConfig() {
        return config.getFileConfiguration();
    }

    /**
     * Obtiene la configuración de colores de embeds de Discord.
     *
     * @return configuración YAML de {@code embed-colors.yml}.
     */
    public YamlConfiguration getEmbedColors() {
        return embedColors.getFileConfiguration();
    }

    /**
     * Obtiene la configuración de datos persistentes.
     *
     * @return configuración YAML de {@code data.yml}.
     */
    public YamlConfiguration getData() {
        return data.getFileConfiguration();
    }

    /**
     * Obtiene la configuración de interfaces gráficas.
     *
     * @return configuración YAML de {@code gui.yml}.
     */
    public YamlConfiguration getGui() {
        return gui.getFileConfiguration();
    }

    /**
     * Obtiene la configuración secreta (credenciales, tokens).
     *
     * @return configuración YAML de {@code secret.yml}.
     */
    public YamlConfiguration getSecret() {
        return secret.getFileConfiguration();
    }

    /**
     * Guarda todos los archivos de configuración y de idioma en disco.
     */
    public void save() {
        config.save();
        data.save();
        gui.save();
        embedColors.save();
        secret.save();
        LanguageHandler.save();
    }

    /**
     * Recarga todos los archivos de configuración y de idioma desde disco.
     */
    public void reload() {
        config.reload();
        data.reload();
        gui.reload();
        embedColors.reload();
        secret.reload();
        LanguageHandler.reload();
    }

    /**
     * Obtiene la instancia singleton del manejador de configuración.
     *
     * @return instancia única de {@code ConfigHandler}.
     */
    public static ConfigHandler getInstance() {
        if (instance == null) {
            instance = new ConfigHandler();
        }
        return instance;
    }

}
