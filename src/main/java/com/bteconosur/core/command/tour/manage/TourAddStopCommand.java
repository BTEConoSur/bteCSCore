package com.bteconosur.core.command.tour.manage;

import java.util.Collections;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.prep.PreparedGeometryFactory;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.core.util.RegionUtils;
import com.bteconosur.db.PermissionManager;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.Tour;
import com.bteconosur.db.model.TourStop;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.db.registry.TourRegistry;
import com.bteconosur.db.util.PlaceholderUtils;

public class TourAddStopCommand extends BaseCommand {

    public TourAddStopCommand() {
        super("addstop", "<id_tour> <id_parada> [orden]", "btecs.command.tour", CommandMode.PLAYER_ONLY);
        this.addSubcommand(new GenericHelpCommand(this));
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        if (args.length < 2 || args.length > 3) {
            String message = LanguageHandler.getText(PlayerRegistry.getInstance().get(sender).getLanguage(), "help-command-usage").replace("%comando%", getFullCommand());
            PlayerLogger.info(sender, message, (String) null);
            return true;
        }

        Player commandPlayer = PlayerRegistry.getInstance().get(sender);
        Language language = commandPlayer.getLanguage();
        TourRegistry tr = TourRegistry.getInstance();
        Tour tour = tr.get(args[0]);
        if (tour == null) {
            PlayerLogger.error(sender, LanguageHandler.getText(language, "tour.not-found").replace("%id%", args[0]), (String) null);
            return true;
        }

        PermissionManager pm = PermissionManager.getInstance();
        if (tour.getPais() != null && !pm.isManager(commandPlayer, tour.getPais())) {
            PlayerLogger.error(sender, LanguageHandler.replaceMC("tour.no-permission-country", language, tour.getPais()), (String) null);
            return true;
        }
        if (tour.getPais() == null && !pm.isAdmin(commandPlayer)) {
            PlayerLogger.error(sender, LanguageHandler.getText(language, "tour.no-permission-none-country"), (String) null);
            return true;
        }

        String stopId = args[1];
        stopId = stopId.replaceAll("[^a-zA-Z0-9_-]", "_");
        if (tour.hasParada(stopId)) {
            PlayerLogger.error(sender, LanguageHandler.getText(language, "tour.stop.exists").replace("%id%", stopId), (String) null);
            return true;
        }
        
        int orden;
        if (args.length >= 3) {
            try {
                orden = Integer.parseInt(args[2]);
            } catch (NumberFormatException ex) {
                PlayerLogger.error(sender, LanguageHandler.getText(language, "invalid-number"), (String) null);
                return true;
            }
        } else {
            orden = tour.getParadas().size() + 1;
        }
        if (!tour.checkOrden(orden)) {
            PlayerLogger.error(sender, LanguageHandler.getText(language, "tour.stop.invalid-order")
                .replace("%orden%", String.valueOf(orden))
                .replace("%max%", String.valueOf(tour.getParadas().size() + 1)), (String) null);
            return true;
        }

        Polygon polygon = RegionUtils.getPolygon(sender);
        if (polygon == null) return true;

        Location loc = ((org.bukkit.entity.Player) sender).getLocation();
        if (!RegionUtils.containsCoordinate(PreparedGeometryFactory.prepare(polygon), polygon.getEnvelopeInternal(), loc.getX(), loc.getZ())) {   
            PlayerLogger.error(sender, LanguageHandler.getText(language, "tour.stop.bad-position"), (String) null);
            return true;
        }
        TourStop parada = tr.createTourParada(tour.getId(), stopId, orden, loc, polygon);
        String message = LanguageHandler.replaceMC("tour.addstop-success", language, tour);
        PlayerLogger.info(sender, PlaceholderUtils.replaceMC(message, language, parada), (String) null);
        return true;
    }

    @Override
    protected List<String> tabCompleteArgs(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return TourRegistry.getInstance().getIds().stream().filter(id -> id.toLowerCase().startsWith(args[0].toLowerCase())).toList();
        }
        return Collections.emptyList();
    }

    @Override
    protected boolean customPermissionCheck(CommandSender sender) {
        Player commandPlayer = PlayerRegistry.getInstance().get(((org.bukkit.entity.Player) sender).getUniqueId());
        PermissionManager pm = PermissionManager.getInstance();
        return pm.isManager(commandPlayer) || pm.isAdmin(commandPlayer);
    }
}
