package com.bteconosur.core.command.btecs.crud.pais;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.DBManager;
import com.bteconosur.db.model.Pais;

public class RPaisCommand extends BaseCommand {

    private final YamlConfiguration lang;
    private final DBManager dbManager;

    public RPaisCommand() {
        super("read", "Obtener País.", "<id>", CommandMode.BOTH);
        
        ConfigHandler configHandler = ConfigHandler.getInstance();
        lang = configHandler.getLang();
        dbManager = DBManager.getInstance();
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        if (args.length != 1) {
            String message = lang.getString("help-command-usage").replace("%command%", getFullCommand().replace(" " + command, ""));
            PlayerLogger.info(sender, message, (String) null);
            return true;
        }

        Long id;

        try {
            id = Long.parseLong(args[0]);
        } catch (NumberFormatException exception) {
            String message = lang.getString("crud-not-valid-id").replace("%entity%", "Pais").replace("%id%", args[0]);
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        if (!dbManager.exists(Pais.class, id)) {
            String message = lang.getString("crud-read-not-found").replace("%entity%", "Pais").replace("%id%", args[0]);
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }
        
        Pais pais = dbManager.get(Pais.class, id);

        String message = lang.getString("crud-read").replace("%entity%", "Pais").replace("%id%", args[0]);
        PlayerLogger.info(sender, message, (String) null, pais);
        return true;
    }
    
}
