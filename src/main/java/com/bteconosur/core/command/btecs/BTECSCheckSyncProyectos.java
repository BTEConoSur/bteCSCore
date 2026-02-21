package com.bteconosur.core.command.btecs;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.world.WorldManager;

public class BTECSCheckSyncProyectos extends BaseCommand {

    public BTECSCheckSyncProyectos() {
        super("syncproyectos", "Verifica la sincronización de proyectos", null, CommandMode.CONSOLE_ONLY);
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        PlayerLogger.info(sender, LanguageHandler.getText("btecs-sync-proyectos-init"), (String) null);
        WorldManager.getInstance().syncRegions();
        String message = LanguageHandler.getText("btecs-sync-proyectos-success");
        PlayerLogger.info(sender, message, (String) null);
        return true;
    }

}
