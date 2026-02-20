package com.bteconosur.core.command.crud.tipousuario.update;

import java.util.List;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.DBManager;
import com.bteconosur.db.model.NodoPermiso;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.TipoUsuario;

public class UTipoUsuarioAddPermisoCommand extends BaseCommand {

    private final DBManager dbManager;

    public UTipoUsuarioAddPermisoCommand() {
        super("addpermiso", "Agregar permiso a un Tipo de Usuario.", "<id_tipo> <permiso>", CommandMode.BOTH);
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
            String message = LanguageHandler.getText(language, "crud.not-valid-id").replace("%entity%", "Tipo de Usuario").replace("%id%", args[0]);
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        if (!dbManager.exists(TipoUsuario.class, id)) {
            String message = LanguageHandler.getText(language, "crud.read-not-found").replace("%entity%", "Tipo de Usuario").replace("%id%", args[0]);
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        String permisoNombre = args[1];
        if (permisoNombre.length() > 100) {
            String message = LanguageHandler.getText(language, "crud.not-valid-name").replace("%entity%", "Nodo Permiso").replace("%name%", permisoNombre).replace("%reason%", "Máximo 100 caracteres.");
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        TipoUsuario tipo = dbManager.get(TipoUsuario.class, id);

        List<NodoPermiso> existentes = dbManager.findByProperty(NodoPermiso.class, "nombre", permisoNombre);
        NodoPermiso permiso;
        if (existentes != null && !existentes.isEmpty()) {
            permiso = existentes.get(0);
        } else {
            permiso = new NodoPermiso(permisoNombre);
            dbManager.save(permiso);
        }

        if (tipo.getPermisos().contains(permiso)) {
            String message = LanguageHandler.getText(language, "crud.tipo-already-permiso");
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        tipo.getPermisos().add(permiso);
        dbManager.merge(tipo);

        String message = LanguageHandler.getText(language, "crud.update").replace("%entity%", "Tipo de Usuario").replace("%id%", args[0]);
        PlayerLogger.info(sender, message, (String) null);
        return true;
    }
}
