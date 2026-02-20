package com.bteconosur.core.command.crud.tipousuario;

import java.util.stream.Collectors;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.DBManager;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.TipoUsuario;

public class GetTipoUsuarioListPermisosCommand extends BaseCommand {

    private final DBManager dbManager;

    public GetTipoUsuarioListPermisosCommand() {
        super("listpermisos", "Listar permisos de un Tipo de Usuario.", "<id_tipo>", CommandMode.BOTH);
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
            String message = LanguageHandler.getText(language, "crud.not-valid-id").replace("%entity%", "Tipo de Usuario").replace("%id%", args[0]);
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        TipoUsuario tipo = dbManager.get(TipoUsuario.class, id);
        if (tipo == null) {
            String message = LanguageHandler.getText(language, "crud.read-not-found").replace("%entity%", "Tipo de Usuario").replace("%id%", args[0]);
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        if (tipo.getPermisos().isEmpty()) {
            String emptyMsg = LanguageHandler.getText(language, "get-list.empty").replace("%entity%", "permisos");
            PlayerLogger.warn(sender, emptyMsg, (String) null);
            return true;
        }

        String lista = tipo.getPermisos().stream()
            .map(p -> p.getNombre())
            .sorted()
            .collect(Collectors.joining(", "));
        String message = LanguageHandler.getText(language, "permisos-list").replace("%permisos%", lista);
        PlayerLogger.info(sender, message, (String) null);
        return true;
    }
}
