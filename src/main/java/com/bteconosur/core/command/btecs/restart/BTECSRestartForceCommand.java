package com.bteconosur.core.command.btecs.restart;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.core.util.RestartService;
import com.bteconosur.db.model.Player;

public class BTECSRestartForceCommand extends BaseCommand {

    public BTECSRestartForceCommand() {
        super("force", "<minutos>", "btecs.command.btecs.restart", CommandMode.BOTH);
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player commandPlayer = null;
        if (sender instanceof org.bukkit.entity.Player) commandPlayer = Player.getBTECSPlayer((org.bukkit.entity.Player) sender);
        Language language = commandPlayer != null ? commandPlayer.getLanguage() : Language.getDefault();

        if (args.length == 0) {
            String usage = LanguageHandler.getText(language, "help-command-usage").replace("%comando%", getFullCommand());
            PlayerLogger.info(sender, usage, (String) null);
            return true;
        }

        long minutes;
        try {
            minutes = Long.parseLong(args[0]);
        } catch (NumberFormatException ex) {
            PlayerLogger.info(sender, LanguageHandler.getText(language, "restart-invalid-minutes"), (String) null);
            return true;
        }

        if (minutes < 0L) {
            PlayerLogger.info(sender, LanguageHandler.getText(language, "restart-invalid-minutes"), (String) null);
            return true;
        }

        RestartService restartService = RestartService.getInstance();
        restartService.forceRestart(minutes);
        String message = LanguageHandler.getText(language, "restart-forced").replace("%time%", restartService.getRemainingTimeText(language));
        PlayerLogger.info(sender, message, (String) null);
        return true;
    }
}