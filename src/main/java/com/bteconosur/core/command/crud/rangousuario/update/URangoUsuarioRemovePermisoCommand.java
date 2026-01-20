package com.bteconosur.core.command.crud.rangousuario.update;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.DBManager;
import com.bteconosur.db.model.NodoPermiso;
import com.bteconosur.db.model.RangoUsuario;

public class URangoUsuarioRemovePermisoCommand extends BaseCommand {

    private final YamlConfiguration lang;
    private final DBManager dbManager;

    public URangoUsuarioRemovePermisoCommand() {
        super("removepermiso", "Remover permiso de un RangoUsuario.", "<id_rango> <permiso>", CommandMode.BOTH);
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
            String message = lang.getString("crud-not-valid-id").replace("%entity%", "RangoUsuario").replace("%id%", args[0]);
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        if (!dbManager.exists(RangoUsuario.class, id)) {
            String message = lang.getString("crud-read-not-found").replace("%entity%", "RangoUsuario").replace("%id%", args[0]);
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        String permisoNombre = args[1];
        List<NodoPermiso> existentes = dbManager.findByProperty(NodoPermiso.class, "nombre", permisoNombre);
        if (existentes == null || existentes.isEmpty()) {
            PlayerLogger.error(sender, "El permiso no existe.", (String) null);
            return true;
        }

        NodoPermiso permiso = existentes.get(0);
        RangoUsuario rango = dbManager.get(RangoUsuario.class, id);

        if (!rango.getPermisos().contains(permiso)) {
            PlayerLogger.error(sender, "El RangoUsuario no posee ese permiso.", (String) null);
            return true;
        }

        rango.getPermisos().remove(permiso);
        dbManager.merge(rango);

        String message = lang.getString("crud-update").replace("%entity%", "RangoUsuario").replace("%id%", args[0]);
        PlayerLogger.info(sender, message, (String) null);
        return true;
    }
}
