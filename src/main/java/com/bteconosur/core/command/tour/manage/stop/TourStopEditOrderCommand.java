package com.bteconosur.core.command.tour.manage.stop;

import java.util.Collections;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
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

public class TourStopEditOrderCommand extends BaseCommand {

    public TourStopEditOrderCommand() {
        super("order", "<id_tour> <id_parada> <orden>", "btecs.command.tour", CommandMode.PLAYER_ONLY);
        this.addSubcommand(new GenericHelpCommand(this));
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player commandPlayer = PlayerRegistry.getInstance().get(sender);
        Language language = commandPlayer.getLanguage();
        if (args.length != 3) {
            PlayerLogger.info(sender, LanguageHandler.getText(language, "help-command-usage").replace("%comando%", getFullCommand()), (String) null);
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

        int nuevoOrden;
        try {
            nuevoOrden = Integer.parseInt(args[2]);
        } catch (NumberFormatException ex) {
            PlayerLogger.error(sender, LanguageHandler.getText(language, "invalid-number"), (String) null);
            return true;
        }
        if (!tour.checkOrden(nuevoOrden)) {
            PlayerLogger.error(sender, LanguageHandler.getText(language, "tour.stop.invalid-order")
                .replace("%orden%", String.valueOf(nuevoOrden))
                .replace("%max%", String.valueOf(tour.getParadas().size() + 1)), (String) null);
            return true;
        }

        parada = tr.editTourParada(tour.getId(), parada.getId(), nuevoOrden);
        String message = LanguageHandler.replaceMC("tour.stop.edit-order-success", language, tour);
        PlayerLogger.info(sender, PlaceholderUtils.replaceMC(message, language, parada), (String) null);
        return true;
    }

    @Override
    protected List<String> tabCompleteArgs(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return TourRegistry.getInstance().getIds().stream().filter(id -> id.toLowerCase().startsWith(args[0].toLowerCase())).toList();
        }
        if (args.length == 2) {
            return TourRegistry.getInstance().getTourStopIds(args[0]).stream().filter(id -> id.toLowerCase().startsWith(args[1].toLowerCase())).toList();
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