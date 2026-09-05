package com.bteconosur.core.command.tour.manage;

import java.util.Collections;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.Tour;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.db.registry.TourRegistry;

public class TourGetCommand extends BaseCommand {

    public TourGetCommand() {
        super("get", "<id_tour>", "btecs.command.tour", CommandMode.PLAYER_ONLY);
        this.addSubcommand(new GenericHelpCommand(this));
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player commandPlayer = PlayerRegistry.getInstance().get(sender);
        Language language = commandPlayer.getLanguage();
        if (args.length != 1) {
            String message = LanguageHandler.getText(language, "help-command-usage").replace("%comando%", getFullCommand());
            PlayerLogger.info(sender, message, (String) null);
            return true;
        }

        Tour tour = TourRegistry.getInstance().get(args[0]);
        if (tour == null) {
            PlayerLogger.error(sender, LanguageHandler.getText(language, "tour.not-found").replace("%id%", args[0]), (String) null);
            return true;
        }

        List<String> lines = LanguageHandler.getTextList(language, "tour.get-info");
        for (String line : lines) {
           PlayerLogger.send(sender, LanguageHandler.replaceMC(line, language, tour), (String) null);
        }
        for (String line : tour.getDescription(language)) {
            PlayerLogger.send(sender, " " + line, (String) null);
        }
        return true;
    }

    @Override
    protected List<String> tabCompleteArgs(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return TourRegistry.getInstance().getIds().stream().filter(id -> id.toLowerCase().startsWith(args[0].toLowerCase())).toList();
        }
        return Collections.emptyList();
    }
}
