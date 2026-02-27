package com.bteconosur.core.command.crud.rangousuario;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.DBManager;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.RangoUsuario;

public class CRangoUsuarioCommand extends BaseCommand {

    private final DBManager dbManager;

    public CRangoUsuarioCommand() {
        super("create", "<nombre>", "btecs.command.crud", CommandMode.BOTH);
        dbManager = DBManager.getInstance();
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player commandPlayer = null;
        if (sender instanceof org.bukkit.entity.Player) commandPlayer = Player.getBTECSPlayer((org.bukkit.entity.Player) sender);
        Language language = commandPlayer != null ? commandPlayer.getLanguage() : Language.getDefault();
        if (args.length != 1) {
            String message = LanguageHandler.getText(language, "help-command-usage").replace("%comando%", getFullCommand().replace(" " + command, ""));
            PlayerLogger.info(sender, message, (String) null);
            return true;
        }

        String nombre = args[0];

        if (nombre.length() > 20) {
            String message = LanguageHandler.getText(language, "crud.not-valid-name").replace("%entity%", "Rango de Usuario").replace("%name%", nombre).replace("%reason%", "Máximo 20 caracteres.");
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        RangoUsuario rangoUsuario = new RangoUsuario(nombre);
        dbManager.save(rangoUsuario);

        String message = LanguageHandler.getText(language, "crud.create").replace("%entity%", "Rango de Usuario");
        PlayerLogger.info(sender, message, (String) null);
        return true;
    }
    
}
