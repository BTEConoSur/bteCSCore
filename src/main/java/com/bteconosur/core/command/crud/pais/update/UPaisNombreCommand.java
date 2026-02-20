package com.bteconosur.core.command.crud.pais.update;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.DBManager;
import com.bteconosur.db.model.Pais;
import com.bteconosur.db.model.Player;

public class UPaisNombreCommand extends BaseCommand {

    private final DBManager dbManager;

    public UPaisNombreCommand() {
        super("nombre", "Actualizar nombre de un País.", "<id> <nombre>", CommandMode.BOTH);
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

        Long id;
        try {
            id = Long.parseLong(args[0]);
        } catch (NumberFormatException ex) {
            String message = LanguageHandler.getText(language, "crud.not-valid-id").replace("%entity%", "Pais").replace("%id%", args[0]);
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        if (!dbManager.exists(Pais.class, id)) {
            String message = LanguageHandler.getText(language, "crud.read-not-found").replace("%entity%", "Pais").replace("%id%", args[0]);
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        String nuevoNombre = args[1];
        if (nuevoNombre.length() > 50) {
            String message = LanguageHandler.getText(language, "crud.not-valid-name").replace("%entity%", "Pais").replace("%name%", nuevoNombre).replace("%reason%", "Máximo 50 caracteres.");
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        Pais pais = dbManager.get(Pais.class, id);
        pais.setNombre(nuevoNombre);
        dbManager.merge(pais);

        String message = LanguageHandler.getText(language, "crud.update").replace("%entity%", "Pais").replace("%id%", args[0]);
        PlayerLogger.info(sender, message, (String) null);
        return true;
    }
}
