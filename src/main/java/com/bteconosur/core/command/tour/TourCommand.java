package com.bteconosur.core.command.tour;

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
import com.bteconosur.core.menu.tour.TourListMenu;
import com.bteconosur.core.menu.tour.TourPaisMenu;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.model.Pais;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PaisRegistry;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.db.registry.TourRegistry;

public class TourCommand extends BaseCommand {

    private final Set<String> paises = PaisRegistry.getInstance().getMap().values()
        .stream().map(Pais::getNombre).collect(Collectors.toSet());

    public TourCommand() {
        super("tour", "[pais]", "btecs.command.tour", CommandMode.BOTH);
        this.addSubcommand(new TourHereCommand());
        this.addSubcommand(new GenericHelpCommand(this));
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player commandPlayer = PlayerRegistry.getInstance().get(sender);
        Language language = commandPlayer.getLanguage();
        if (args.length > 1) {
            String message = LanguageHandler.getText(language, "help-command-usage").replace("%comando%", getFullCommand());
            PlayerLogger.info(sender, message, (String) null);
            return true;
        }

        TourRegistry tr = TourRegistry.getInstance();
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase(LanguageHandler.getText(language, "placeholder.tour.international").toLowerCase())) {
                new TourListMenu(commandPlayer, (Pais) null, false).open();
                return true;
            }
            Pais pais = PaisRegistry.getInstance().get(args[0]);
            if (pais == null) {
                PlayerLogger.error(sender, LanguageHandler.getText(language, "pais-not-found").replace("%search%", args[0]), (String) null);
                return true;
            }

            new TourListMenu(commandPlayer, pais, false).open();
            return true;
        }

        new TourPaisMenu(commandPlayer, false).open();
        return true;
    }

    @Override
    protected List<String> tabCompleteArgs(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            Player commandPlayer = PlayerRegistry.getInstance().get(sender);
            paises.add(LanguageHandler.getText(commandPlayer.getLanguage(), "placeholder.tour.international").toLowerCase());
            return paises.stream().filter(v -> v.toLowerCase().startsWith(args[0].toLowerCase())).toList();
        }
        return Collections.emptyList();
    }
}
