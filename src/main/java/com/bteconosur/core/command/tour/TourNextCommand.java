package com.bteconosur.core.command.tour;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.tour.TourService;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.model.Player;

public class TourNextCommand extends BaseCommand {

    public TourNextCommand() {
        super("tournext", "", "btecs.command.tour", CommandMode.PLAYER_ONLY);
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player player = Player.getBTECSPlayer((org.bukkit.entity.Player) sender);
        Language language = player.getLanguage();
        TourService ts = TourService.getInstance();
        if (!ts.isInTour(player.getUuid())) {
            PlayerLogger.warn(sender, LanguageHandler.getText(language, "tour.not-in-tour"), (String) null);
            return true;
        }
        ts.nextStop(player.getUuid());
        return true;
    }

}
