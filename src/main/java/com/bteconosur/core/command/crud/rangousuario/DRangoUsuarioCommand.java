package com.bteconosur.core.command.crud.rangousuario;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.DBManager;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.RangoUsuario;

public class DRangoUsuarioCommand extends BaseCommand {

    private final DBManager dbManager;

    public DRangoUsuarioCommand() {
        super("delete", "Eliminar un Rango de Usuario.", "<id>", CommandMode.BOTH);
        dbManager = DBManager.getInstance();
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player commandPlayer = Player.getBTECSPlayer((org.bukkit.entity.Player) sender);
        Language language = commandPlayer.getLanguage();
        if (args.length != 1) {
            String message = LanguageHandler.getText(language, "help-command-usage").replace("%comando%", getFullCommand().replace(" " + command, ""));
            PlayerLogger.info(sender, message, (String) null);
            return true;
        }

        Long id;
        try {
            id = Long.parseLong(args[0]);
        } catch (NumberFormatException ex) {
            String message = LanguageHandler.getText(language, "crud.not-valid-id").replace("%entity%", "Rango de Usuario").replace("%id%", args[0]);
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        if (!dbManager.exists(RangoUsuario.class, id)) {
            String message = LanguageHandler.getText(language, "crud.read-not-found").replace("%entity%", "Rango de Usuario").replace("%id%", args[0]);
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        RangoUsuario rangoUsuario = dbManager.get(RangoUsuario.class, id);
        if (rangoUsuario != null) {
            dbManager.remove(rangoUsuario);
        }

        String message = LanguageHandler.getText(language, "crud.delete").replace("%entity%", "Rango de Usuario").replace("%id%", args[0]);
        PlayerLogger.info(sender, message, (String) null, rangoUsuario);
        return true;
    }
}
