package com.bteconosur.core.command.btecs.crud.player.update;

import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.DBManager;
import com.bteconosur.db.model.Player;

public class UPlayerNombrePublicoCommand extends BaseCommand {

    private final YamlConfiguration lang;
    private final DBManager dbManager;

    public UPlayerNombrePublicoCommand() {
        super("nombrepublico", "Actualizar nombre público de un Player.", "<uuid> <valor>", CommandMode.BOTH);
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

        UUID uuid;
        try {
            uuid = UUID.fromString(args[0]);
        } catch (IllegalArgumentException ex) {
            String message = lang.getString("crud-not-valid-id").replace("%entity%", "Player").replace("%id%", args[0]);
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        if (!dbManager.exists(Player.class, uuid)) {
            String message = lang.getString("crud-read-not-found").replace("%entity%", "Player").replace("%id%", args[0]);
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        String nuevoNombre = args[1];
        if (nuevoNombre.length() > 16) {
            String message = lang.getString("crud-not-valid-name").replace("%entity%", "Player").replace("%name%", nuevoNombre).replace("%reason%", "Máximo 16 caracteres.");
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        Player player = dbManager.get(Player.class, uuid);
        player.setNombrePublico(nuevoNombre);
        dbManager.merge(player);

        String message = lang.getString("crud-update").replace("%entity%", "Player").replace("%id%", args[0]);
        PlayerLogger.info(sender, message, (String) null, player);
        return true;
    }
}
