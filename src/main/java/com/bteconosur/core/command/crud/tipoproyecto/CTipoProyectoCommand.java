package com.bteconosur.core.command.crud.tipoproyecto;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.DBManager;
import com.bteconosur.db.model.TipoProyecto;

public class CTipoProyectoCommand extends BaseCommand {

    private final YamlConfiguration lang;
    private final DBManager dbManager;

    public CTipoProyectoCommand() {
        super("create", "Crear un nuevo TipoProyecto.", "<nombre> <max_miembros> <tamaño_min> <tamaño_max>", CommandMode.BOTH);

        ConfigHandler configHandler = ConfigHandler.getInstance();
        lang = configHandler.getLang();
        dbManager = DBManager.getInstance();
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        if (args.length != 4) {
            String message = lang.getString("help-command-usage").replace("%command%", getFullCommand().replace(" " + command, ""));
            PlayerLogger.info(sender, message, (String) null);
            return true;
        }

        String nombre = args[0];
        Integer maxMiembros;
        Integer tamanoMin;
        Integer tamanoMax;
        
        if (nombre.length() > 50) {
            String message = lang.getString("crud-not-valid-name").replace("%entity%", "TipoProyecto").replace("%name%", nombre).replace("%reason%", "Máximo 50 caracteres.");
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        try {
            maxMiembros = Integer.parseInt(args[1]);
        } catch (NumberFormatException ex) {
            String message = lang.getString("crud-not-valid-parse").replace("%entity%", "max_miembros").replace("%value%", args[1]).replace("%type%", "Integer");
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        try {
            tamanoMin = Integer.parseInt(args[2]);
        } catch (NumberFormatException ex) {
            String message = lang.getString("crud-not-valid-parse").replace("%entity%", "tamaño_min").replace("%value%", args[2]).replace("%type%", "Integer");
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        try {
            tamanoMax = Integer.parseInt(args[3]);
        } catch (NumberFormatException ex) {
            String message = lang.getString("crud-not-valid-parse").replace("%entity%", "tamaño_max").replace("%value%", args[3]).replace("%type%", "Integer");
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        if (tamanoMin > tamanoMax) {
            String message = lang.getString("crud-not-valid-name").replace("%entity%", "TipoProyecto").replace("%name%", "Tamaños").replace("%reason%", "El tamaño mínimo no puede ser mayor al máximo.");
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        TipoProyecto tipoProyecto = new TipoProyecto(nombre, maxMiembros, tamanoMin, tamanoMax);
        dbManager.save(tipoProyecto);

        String message = lang.getString("crud-create").replace("%entity%", "TipoProyecto");
        PlayerLogger.info(sender, message, (String) null);
        return true;
    }
    
}
