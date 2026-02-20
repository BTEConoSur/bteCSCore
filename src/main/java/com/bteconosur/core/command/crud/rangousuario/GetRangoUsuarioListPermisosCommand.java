package com.bteconosur.core.command.crud.rangousuario;

import java.util.stream.Collectors;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.DBManager;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.RangoUsuario;

public class GetRangoUsuarioListPermisosCommand extends BaseCommand {

    private final DBManager dbManager;

    public GetRangoUsuarioListPermisosCommand() {
        super("listpermisos", "Listar permisos de un Rango de Usuario.", "<id_rango>", CommandMode.BOTH);
        dbManager = DBManager.getInstance();
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player commandPlayer = Player.getBTECSPlayer((org.bukkit.entity.Player) sender);
        Language language = commandPlayer.getLanguage();
        if (args.length != 1) {
            String message = LanguageHandler.getText(language, "help-command-usage").replace("%comando%", getFullCommand().replace(" " + command, ""));
            PlayerLogger.error(sender, message, (String) null);
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

        RangoUsuario rango = dbManager.get(RangoUsuario.class, id);
        if (rango == null) {
            String message = LanguageHandler.getText(language, "crud.read-not-found").replace("%entity%", "Rango de Usuario").replace("%id%", args[0]);
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        if (rango.getPermisos().isEmpty()) {
            String emptyMsg = LanguageHandler.getText(language, "get-list.empty").replace("%entity%", "permisos");
            PlayerLogger.warn(sender, emptyMsg, (String) null);
            return true;
        }

        String lista = rango.getPermisos().stream()
            .map(p -> p.getNombre())
            .sorted()
            .collect(Collectors.joining(", "));
        String message = LanguageHandler.getText(language, "permisos-list").replace("%permisos%", lista);
        PlayerLogger.info(sender, message, (String) null);
        return true;
    }
}
