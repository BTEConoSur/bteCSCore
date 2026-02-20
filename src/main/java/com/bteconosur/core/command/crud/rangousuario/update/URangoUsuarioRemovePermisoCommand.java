package com.bteconosur.core.command.crud.rangousuario.update;

import java.util.List;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.DBManager;
import com.bteconosur.db.model.NodoPermiso;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.RangoUsuario;

public class URangoUsuarioRemovePermisoCommand extends BaseCommand {

    private final DBManager dbManager;

    public URangoUsuarioRemovePermisoCommand() {
        super("removepermiso", "Remover permiso de un RangoUsuario.", "<id_rango> <permiso>", CommandMode.BOTH);
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
            String message = LanguageHandler.getText(language, "crud.not-valid-id").replace("%entity%", "Rango de Usuario").replace("%id%", args[0]);
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        if (!dbManager.exists(RangoUsuario.class, id)) {
            String message = LanguageHandler.getText(language, "crud.read-not-found").replace("%entity%", "Rango de Usuario").replace("%id%", args[0]);
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        String permisoNombre = args[1];
        List<NodoPermiso> existentes = dbManager.findByProperty(NodoPermiso.class, "nombre", permisoNombre);
        if (existentes == null || existentes.isEmpty()) {
            String message = LanguageHandler.getText(language, "crud.permiso-not-found");
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        NodoPermiso permiso = existentes.get(0);
        RangoUsuario rango = dbManager.get(RangoUsuario.class, id);

        if (!rango.getPermisos().contains(permiso)) {
            String message = LanguageHandler.getText(language, "crud.rango-not-have-permiso");
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        rango.getPermisos().remove(permiso);
        dbManager.merge(rango);

        String message = LanguageHandler.getText(language, "crud.update").replace("%entity%", "Rango de Usuario").replace("%id%", args[0]);
        PlayerLogger.info(sender, message, (String) null);
        return true;
    }
}
