package com.bteconosur.core.command.tour;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.tour.TourService;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.model.Division;
import com.bteconosur.db.model.Pais;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PaisRegistry;

public class TourHereCommand extends BaseCommand {

    public TourHereCommand() {
        super("here", "", "btecs.command.tour", CommandMode.PLAYER_ONLY);
        this.addSubcommand(new GenericHelpCommand(this));
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        org.bukkit.entity.Player bukkitPlayer = (org.bukkit.entity.Player) sender;
        Player player = Player.getBTECSPlayer(bukkitPlayer);
        Language language = player.getLanguage();
        TourService ts = TourService.getInstance();
        PaisRegistry pr = PaisRegistry.getInstance();

        Location loc = bukkitPlayer.getLocation();
        Pais pais = pr.findByLocation(loc.getX(), loc.getZ());
        if (pais == null) {
            PlayerLogger.error(player, LanguageHandler.getText(language, "where.no-pais"), (String) null);
            return true;
        }
        Division division = pr.findDivisionByLocation(loc.getX(), loc.getZ(), pais);
        if (division == null) {
            PlayerLogger.error(player, LanguageHandler.replaceMC("where.no-division2", language, pais), (String) null);
            return true;
        }
        ts.startProjectTour(player, division);
        return true;
    }

}
