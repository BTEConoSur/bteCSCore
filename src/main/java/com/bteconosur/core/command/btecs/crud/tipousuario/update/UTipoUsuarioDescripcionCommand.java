package com.bteconosur.core.command.btecs.crud.tipousuario.update;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.db.DBManager;
import com.bteconosur.db.model.TipoUsuario;

public class UTipoUsuarioDescripcionCommand extends BaseCommand {

    private final YamlConfiguration lang;
    private final DBManager dbManager;

    public UTipoUsuarioDescripcionCommand() {
        super("descripcion", "Actualizar descripción de un TipoUsuario.", "<id> <descripcion>", CommandMode.BOTH);
        ConfigHandler configHandler = ConfigHandler.getInstance();
        lang = configHandler.getLang();
        dbManager = DBManager.getInstance();
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            String message = lang.getString("help-command-usage").replace("%command%", getFullCommand().replace(" " + command, ""));
            sender.sendMessage(message);
            return true;
        }

        Long id;
        try {
            id = Long.parseLong(args[0]);
        } catch (NumberFormatException ex) {
            String message = lang.getString("crud-not-valid-id").replace("%entity%", "TipoUsuario").replace("%id%", args[0]);
            sender.sendMessage(message);
            return true;
        }

        if (!dbManager.exists(TipoUsuario.class, id)) {
            String message = lang.getString("crud-read-not-found").replace("%entity%", "TipoUsuario").replace("%id%", args[0]);
            sender.sendMessage(message);
            return true;
        }

        StringBuilder descripcionBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            if (i > 1) descripcionBuilder.append(" ");
            descripcionBuilder.append(args[i]);
        }
        String nuevaDescripcion = descripcionBuilder.toString();
        if (nuevaDescripcion.length() > 500) {
            String message = lang.getString("crud-not-valid-name").replace("%entity%", "TipoUsuario").replace("%name%", nuevaDescripcion).replace("%reason%", "Máximo 500 caracteres.");
            sender.sendMessage(message);
            return true;
        }

        TipoUsuario tipoUsuario = dbManager.get(TipoUsuario.class, id);
        tipoUsuario.setDescripcion(nuevaDescripcion);
        dbManager.merge(tipoUsuario);

        String message = lang.getString("crud-update").replace("%entity%", "TipoUsuario").replace("%id%", args[0]);
        sender.sendMessage(message);
        return true;
    }
}
