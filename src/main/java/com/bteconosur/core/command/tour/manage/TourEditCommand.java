package com.bteconosur.core.command.tour.manage;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.PermissionManager;
import com.bteconosur.db.model.Pais;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.Tour;
import com.bteconosur.db.registry.PaisRegistry;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.db.registry.TourRegistry;

public class TourEditCommand extends BaseCommand {

    private final Set<String> paises = PaisRegistry.getInstance().getMap().values().stream().map(Pais::getNombre).collect(Collectors.toSet());

    public TourEditCommand() {
        super("edit", "<id_tour> [pais]", "btecs.command.tour", CommandMode.PLAYER_ONLY);
        this.addSubcommand(new GenericHelpCommand(this));
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player commandPlayer = PlayerRegistry.getInstance().get(sender);
        Language language = commandPlayer.getLanguage();
        if (args.length < 1 || args.length > 2) {
            String message = LanguageHandler.getText(language, "help-command-usage").replace("%comando%", getFullCommand());
            PlayerLogger.info(sender, message, (String) null);
            return true;
        }

        TourRegistry tr = TourRegistry.getInstance();
        Tour tour = TourRegistry.getInstance().get(args[0]);
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

        Pais pais;
        if (args.length == 1) {
            pais = null;
        } else {
            String normalized = args[1].trim();
            pais = PaisRegistry.getInstance().get(normalized.toLowerCase());
        }

        if (pais != null && !pm.isManager(commandPlayer, pais)) {
            PlayerLogger.error(sender, LanguageHandler.replaceMC("tour.no-permission-country", language, pais), (String) null);
            return true;
        }
        if (pais == null && !pm.isAdmin(commandPlayer)) {
            PlayerLogger.error(sender, LanguageHandler.getText(language, "tour.no-permission-none-country"), (String) null);
            return true;
        }
        if (pais.equals(tour.getPais())) {
            PlayerLogger.error(sender, LanguageHandler.replaceMC("tour.edit-already", language, tour), (String) null);
            return true;
        }

        tour.setPais(pais);
        tour = tr.merge(tour.getId());
        PlayerLogger.info(sender, LanguageHandler.replaceMC("tour.edit-success", language, tour), (String) null);
        return true;
    }

    @Override
    protected List<String> tabCompleteArgs(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return TourRegistry.getInstance().getIds().stream().filter(id -> id.toLowerCase().startsWith(args[0].toLowerCase())).toList();
        }
        if (args.length == 2) {
            return paises.stream().filter(v -> v.toLowerCase().startsWith(args[1].toLowerCase())).toList();
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
