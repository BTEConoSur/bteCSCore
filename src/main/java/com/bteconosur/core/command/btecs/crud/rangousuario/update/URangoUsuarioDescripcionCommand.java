package com.bteconosur.core.command.btecs.crud.rangousuario.update;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.DBManager;
import com.bteconosur.db.model.RangoUsuario;

public class URangoUsuarioDescripcionCommand extends BaseCommand {

    private final YamlConfiguration lang;
    private final DBManager dbManager;

    public URangoUsuarioDescripcionCommand() {
        super("descripcion", "Actualizar descripción de un RangoUsuario.", "<id> <descripcion>", CommandMode.BOTH);
        ConfigHandler configHandler = ConfigHandler.getInstance();
        lang = configHandler.getLang();
        dbManager = DBManager.getInstance();
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            String message = lang.getString("help-command-usage").replace("%command%", getFullCommand().replace(" " + command, ""));
            PlayerLogger.info(sender, message, (String) null);
            return true;
        }

        Long id;
        try {
            id = Long.parseLong(args[0]);
        } catch (NumberFormatException ex) {
            String message = lang.getString("crud-not-valid-id").replace("%entity%", "RangoUsuario").replace("%id%", args[0]);
            PlayerLogger.info(sender, message, (String) null);
            return true;
        }

        if (!dbManager.exists(RangoUsuario.class, id)) {
            String message = lang.getString("crud-read-not-found").replace("%entity%", "RangoUsuario").replace("%id%", args[0]);
            PlayerLogger.info(sender, message, (String) null);
            return true;
        }

        StringBuilder descripcionBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            if (i > 1) descripcionBuilder.append(" ");
            descripcionBuilder.append(args[i]);
        }
        String nuevaDescripcion = descripcionBuilder.toString();

        if (nuevaDescripcion.length() > 500) {
            String message = lang.getString("crud-not-valid-name").replace("%entity%", "RangoUsuario").replace("%name%", nuevaDescripcion).replace("%reason%", "Máximo 500 caracteres.");
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        RangoUsuario rangoUsuario = dbManager.get(RangoUsuario.class, id);
        rangoUsuario.setDescripcion(nuevaDescripcion);
        dbManager.merge(rangoUsuario);

        String message = lang.getString("crud-update").replace("%entity%", "RangoUsuario").replace("%id%", args[0]);
        PlayerLogger.info(sender, message, (String) null);
        return true;
    }
}
