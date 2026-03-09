package com.bteconosur.core.config;

import com.bteconosur.core.BTEConoSur;
import com.bteconosur.core.util.ConsoleLogger;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

/**
 * Maneja la carga, guardado y recarga de archivos de configuración YAML.
 */
public class ConfigFile {
    private YamlConfiguration fileConfiguration;
    private final BTEConoSur plugin = BTEConoSur.getInstance();
    private File file;
    private final String fileName;

    /**
     * Crea una instancia del manejador de archivo de configuración.
     *
     * @param fileName nombre del archivo relativo a la carpeta de datos del plugin.
     */
    public ConfigFile(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Registra el archivo de configuración: lo crea desde recursos si no existe y carga la configuración.
     */
    public void register() {
        this.file = new File(plugin.getDataFolder(), fileName);

        if (!this.file.exists()) {
            plugin.saveResource(fileName, false);
        }

        this.fileConfiguration = YamlConfiguration.loadConfiguration(this.file);
    }

    /**
     * Guarda los cambios de la configuración al archivo en disco.
     *
     * @throws RuntimeException si ocurre un error de I/O durante el guardado.
     */
    public void save() {
        try {
            this.fileConfiguration.save(this.file);
        } catch (IOException e) {
            ConsoleLogger.error("Error al guardar el archivo de configuración " + fileName, e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Recarga la configuración desde el archivo en disco.
     *
     * @throws RuntimeException si ocurre un error de I/O o de formato YAML durante la recarga.
     */
    public void reload() {
        try {
            this.fileConfiguration.load(this.file);
        } catch (IOException | InvalidConfigurationException e) {
            ConsoleLogger.error("Error al recargar el archivo de configuración " + fileName, e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Obtiene la configuración YAML cargada en memoria.
     *
     * @return instancia de {@code YamlConfiguration} del archivo.
     */
    public YamlConfiguration getFileConfiguration() {
        return this.fileConfiguration;
    }
}
