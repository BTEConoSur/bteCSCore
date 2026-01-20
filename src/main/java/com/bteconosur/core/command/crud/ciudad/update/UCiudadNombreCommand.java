package com.bteconosur.core.command.crud.ciudad.update;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.DBManager;
import com.bteconosur.db.model.Ciudad;

public class UCiudadNombreCommand extends BaseCommand {

    private final YamlConfiguration lang;
    private final DBManager dbManager;

    public UCiudadNombreCommand() {
        super("nombre", "Actualizar nombre de una Ciudad.", "<id> <nuevo_nombre>", CommandMode.BOTH);
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
            String message = lang.getString("crud-not-valid-id").replace("%entity%", "Ciudad").replace("%id%", args[0]);
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        if (!dbManager.exists(Ciudad.class, id)) {
            String message = lang.getString("crud-read-not-found").replace("%entity%", "Ciudad").replace("%id%", args[0]);
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        String nuevoNombre = args[1];
        if (nuevoNombre.length() > 50) {
            String message = lang.getString("crud-not-valid-name").replace("%entity%", "Ciudad").replace("%name%", nuevoNombre).replace("%reason%", "Máximo 50 caracteres.");
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        Ciudad ciudad = dbManager.get(Ciudad.class, id);
        ciudad.setNombre(nuevoNombre);
        dbManager.merge(ciudad);

        String message = lang.getString("crud-update").replace("%entity%", "Ciudad").replace("%id%", args[0]);
        PlayerLogger.info(sender, message, (String) null);
        return true;
    }

}
