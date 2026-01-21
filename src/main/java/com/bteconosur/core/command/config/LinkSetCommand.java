package com.bteconosur.core.command.config;

import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.chat.ChatUtil;
import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.discord.util.LinkService;

public class LinkSetCommand extends BaseCommand {

    private final YamlConfiguration lang;

    public LinkSetCommand() {
        super("set", "Linkear la cuenta de Discord a un jugador.", "<uuid/nombre> <discordId>", "btecs.command.link.set");
        lang = ConfigHandler.getInstance().getLang();
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        if (args.length != 2) {
            String message = lang.getString("help-command-usage").replace("%command%", getFullCommand().replace(" " + command, ""));
            PlayerLogger.info(sender, message, (String) null);
            return true;
        }

        UUID uuid;
        Long dsId;
        Player targetPlayer;
        PlayerRegistry playerRegistry = PlayerRegistry.getInstance();

        try{
            uuid = UUID.fromString(args[0]);
            targetPlayer = playerRegistry.get(uuid);
        } catch (IllegalArgumentException exception){
            targetPlayer = playerRegistry.findByName(args[0]);
        }

        if (targetPlayer == null) {
            String message = lang.getString("player-not-registered").replace("%player%", args[0]);
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        if (LinkService.isPlayerLinked(targetPlayer)) {
            PlayerLogger.error(sender, lang.getString("minecraft-already-linked"), (String) null);
            return true;
        }

        try {
            dsId = Long.parseLong(args[1]);
        } catch (NumberFormatException ex) {
            String message = lang.getString("crud-not-valid-parse").replace("%entity%", "Player").replace("%value%", args[1]).replace("%type%", "Long");
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        if(!LinkService.isValidUserId(dsId)) {
            PlayerLogger.error(sender, lang.getString("invalid-discord-id"), (String) null);
            return true;
        }
        
        Player commandPlayer = playerRegistry.get(((org.bukkit.entity.Player) sender).getUniqueId());
        targetPlayer = LinkService.link(dsId, targetPlayer);
        PlayerLogger.info(targetPlayer, lang.getString("minecraft-link-success"), ChatUtil.getDsLinkSuccess(targetPlayer.getNombre()));
        if (targetPlayer != commandPlayer) {
            String message = lang.getString("link-set-success").replace("%player%", targetPlayer.getNombre());
            PlayerLogger.info(sender, message, (String) null);
        }

        return true;
    }

}
