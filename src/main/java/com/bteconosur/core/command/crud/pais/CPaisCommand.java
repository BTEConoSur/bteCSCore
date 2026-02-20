package com.bteconosur.core.command.crud.pais;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.DBManager;
import com.bteconosur.db.model.Pais;
import com.bteconosur.db.model.Player;

public class CPaisCommand extends BaseCommand {

    private final DBManager dbManager;

    public CPaisCommand() {
        super("create", "Crear un nuevo País.", "<nombre> <nombre_publico> <ds_id_guild> <ds_id_global_chat> <ds_id_country_chat> <ds_id_log> <ds_id_request>", CommandMode.BOTH);
        dbManager = DBManager.getInstance();
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player commandPlayer = Player.getBTECSPlayer((org.bukkit.entity.Player) sender);
        Language language = commandPlayer.getLanguage();
        if (args.length != 6) {
            String message = LanguageHandler.getText(language, "help-command-usage").replace("%comando%", getFullCommand().replace(" " + command, ""));
            PlayerLogger.info(sender, message, (String) null);
            return true;
        }

        String nombre = args[0];
        String nombrePublico = args[1];
        Long dsIdGuild, dsIdGlobalChat, dsIdCountryChat, dsIdLog, dsIdRequest;

        if (nombre.length() > 50) {
            String message = LanguageHandler.getText(language, "crud.not-valid-name").replace("%entity%", "Pais").replace("%name%", nombre).replace("%reason%", "Máximo 50 caracteres.");
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        if (nombrePublico.length() > 50) {
            String message = LanguageHandler.getText(language, "crud.not-valid-name").replace("%entity%", "Pais").replace("%name%", nombrePublico).replace("%reason%", "Máximo 50 caracteres.");
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
            String message = LanguageHandler.getText(language, "crud.not-valid-parse").replace("%entity%", "Pais").replace("%value%", "uno de los argumentos").replace("%type%", "Long");
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        Pais pais = new Pais(nombre, nombrePublico, dsIdGuild, dsIdGlobalChat, dsIdCountryChat, dsIdLog, dsIdRequest);
        dbManager.save(pais);

        String message = LanguageHandler.getText(language, "crud.create").replace("%entity%", "Pais");
        PlayerLogger.info(sender, message, (String) null);
        return true;
    }
    
}
