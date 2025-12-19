package com.bteconosur.core.command.btecs.crud.player;

import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.BTEConoSur;
import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.db.DBManager;
import com.bteconosur.db.model.Player;

public class DPlayerCommand extends BaseCommand {

    private final YamlConfiguration lang;
    private final DBManager dbManager;

    public DPlayerCommand() {
        super("delete", "Eliminar un Player.", "<uuid>", CommandMode.BOTH);
        ConfigHandler configHandler = ConfigHandler.getInstance();
        lang = configHandler.getLang();
        dbManager = DBManager.getInstance();
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        if (args.length != 1) {
            String message = lang.getString("help-command-usage").replace("%command%", getFullCommand().replace(" " + command, ""));
            sender.sendMessage(message);
            return true;
        }

        UUID uuid;
        try {
            uuid = UUID.fromString(args[0]);
        } catch (IllegalArgumentException ex) {
            String message = lang.getString("crud-not-valid-id").replace("%entity%", "Player").replace("%id%", args[0]);
            sender.sendMessage(message);
            return true;
        }

        if (!dbManager.exists(Player.class, uuid)) {
            String message = lang.getString("crud-read-not-found").replace("%entity%", "Player").replace("%id%", args[0]);
            sender.sendMessage(message);
            return true;
        }

        Player player = dbManager.get(Player.class, uuid);
        if (player != null) {
            dbManager.remove(player);
        }

        String message = lang.getString("crud-delete").replace("%entity%", "Player").replace("%id%", args[0]);
        BTEConoSur.getConsoleLogger().debug(message, player);
        sender.sendMessage(message);
        return true;
    }
}
