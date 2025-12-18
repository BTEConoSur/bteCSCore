package com.bteconosur.core.command.btecs.crud.pais.update;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.db.DBManager;
import com.bteconosur.db.model.Pais;

public class UPaisDsIdRequestCommand extends BaseCommand {

    private final YamlConfiguration lang;
    private final DBManager dbManager;

    public UPaisDsIdRequestCommand() {
        super("dsidrequest", "Actualizar Discord Request ID de un País.", "<id> <ds_id_request>", CommandMode.BOTH);
        ConfigHandler configHandler = ConfigHandler.getInstance();
        lang = configHandler.getLang();
        dbManager = DBManager.getInstance();
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        if (args.length != 2) {
            String message = lang.getString("help-command-usage").replace("%command%", getFullCommand().replace(" " + command, ""));
            sender.sendMessage(message);
            return true;
        }

        Long id;
        try {
            id = Long.parseLong(args[0]);
        } catch (NumberFormatException ex) {
            String message = lang.getString("crud-not-valid-id").replace("%entity%", "Pais").replace("%id%", args[0]);
            sender.sendMessage(message);
            return true;
        }

        if (!dbManager.exists(Pais.class, id)) {
            String message = lang.getString("crud-read-not-found").replace("%entity%", "Pais").replace("%id%", args[0]);
            sender.sendMessage(message);
            return true;
        }

        Long nuevoId;
        try {
            nuevoId = Long.parseLong(args[1]);
        } catch (NumberFormatException ex) {
            String message = lang.getString("crud-not-valid-parse").replace("%entity%", "Pais").replace("%value%", args[1]).replace("%type%", "Long");
            sender.sendMessage(message);
            return true;
        }

        Pais pais = dbManager.get(Pais.class, id);
        pais.setDsIdRequest(nuevoId);
        dbManager.merge(pais);

        String message = lang.getString("crud-update").replace("%entity%", "Pais").replace("%id%", args[0]);
        sender.sendMessage(message);
        return true;
    }
}
