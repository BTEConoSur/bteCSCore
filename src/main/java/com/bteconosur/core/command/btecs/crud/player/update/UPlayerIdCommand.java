package com.bteconosur.core.command.btecs.crud.player.update;

import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.BTEConoSur;
import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.db.DBManager;
import com.bteconosur.db.model.Player;

public class UPlayerIdCommand extends BaseCommand{

    private final YamlConfiguration lang;
    private final DBManager dbManager;

    public UPlayerIdCommand() {
        super("id", "Actualizar id de un Player.", "<uuid> <valor>", CommandMode.BOTH);
        
        ConfigHandler configHandler = ConfigHandler.getInstance();
        lang = configHandler.getLang();
        dbManager = DBManager.getInstance();
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        // TODO: Enviar por sistema de notificaciones que use help
        if (args.length != 2) {
            String message = lang.getString("help-command-usage").replace("%command%", getFullCommand().replace(" " + command, ""));
            sender.sendMessage(message);
            return true;
        }

        UUID uuid;
        UUID newUuid;

        try {
            uuid = UUID.fromString(args[0]);
        } catch (IllegalArgumentException exception) {
            String message = lang.getString("crud-not-valid-id").replace("%entity%", "Player").replace("%id%", args[0]);
            sender.sendMessage(message);
            return true;
        }

        try {
            newUuid = UUID.fromString(args[1]);
        } catch (IllegalArgumentException exception) {
            String message = lang.getString("crud-not-valid-id").replace("%entity%", "Player").replace("%id%", args[1]);
            sender.sendMessage(message);
            return true;
        }

        if(!dbManager.exists(Player.class, uuid)) {
            String message = lang.getString("crud-read-not-found").replace("%entity%", "Player").replace("%id%", args[0]);
            sender.sendMessage(message);
            return true;
        }

        if(dbManager.exists(Player.class, newUuid)) {
            String message = lang.getString("crud-already-exists").replace("%entity%", "Player").replace("%id%", args[1]);
            sender.sendMessage(message);
            return true;
        }
        
        Player player = dbManager.get(Player.class, uuid);
        player.setUuid(newUuid);
        dbManager.merge(player);

        String message = lang.getString("crud-update").replace("%entity%", "Player").replace("%id%", args[0]); //TODO: Notificacion con objeto.
        BTEConoSur.getConsoleLogger().debug(message, player);
        sender.sendMessage(message);
        return true;
    }
}
