package com.bteconosur.core.command.btecs.crud.rangousuario;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.db.DBManager;
import com.bteconosur.db.model.RangoUsuario;

public class CRangoUsuarioCommand extends BaseCommand {

    private final YamlConfiguration lang;
    private final DBManager dbManager;

    public CRangoUsuarioCommand() {
        super("create", "Crear un nuevo RangoUsuario.", "<nombre> <descripcion>", CommandMode.BOTH);

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

        String nombre = args[0];

        if (nombre.length() > 20) {
            String message = lang.getString("crud-not-valid-name").replace("%entity%", "RangoUsuario").replace("%name%", nombre).replace("%reason%", "Máximo 20 caracteres.");
            sender.sendMessage(message);
            return true;
        }

        // Concatenar todos los argumentos restantes para la descripción
        StringBuilder descripcionBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            if (i > 1) descripcionBuilder.append(" ");
            descripcionBuilder.append(args[i]);
        }
        String descripcion = descripcionBuilder.toString();

        if (descripcion.length() > 500) {
            String message = lang.getString("crud-not-valid-name").replace("%entity%", "RangoUsuario").replace("%name%", descripcion).replace("%reason%", "Máximo 500 caracteres.");
            sender.sendMessage(message);
            return true;
        }

        RangoUsuario rangoUsuario = new RangoUsuario(nombre, descripcion);
        dbManager.save(rangoUsuario);

        String message = lang.getString("crud-create").replace("%entity%", "RangoUsuario");
        sender.sendMessage(message);
        return true;
    }
    
}
