package com.bteconosur.core.command.crud.tipousuario.update;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.DBManager;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.TipoUsuario;

public class UTipoUsuarioDescripcionCommand extends BaseCommand {

    private final DBManager dbManager;

    public UTipoUsuarioDescripcionCommand() {
        super("descripcion", "Actualizar descripción de un Tipo de Usuario.", "<id> <descripcion>", CommandMode.BOTH);
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
            String message = LanguageHandler.getText(language, "crud.not-valid-id").replace("%entity%", "Tipo de Usuario").replace("%id%", args[0]);
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        if (!dbManager.exists(TipoUsuario.class, id)) {
            String message = LanguageHandler.getText(language, "crud.read-not-found").replace("%entity%", "Tipo de Usuario").replace("%id%", args[0]);
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        StringBuilder descripcionBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            if (i > 1) descripcionBuilder.append(" ");
            descripcionBuilder.append(args[i]);
        }
        String nuevaDescripcion = descripcionBuilder.toString();
        if (nuevaDescripcion.length() > 500) {
            String message = LanguageHandler.getText(language, "crud.not-valid-description").replace("%entity%", "Tipo de Usuario").replace("%name%", nuevaDescripcion).replace("%reason%", "Máximo 500 caracteres.");
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        TipoUsuario tipoUsuario = dbManager.get(TipoUsuario.class, id);
        tipoUsuario.setDescripcion(nuevaDescripcion);
        dbManager.merge(tipoUsuario);

        String message = LanguageHandler.getText(language,"crud.update").replace("%entity%", "Tipo de Usuario").replace("%id%", args[0]);
        PlayerLogger.info(sender, message, (String) null);
        return true;
    }
}
