package com.bteconosur.core.command.btecs;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.api.ApiManager;
import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.PlayerLogger;

public class BTECSSyncWeb extends BaseCommand {

    public BTECSSyncWeb() {
        super("syncweb", "[id]", "btecs.command.btecs.syncweb", CommandMode.CONSOLE_ONLY);
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        if (args.length > 0 && !args[0].isBlank()) {
            String proyectoId = args[0].trim();
            PlayerLogger.info(sender, LanguageHandler.getText("web-sync-all") + " " + proyectoId, (String) null);
            ApiManager.getInstance().syncProject(proyectoId);
            return true;
        }

        PlayerLogger.info(sender, LanguageHandler.getText("web-sync-all"), (String) null);
        ApiManager.getInstance().syncAll();
        return true;
    }

}
