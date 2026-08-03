package com.bteconosur.core.command.pwarp;

import java.util.Collections;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import com.bteconosur.core.chat.ChatUtil;
import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.world.WorldManager;
import com.bteconosur.world.model.BTEWorld;

public class PwarpEditCommand extends BaseCommand {

    public PwarpEditCommand() {
        super("edit", "<nombre_warp>", "btecs.command.pwarp", CommandMode.PLAYER_ONLY);
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
        if (!player.hasPwarp(nombreWarp)) {
            PlayerLogger.error(player, LanguageHandler.getText(player.getLanguage(), "pwarp.not-found").replace("%nombre%", nombreWarp), (String) null);
            return true;
        }

        if (nombreWarp.length() > 50) {
            PlayerLogger.error(player, LanguageHandler.getText(language, "pwarp.invalid-name-length"), (String) null);
            return true;
        }

        if (nombreWarp.matches(".*<[^>]+>.*")) {
            PlayerLogger.error(sender, LanguageHandler.getText(language, "invalid-regex"), (String) null);
            return true;
        }

        if (ChatUtil.hasBannedChars(nombreWarp)) {
            PlayerLogger.error(sender, LanguageHandler.getText(language, "invalid-chars"), (String) null);
            return true;
        }

        if (nombreWarp.toLowerCase().equals("add") || nombreWarp.toLowerCase().equals("remove") || nombreWarp.toLowerCase().equals("help")
            || nombreWarp.toLowerCase().equals("list") || nombreWarp.toLowerCase().equals("edit")) {
            PlayerLogger.error(player, LanguageHandler.getText(language, "pwarp.invalid-name"), (String) null);
            return true;
        }
        registry.editPwarp(player.getUuid(), nombreWarp, loc);
        PlayerLogger.info(player, LanguageHandler.getText(language, "pwarp.edited").replace("%nombre%", nombreWarp), (String) null);
        return true;
    }

    @Override
    protected List<String> tabCompleteArgs(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) {
        Player player = PlayerRegistry.getInstance().get(sender);
        if (args.length == 1) return player.getPwarpNames().stream().filter(p -> p.toLowerCase().startsWith(args[0].toLowerCase())).toList();
        return Collections.emptyList();
    }

}
