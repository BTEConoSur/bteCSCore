package com.bteconosur.core.command.crud.division.update;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.DBManager;
import com.bteconosur.db.model.Division;
import com.bteconosur.db.model.Player;

public class UDivisionFnaCommand extends BaseCommand {

    private final DBManager dbManager;

    public UDivisionFnaCommand() {
        super("fna", "Actualizar fna de una Division.", "<id> <nuevo_fna>", CommandMode.BOTH);
        dbManager = DBManager.getInstance();
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player commandPlayer = Player.getBTECSPlayer((org.bukkit.entity.Player) sender);
        Language language = commandPlayer.getLanguage();
        if (args.length < 2) {
            String message = LanguageHandler.getText(language, "help-command-usage").replace("%comando%", getFullCommand().replace(" " + command, ""));
            PlayerLogger.info(sender, message, (String) null);
            return true;
        }

        Long id;
        try {
            id = Long.parseLong(args[0]);
        } catch (NumberFormatException ex) {
            String message = LanguageHandler.getText(language, "crud.not-valid-id").replace("%entity%", "Division").replace("%id%", args[0]);
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        if (!dbManager.exists(Division.class, id)) {
            String message = LanguageHandler.getText(language, "crud.read-not-found").replace("%entity%", "Division").replace("%id%", args[0]);
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        StringBuilder fnaBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            if (i > 1) fnaBuilder.append(" ");
            fnaBuilder.append(args[i]);
        }
        String nuevoFna = fnaBuilder.toString();

        if (nuevoFna.length() > 100) {
            PlayerLogger.error(sender, LanguageHandler.getText(language, "invalid-project-description"), (String) null);
            return true;
        }

        Division division = dbManager.get(Division.class, id);
        division.setFna(nuevoFna);
        dbManager.merge(division);

        String message = LanguageHandler.getText(language, "crud.update").replace("%entity%", "Division").replace("%id%", args[0]);
        PlayerLogger.info(sender, message, (String) null);
        return true;
    }

}
