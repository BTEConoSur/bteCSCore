package com.bteconosur.core.command.btecs.crud.pais;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.DBManager;
import com.bteconosur.db.model.Pais;

public class CPaisCommand extends BaseCommand {

    private final YamlConfiguration lang;
    private final DBManager dbManager;

    public CPaisCommand() {
        super("create", "Crear un nuevo País.", "<nombre> <nombre_publico> <ds_id_guild> <ds_id_global_chat> <ds_id_country_chat> <ds_id_log> <ds_id_request>", CommandMode.BOTH);

        ConfigHandler configHandler = ConfigHandler.getInstance();
        lang = configHandler.getLang();
        dbManager = DBManager.getInstance();
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        if (args.length != 6) {
            String message = lang.getString("help-command-usage").replace("%command%", getFullCommand().replace(" " + command, ""));
            PlayerLogger.info(sender, message, (String) null);
            return true;
        }

        String nombre = args[0];
        String nombrePublico = args[1];
        Long dsIdGuild, dsIdGlobalChat, dsIdCountryChat, dsIdLog, dsIdRequest;

        if (nombre.length() > 50) {
            String message = lang.getString("crud-not-valid-name").replace("%entity%", "Pais").replace("%name%", nombre).replace("%reason%", "Máximo 50 caracteres.");
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        if (nombrePublico.length() > 50) {
            String message = lang.getString("crud-not-valid-name").replace("%entity%", "Pais").replace("%name%", nombrePublico).replace("%reason%", "Máximo 50 caracteres.");
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        try {
            dsIdGuild = Long.parseLong(args[2]);
            dsIdGlobalChat = Long.parseLong(args[3]);
            dsIdCountryChat = Long.parseLong(args[4]);
            dsIdLog = Long.parseLong(args[5]);
            dsIdRequest = Long.parseLong(args[6]);
        } catch (NumberFormatException ex) {
            String message = lang.getString("crud-not-valid-parse").replace("%entity%", "Pais").replace("%value%", "uno de los argumentos").replace("%type%", "Long");
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        Pais pais = new Pais(nombre, nombrePublico, dsIdGuild, dsIdGlobalChat, dsIdCountryChat, dsIdLog, dsIdRequest);
        dbManager.save(pais);

        String message = lang.getString("crud-create").replace("%entity%", "Pais");
        PlayerLogger.info(sender, message, (String) null);
        return true;
    }
    
}
