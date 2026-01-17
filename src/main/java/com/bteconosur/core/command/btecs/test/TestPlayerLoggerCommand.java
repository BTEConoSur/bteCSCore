package com.bteconosur.core.command.btecs.test;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PlayerRegistry;

public class TestPlayerLoggerCommand extends BaseCommand {

    public TestPlayerLoggerCommand() {
        super("playerlogger", "Comando de prueba para el logger de jugadores.", null, CommandMode.PLAYER_ONLY);
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        org.bukkit.entity.Player bukkitPlayer = (org.bukkit.entity.Player) sender;
        Player player = PlayerRegistry.getInstance().get(bukkitPlayer.getUniqueId());
        
        PlayerLogger.info(sender, "Este es un mensaje de info de prueba.", "Este es un mensaje de info de prueba.");
        PlayerLogger.warn(sender, "Este es un mensaje de advertencia de prueba.", "Este es un mensaje de advertencia de prueba.");
        PlayerLogger.error(sender, "Este es un mensaje de error de prueba.", "Este es un mensaje de error de prueba.");
        PlayerLogger.debug(sender, "Este es un mensaje de debug de prueba.", "Este es un mensaje de debug de prueba.");
        PlayerLogger.send(sender, "<green>Este es un mensaje directo sin prefijo.", (String) null);
        PlayerLogger.debug(player, "Datos del jugador:", "Datos del jugador:", player);
        
        return true;
    }

}
