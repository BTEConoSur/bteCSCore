package com.bteconosur.core.command.pwarp;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PlayerRegistry;

public class PwarpRemoveCommand extends BaseCommand {

    public PwarpRemoveCommand() {
        super("remove", "<nombre_warp>", CommandMode.PLAYER_ONLY);
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        PlayerRegistry registry = PlayerRegistry.getInstance();
        Player player = registry.get(sender);
        Language language = player.getLanguage();
        if (args.length != 1) {
            String message = LanguageHandler.getText(language, "help-command-usage").replace("%comando%", getFullCommand());
            PlayerLogger.info(sender, message, (String) null);
            return true;
        }
        String nombreWarp = args[0];
        if (!player.hasPwarp(nombreWarp)) {
            PlayerLogger.error(player, LanguageHandler.getText(player.getLanguage(), "pwarp.not-found").replace("%nombre%", nombreWarp), (String) null);
            return true;
        }
        registry.removePwarp(player.getUuid(), nombreWarp);
        PlayerLogger.info(player, LanguageHandler.getText(player.getLanguage(), "pwarp.deleted").replace("%nombre%", nombreWarp), (String) null);
        return true;
    }

}
