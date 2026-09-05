package com.bteconosur.core.command.tour;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.command.tour.manage.TourManageCommand;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PlayerRegistry;

public class TourCommand extends BaseCommand {

    public TourCommand() {
        super("tour", null, "btecs.command.tour", CommandMode.BOTH);
        this.addSubcommand(new TourManageCommand());
        this.addSubcommand(new GenericHelpCommand(this));
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player commandPlayer = PlayerRegistry.getInstance().get(sender);
        Language language = commandPlayer != null ? commandPlayer.getLanguage() : Language.getDefault();
        String message = LanguageHandler.getText(language, "help-command-usage")
            .replace("%comando%", getFullCommand().replace(" " + command, ""));
        PlayerLogger.info(sender, message, (String) null);
        return true;
    }
}
