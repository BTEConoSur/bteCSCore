package com.bteconosur.core.command.tour.manage.stop;

import java.util.Collections;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.PermissionManager;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.Tour;
import com.bteconosur.db.model.TourStop;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.db.registry.TourRegistry;
import com.bteconosur.db.util.PlaceholderUtils;

public class TourStopRemoveCommand extends BaseCommand {

    public TourStopRemoveCommand() {
        super("remove", "<id_tour> <id_parada>", "btecs.command.tour.manage", CommandMode.PLAYER_ONLY);
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player commandPlayer = PlayerRegistry.getInstance().get(sender);
        Language language = commandPlayer.getLanguage();
        if (args.length != 2) {
            String message = LanguageHandler.getText(language, "help-command-usage").replace("%comando%", getFullCommand());
            PlayerLogger.info(sender, message, (String) null);
            return true;
        }

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

        TourStop parada;
        try {
            parada = tour.getParada(Integer.parseInt(args[1]));
        } catch (NumberFormatException ignored) {
            parada = tour.getParada(args[1]);
        }
        if (parada == null) {
            PlayerLogger.error(sender, LanguageHandler.getText(language, "tour.stop.not-found").replace("%id%", args[1]), (String) null);
            return true;
        }

        tr.removeTourParada(tour.getId(), parada.getId());
        String message = LanguageHandler.replaceMC("tour.stop.remove-success", language, tour);
        PlayerLogger.info(sender, PlaceholderUtils.replaceMC(message, language, parada), (String) null);
        return true;
    }

    @Override
    protected List<String> tabCompleteArgs(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return TourRegistry.getInstance().getMap().keySet().stream().filter(id -> id.toLowerCase().startsWith(args[0].toLowerCase())).toList();
        }
        return Collections.emptyList();
    }
}
