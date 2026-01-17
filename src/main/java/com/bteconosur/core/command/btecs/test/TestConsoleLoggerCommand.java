package com.bteconosur.core.command.btecs.test;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.util.ConsoleLogger;
import com.bteconosur.db.DBManager;
import com.bteconosur.db.model.Player;

public class TestConsoleLoggerCommand extends BaseCommand {

    public TestConsoleLoggerCommand() {
        super("consolelogger", "Comando de prueba para el logger de consola.", null, CommandMode.PLAYER_ONLY);
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        ConsoleLogger.info("Este es un mensaje de info de prueba.");
        ConsoleLogger.warn("Este es un mensaje de advertencia de prueba.");
        ConsoleLogger.error("Este es un mensaje de error de prueba.");
        ConsoleLogger.debug("Este es un mensaje de debug de prueba.");
        Player firstPlayer = DBManager.getInstance().get(Player.class, ((org.bukkit.entity.Player)sender).getUniqueId());
        ConsoleLogger.warn("Este es un mensaje de prueba: ", firstPlayer);
        return true;
    }   

}
