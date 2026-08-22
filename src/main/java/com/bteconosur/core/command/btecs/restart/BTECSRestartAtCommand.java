package com.bteconosur.core.command.btecs.restart;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.DateUtils;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.core.util.RestartService;
import com.bteconosur.db.model.Player;

public class BTECSRestartAtCommand extends BaseCommand  {

    public BTECSRestartAtCommand() {
        super("at", "<HH:mm>", "btecs.command.btecs.restart", CommandMode.BOTH);
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player commandPlayer = null;
        if (sender instanceof org.bukkit.entity.Player) commandPlayer = Player.getBTECSPlayer((org.bukkit.entity.Player) sender);
        Language language = commandPlayer != null ? commandPlayer.getLanguage() : Language.getDefault();

        if (args.length != 1) {
            String usage = LanguageHandler.getText(language, "help-command-usage").replace("%comando%", getFullCommand());
            PlayerLogger.info(sender, usage, (String) null);
            return true;
        }

        RestartService restartService = RestartService.getInstance();
        String timeInput = args[0];
        try {
            LocalTime targetTime = LocalTime.parse(timeInput);
            LocalDateTime now = LocalDateTime.ofInstant(DateUtils.instantOffset(), ZoneOffset.UTC);
            LocalDateTime targetDateTime = now.with(targetTime);

            if (targetDateTime.compareTo(now) <= 0)targetDateTime = targetDateTime.plusDays(1);
            long delayMinutes = Duration.between(now, targetDateTime).toMinutes();
            restartService.forceRestart(delayMinutes);

            String successMsg = LanguageHandler.getText(language, "restart-programmed").replace("%time%", timeInput)
                .replace("%delay%", restartService.getRemainingTimeText(language));
            PlayerLogger.info(sender, successMsg, (String) null);

        } catch (DateTimeParseException e) {
            PlayerLogger.error(sender, LanguageHandler.getText(language, "restart-invalid-hour"), (String) null);
        }

        return true;
    }

}
