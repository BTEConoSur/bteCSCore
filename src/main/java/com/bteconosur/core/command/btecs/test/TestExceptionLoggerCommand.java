package com.bteconosur.core.command.btecs.test;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.util.ConsoleLogger;

public class TestExceptionLoggerCommand extends BaseCommand {

    public TestExceptionLoggerCommand() {
        super("exceptionlogger", null, "btecs.command.test", CommandMode.BOTH);
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        try {
            throw new NullPointerException("Test exception: Variable no inicializada");
        } catch (Exception e) {
            ConsoleLogger.error("Prueba de error compacto", e);
        }

        return true;
    }
}
