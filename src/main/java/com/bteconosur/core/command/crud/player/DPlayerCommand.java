package com.bteconosur.core.command.crud.player;

import java.util.UUID;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.DBManager;
import com.bteconosur.db.model.Player;

public class DPlayerCommand extends BaseCommand {

    private final DBManager dbManager;

    public DPlayerCommand() {
        super("delete", "Eliminar un Player.", "<uuid>", CommandMode.BOTH);
        dbManager = DBManager.getInstance();
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player commandPlayer = Player.getBTECSPlayer((org.bukkit.entity.Player) sender);
        Language language = commandPlayer.getLanguage();
        if (args.length != 1) {
            String message = LanguageHandler.getText(language, "help-command-usage").replace("%comando%", getFullCommand().replace(" " + command, ""));
            PlayerLogger.info(sender, message, (String) null);
            return true;
        }

        UUID uuid;
        try {
            uuid = UUID.fromString(args[0]);
        } catch (IllegalArgumentException ex) {
            String message = LanguageHandler.getText(language, "crud.not-valid-id").replace("%entity%", "Player").replace("%id%", args[0]);
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        if (!dbManager.exists(Player.class, uuid)) {
            String message = LanguageHandler.getText(language, "crud.read-not-found").replace("%entity%", "Player").replace("%id%", args[0]);
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        Player player = dbManager.get(Player.class, uuid);
        if (player != null) {
            dbManager.remove(player);
        }

        String message = LanguageHandler.getText(language, "crud.delete").replace("%entity%", "Player").replace("%id%", args[0]);
        PlayerLogger.info(sender, message, (String) null, player);
        return true;
    }
}
