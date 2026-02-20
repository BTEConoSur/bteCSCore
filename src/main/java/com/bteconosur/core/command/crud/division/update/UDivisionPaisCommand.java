package com.bteconosur.core.command.crud.division.update;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.DBManager;
import com.bteconosur.db.model.Division;
import com.bteconosur.db.model.Pais;
import com.bteconosur.db.model.Player;

public class UDivisionPaisCommand extends BaseCommand {

    private final DBManager dbManager;

    public UDivisionPaisCommand() {
        super("pais", "Actualizar País de una División.", "<id_division> <id_pais>", CommandMode.BOTH);
        dbManager = DBManager.getInstance();
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player commandPlayer = Player.getBTECSPlayer((org.bukkit.entity.Player) sender);
        Language language = commandPlayer.getLanguage();
        if (args.length != 2) {
            String message = LanguageHandler.getText(language, "help-command-usage").replace("%comando%", getFullCommand().replace(" " + command, ""));
            PlayerLogger.info(sender, message, (String) null);
            return true;
        }

        Long divisionId;
        try {
            divisionId = Long.parseLong(args[0]);
        } catch (NumberFormatException ex) {
            String message = LanguageHandler.getText(language, "crud.not-valid-id").replace("%entity%", "Division").replace("%id%", args[0]);
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        if (!dbManager.exists(Division.class, divisionId)) {
            String message = LanguageHandler.getText(language, "crud.read-not-found").replace("%entity%", "Division").replace("%id%", args[0]);
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        Long paisId;
        try {
            paisId = Long.parseLong(args[1]);
        } catch (NumberFormatException ex) {
            String message = LanguageHandler.getText(language, "crud.not-valid-id").replace("%entity%", "Pais").replace("%id%", args[1]);
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        if (!dbManager.exists(Pais.class, paisId)) {
            String message = LanguageHandler.getText(language, "crud.read-not-found").replace("%entity%", "Pais").replace("%id%", args[1]);
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        Division division = dbManager.get(Division.class, divisionId);
        Pais pais = dbManager.get(Pais.class, paisId);
        division.setPais(pais);
        dbManager.merge(division);

        String message = LanguageHandler.getText(language, "crud.update").replace("%entity%", "Division").replace("%id%", args[0]);
        PlayerLogger.info(sender, message, (String) null);
        return true;
    }

}
