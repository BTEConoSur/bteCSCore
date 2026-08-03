package com.bteconosur.core.command.btecs.restart;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.core.util.RestartService;
import com.bteconosur.db.model.Player;

public class BTECSRestartCancelCommand extends BaseCommand {

    public BTECSRestartCancelCommand() {
        super("cancel", null, "btecs.command.btecs.restart", CommandMode.CONSOLE_ONLY);
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        boolean cancelled = RestartService.getInstance().cancelRestart();
        Player commandPlayer = null;
        if (sender instanceof org.bukkit.entity.Player) commandPlayer = Player.getBTECSPlayer((org.bukkit.entity.Player) sender);
        Language language = commandPlayer != null ? commandPlayer.getLanguage() : Language.getDefault();

        String message = cancelled
                ? LanguageHandler.getText(language, "restart-cancelled")
                : LanguageHandler.getText(language, "restart-not-scheduled");
        PlayerLogger.info(sender, message, (String) null);
        return true;
    }
}