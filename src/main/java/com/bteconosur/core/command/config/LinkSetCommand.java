package com.bteconosur.core.command.config;

import java.util.UUID;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.chat.ChatUtil;
import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.discord.util.LinkService;

public class LinkSetCommand extends BaseCommand {

    public LinkSetCommand() {
        super("set", "Linkear la cuenta de Discord a un jugador.", "<uuid/nombre> <discordId>", "btecs.command.link.set");
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player player = Player.getBTECSPlayer((org.bukkit.entity.Player) sender);
        Language language = player.getLanguage();
        if (args.length != 2) {
            String message = LanguageHandler.getText(language, "help-command-usage").replace("%comando%", getFullCommand().replace(" " + command, ""));
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
            String message = LanguageHandler.getText(language, "player-not-registered").replace("%player%", args[0]);
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        if (LinkService.isPlayerLinked(targetPlayer)) {
            PlayerLogger.error(sender, LanguageHandler.getText(language, "link.mc-already-linked"), (String) null);
            return true;
        }

        try {
            dsId = Long.parseLong(args[1]);
        } catch (NumberFormatException ex) {
            String message = LanguageHandler.getText(language, "crud.not-valid-parse").replace("%entity%", "Player").replace("%value%", args[1]).replace("%type%", "Long");
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        if(!LinkService.isValidUserId(dsId)) {
            PlayerLogger.error(sender, LanguageHandler.getText(language, "invalid-discord-id"), (String) null);
            return true;
        }
        
        Player commandPlayer = playerRegistry.get(((org.bukkit.entity.Player) sender).getUniqueId());
        targetPlayer = LinkService.link(dsId, targetPlayer);
        PlayerLogger.info(targetPlayer, LanguageHandler.getText(targetPlayer.getLanguage(), "link.mc-success"), ChatUtil.getDsLinkSuccess(targetPlayer));
        if (!targetPlayer.equals(commandPlayer)) {
            String message = LanguageHandler.replaceMC("link.set-success", language, targetPlayer);
            PlayerLogger.info(sender, message, (String) null);
        }

        return true;
    }

}
