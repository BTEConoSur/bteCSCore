package com.bteconosur.core.command.crud.tipoproyecto.update;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.DBManager;
import com.bteconosur.db.model.TipoProyecto;

public class UTipoProyectoTamañoMinCommand extends BaseCommand {

    private final YamlConfiguration lang;
    private final DBManager dbManager;

    public UTipoProyectoTamañoMinCommand() {
        super("tamañomin", "Actualizar tamaño mínimo de un TipoProyecto.", "<id> <tamaño>", CommandMode.BOTH);
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
            String message = lang.getString("crud-not-valid-id").replace("%entity%", "TipoProyecto").replace("%id%", args[0]);
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        if (!dbManager.exists(TipoProyecto.class, id)) {
            String message = lang.getString("crud-read-not-found").replace("%entity%", "TipoProyecto").replace("%id%", args[0]);
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        Integer nuevoTamano;
        try {
            nuevoTamano = Integer.parseInt(args[1]);
        } catch (NumberFormatException ex) {
            String message = lang.getString("crud-not-valid-parse").replace("%entity%", "tamaño_min").replace("%value%", args[1]).replace("%type%", "Integer");
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        TipoProyecto tipoProyecto = dbManager.get(TipoProyecto.class, id);
        
        if (nuevoTamano > tipoProyecto.getTamanoMax()) {
            String message = lang.getString("crud-not-valid-name").replace("%entity%", "TipoProyecto").replace("%name%", "Tamaño mínimo").replace("%reason%", "No puede ser mayor al tamaño máximo.");
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        tipoProyecto.setTamanoMin(nuevoTamano);
        dbManager.merge(tipoProyecto);

        String message = lang.getString("crud-update").replace("%entity%", "TipoProyecto").replace("%id%", args[0]);
        PlayerLogger.info(sender, message, (String) null);
        return true;
    }
}
