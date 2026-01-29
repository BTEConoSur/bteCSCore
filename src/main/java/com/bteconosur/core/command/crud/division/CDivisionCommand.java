package com.bteconosur.core.command.crud.division;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.DBManager;
import com.bteconosur.db.model.Division;
import com.bteconosur.db.model.Pais;

public class CDivisionCommand extends BaseCommand {

    private final YamlConfiguration lang;
    private final DBManager dbManager;

    public CDivisionCommand() {
        super("create", "Crear una nueva Division. Se crea sin poligono.", "<nombre> <nombre_publico> <id_pais>", CommandMode.BOTH);

        ConfigHandler configHandler = ConfigHandler.getInstance();
        lang = configHandler.getLang();
        dbManager = DBManager.getInstance();
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        if (args.length != 3) {
            String message = lang.getString("help-command-usage").replace("%command%", getFullCommand().replace(" " + command, ""));
            PlayerLogger.info(sender, message, (String) null);
            return true;
        }

        String nombre = args[0];
        String nombrePublico = args[1];
        
        if (nombre.length() > 50) {
            String message = lang.getString("crud-not-valid-name").replace("%entity%", "Division").replace("%name%", nombre).replace("%reason%", "Máximo 50 caracteres.");
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        if (nombrePublico.length() > 50) {
            String message = lang.getString("crud-not-valid-name").replace("%entity%", "Division").replace("%name%", nombrePublico).replace("%reason%", "Máximo 50 caracteres.");
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        Long paisId;
        try {
            paisId = Long.parseLong(args[2]);
        } catch (NumberFormatException ex) {
            String message = lang.getString("crud-not-valid-id").replace("%entity%", "Pais").replace("%id%", args[2]);
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        if (!dbManager.exists(Pais.class, paisId)) {
            String message = lang.getString("crud-read-not-found").replace("%entity%", "Pais").replace("%id%", args[2]);
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        Pais pais = dbManager.get(Pais.class, paisId);
        Division division = new Division(pais, nombre, nombrePublico, null);
        dbManager.save(division);

        String message = lang.getString("crud-create").replace("%entity%", "Division");
        PlayerLogger.info(sender, message, (String) null);
        return true;
    }
    
}
