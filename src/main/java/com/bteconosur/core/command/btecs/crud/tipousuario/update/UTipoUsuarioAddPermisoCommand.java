package com.bteconosur.core.command.btecs.crud.tipousuario.update;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.DBManager;
import com.bteconosur.db.model.NodoPermiso;
import com.bteconosur.db.model.TipoUsuario;

public class UTipoUsuarioAddPermisoCommand extends BaseCommand {

    private final YamlConfiguration lang;
    private final DBManager dbManager;

    public UTipoUsuarioAddPermisoCommand() {
        super("addpermiso", "Agregar permiso a un TipoUsuario.", "<id_tipo> <permiso>", CommandMode.BOTH);
        ConfigHandler configHandler = ConfigHandler.getInstance();
        lang = configHandler.getLang();
        dbManager = DBManager.getInstance();
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        if (args.length != 2) {
            String message = lang.getString("help-command-usage").replace("%command%", getFullCommand().replace(" " + command, ""));
            PlayerLogger.info(sender, message, (String) null);
            return true;
        }

        Long id;
        try {
            id = Long.parseLong(args[0]);
        } catch (NumberFormatException ex) {
            String message = lang.getString("crud-not-valid-id").replace("%entity%", "TipoUsuario").replace("%id%", args[0]);
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        if (!dbManager.exists(TipoUsuario.class, id)) {
            String message = lang.getString("crud-read-not-found").replace("%entity%", "TipoUsuario").replace("%id%", args[0]);
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        String permisoNombre = args[1];
        if (permisoNombre.length() > 100) {
            String message = lang.getString("crud-not-valid-name").replace("%entity%", "NodoPermiso").replace("%name%", permisoNombre).replace("%reason%", "Máximo 100 caracteres.");
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
            PlayerLogger.error(sender, "El TipoUsuario ya posee ese permiso.", (String) null);
            return true;
        }

        tipo.getPermisos().add(permiso);
        dbManager.merge(tipo);

        String message = lang.getString("crud-update").replace("%entity%", "TipoUsuario").replace("%id%", args[0]);
        PlayerLogger.info(sender, message, (String) null);
        return true;
    }
}
