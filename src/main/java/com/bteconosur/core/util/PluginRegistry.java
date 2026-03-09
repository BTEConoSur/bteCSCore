package com.bteconosur.core.util;

import com.bteconosur.core.BTEConoSur;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;

import java.lang.reflect.InvocationTargetException;

/**
 * Utilidad para registrar comandos en Bukkit y administrar ciclo de vida del plugin.
 * Proporciona accés al CommandMap y manejo de deshabilitación del plugin.
 */
public class PluginRegistry {
    
    /**
     * Obtiene el CommandMap de Bukkit usando reflexión.
     *
     * @return CommandMap de Bukkit para registrar comandos.
     */
    public static CommandMap getCommandMap() {
        try {
            return (CommandMap) Bukkit.getServer().getClass().getDeclaredMethod("getCommandMap")
                    .invoke(Bukkit.getServer());
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            disablePlugin("Error getting bukkit command map");
            throw new RuntimeException(e);
        }
    }

    /**
     * Registra un comando con etiqueta BTEConoSur en el CommandMap de Bukkit.
     *
     * @param command comando a registrar.
     */
    public static void registerCommand(Command command) {
        CommandMap commandMap = getCommandMap();
        commandMap.register("BTEConoSur", command);
    }

    /**
     * Deshabilita el plugin del servidor registrando un motivo de error.
     *
     * @param reason razón por la que se deshabilita el plugin.
     */
    public static void disablePlugin(String reason) {
        ConsoleLogger.error("Desabilitando plugin: " + reason);
        Bukkit.getServer().getPluginManager().disablePlugin(BTEConoSur.getInstance());
    }
}
