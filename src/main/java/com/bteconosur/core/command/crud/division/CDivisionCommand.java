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
        super("create", "Crear una nueva Division. Se crea sin poligono.", "<nombre> <nam> <gna> <id_pais> <contexto>", CommandMode.BOTH);

        ConfigHandler configHandler = ConfigHandler.getInstance();
        lang = configHandler.getLang();
        dbManager = DBManager.getInstance();
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        if (args.length != 5) {
            String message = lang.getString("help-command-usage").replace("%command%", getFullCommand().replace(" " + command, ""));
            PlayerLogger.info(sender, message, (String) null);
            return true;
        }

        String nombre = args[0];
        String nombrePublico = args[1];
        String tipoDivision = args[2];
        String contexto;
                
        if (nombre.length() > 100) {
            String message = lang.getString("crud-not-valid-name").replace("%entity%", "Division").replace("%name%", nombre).replace("%reason%", "Máximo 100 caracteres.");
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        if (nombrePublico.length() > 100) {
            String message = lang.getString("crud-not-valid-name").replace("%entity%", "Division").replace("%name%", nombrePublico).replace("%reason%", "Máximo 100 caracteres.");
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        if (tipoDivision.length() > 100) {
            String message = lang.getString("crud-not-valid-type").replace("%entity%", "Division").replace("%type%", tipoDivision).replace("%reason%", "Máximo 50 caracteres.");
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        Long paisId;
        try {
            paisId = Long.parseLong(args[3]);
        } catch (NumberFormatException ex) {
            String message = lang.getString("crud-not-valid-id").replace("%entity%", "Division").replace("%id%", args[3]);
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        if (!dbManager.exists(Pais.class, paisId)) {
            String message = lang.getString("crud-read-not-found").replace("%entity%", "Division").replace("%id%", args[3]);
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        StringBuilder contextoBuilder = new StringBuilder();
        for (int i = 4; i < args.length; i++) {
            if (i > 4) contextoBuilder.append(" ");
            contextoBuilder.append(args[i]);
        }
        contexto = contextoBuilder.toString();

        if (contexto.length() > 100) {
            PlayerLogger.error(sender, lang.getString("invalid-project-description"), (String) null);
            return true;
        }

        Pais pais = dbManager.get(Pais.class, paisId);
        Division division = new Division(pais, nombrePublico, nombre, tipoDivision, tipoDivision + " " + nombre , contexto); //TODO: poligono desde geojson
        dbManager.save(division);

        String message = lang.getString("crud-create").replace("%entity%", "Division");
        PlayerLogger.info(sender, message, (String) null);
        return true;
    }
    
}
