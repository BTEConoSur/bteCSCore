package com.bteconosur.core.command.pwarp;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.world.WorldManager;
import com.bteconosur.world.model.BTEWorld;

public class PwarpCreateCommand extends BaseCommand {

    public PwarpCreateCommand() {
        super("create", "<nombre_warp>", CommandMode.PLAYER_ONLY);
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        PlayerRegistry registry = PlayerRegistry.getInstance();
        Player player = registry.get(sender);
        Language language = player.getLanguage();
        if (args.length != 1) {
            String message = LanguageHandler.getText(language, "help-command-usage").replace("%comando%", getFullCommand().replace(" " + command, ""));
            PlayerLogger.info(sender, message, (String) null);
            return true;
        }
        String nombreWarp = args[0];   
        org.bukkit.entity.Player bukkitPlayer = (org.bukkit.entity.Player) sender;
        Location loc = bukkitPlayer.getLocation();
        BTEWorld world = WorldManager.getInstance().getBTEWorld();
        if (world.isLobbyLocation(loc)) {
            PlayerLogger.error(player, LanguageHandler.getText(language, "pwarp.cant-create"), (String) null);
            return true;
        }
        if (player.hasPwarp(nombreWarp)) {
            PlayerLogger.error(player, LanguageHandler.getText(language, "pwarp.already").replace("%nombre%", nombreWarp), (String) null);
            return true;
        }

        if (nombreWarp.length() > 50) {
            PlayerLogger.error(player, LanguageHandler.getText(language, "pwarp.invalid-name-length"), (String) null);
            return true;
        }

        if (nombreWarp.toLowerCase().equals("add") || nombreWarp.toLowerCase().equals("remove") || nombreWarp.toLowerCase().equals("help")) {
            PlayerLogger.error(player, LanguageHandler.getText(language, "pwarp.invalid-name"), (String) null);
            return true;
        }
        registry.createPwarp(player.getUuid(), nombreWarp, loc);
        PlayerLogger.info(player, LanguageHandler.getText(language, "pwarp.created").replace("%nombre%", nombreWarp), (String) null);
        return true;
    }

}
