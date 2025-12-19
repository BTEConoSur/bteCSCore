package com.bteconosur.core.command.btecs.crud.player.update;

import java.util.Date;
import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.BTEConoSur;
import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.db.DBManager;
import com.bteconosur.db.model.Player;

public class UPlayerFechaIngresoCommand extends BaseCommand {

    private final YamlConfiguration lang;
    private final DBManager dbManager;

    public UPlayerFechaIngresoCommand() {
        super("fechaingreso", "Actualizar fecha de ingreso (UNIX ms) de un Player.", "<uuid> <valor>", CommandMode.BOTH);
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
        long millis;
        try {
            uuid = UUID.fromString(args[0]);
        } catch (IllegalArgumentException ex) {
            String message = lang.getString("crud-not-valid-id").replace("%entity%", "Player").replace("%id%", args[0]);
            sender.sendMessage(message);
            return true;
        }

        try {
            millis = Long.parseLong(args[1]);
        } catch (NumberFormatException ex) {
            String message = lang.getString("crud-not-valid-parse").replace("%entity%", "Player").replace("%value%", args[1]).replace("%type%", "Long (UNIX ms)");
            sender.sendMessage(message);
            return true;
        }

        if (!dbManager.exists(Player.class, uuid)) {
            String message = lang.getString("crud-read-not-found").replace("%entity%", "Player").replace("%id%", args[0]);
            sender.sendMessage(message);
            return true;
        }

        Player player = dbManager.get(Player.class, uuid);
        player.setFechaIngreso(new Date(millis));
        dbManager.merge(player);

        String message = lang.getString("crud-update").replace("%entity%", "Player").replace("%id%", args[0]);
        BTEConoSur.getConsoleLogger().debug(message, player);
        sender.sendMessage(message);
        return true;
    }
}
