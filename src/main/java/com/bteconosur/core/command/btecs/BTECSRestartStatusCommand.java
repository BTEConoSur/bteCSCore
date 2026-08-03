package com.bteconosur.core.command.btecs;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.core.util.RestartService;
import com.bteconosur.db.model.Player;

public class BTECSRestartStatusCommand extends BaseCommand {

    public BTECSRestartStatusCommand() {
        super("status", null, "btecs.command.btecs.status", CommandMode.BOTH);
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player commandPlayer = null;
        if (sender instanceof org.bukkit.entity.Player) commandPlayer = Player.getBTECSPlayer((org.bukkit.entity.Player) sender);
        Language language = commandPlayer != null ? commandPlayer.getLanguage() : Language.getDefault();

        String message = RestartService.getInstance().getStatusMessage(language);
        PlayerLogger.info(sender, message, (String) null);
        return true;
    }
}