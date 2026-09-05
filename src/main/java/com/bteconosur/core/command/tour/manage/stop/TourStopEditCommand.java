package com.bteconosur.core.command.tour.manage.stop;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PlayerRegistry;

public class TourStopEditCommand extends BaseCommand {

    public TourStopEditCommand() {
        super("edit", null, "btecs.command.tour", CommandMode.PLAYER_ONLY);
        this.addSubcommand(new TourStopEditOrderCommand());
        this.addSubcommand(new TourStopEditPolygonCommand());
        this.addSubcommand(new TourStopEditPositionCommand());
        this.addSubcommand(new GenericHelpCommand(this));
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player player = PlayerRegistry.getInstance().get(sender);
        Language language = player != null ? player.getLanguage() : Language.getDefault();
        String message = LanguageHandler.getText(language, "help-command-usage").replace("%comando%", getFullCommand());
        PlayerLogger.info(sender, message, (String) null);
        return true;
    }
}
