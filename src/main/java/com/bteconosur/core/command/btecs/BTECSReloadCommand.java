package com.bteconosur.core.command.btecs;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.PlayerLogger;

import org.bukkit.command.CommandSender;

public class BTECSReloadCommand extends BaseCommand {
    public BTECSReloadCommand() {
        super("reload", "Recarga la configuración del plugin", null, CommandMode.CONSOLE_ONLY);
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        ConfigHandler configHandler = ConfigHandler.getInstance();
        configHandler.reload();
        String message = LanguageHandler.getText("btecs-reload-success");
        PlayerLogger.info(sender, message, (String) null);
        return true;
    }
}
