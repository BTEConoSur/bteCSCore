package com.bteconosur.core.command.config;

import java.util.UUID;

import org.bukkit.command.CommandSender;
import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.menu.ConfirmationMenu;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.discord.util.LinkService;

public class UnlinkSetCommand extends BaseCommand {

    private ConfirmationMenu confirmationMenu;

    public UnlinkSetCommand() {
        super("set", "Desvincular la cuenta de Discord de un jugador.", "<uuid/nombre>", "btecs.command.unlink.set");
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

        if (!LinkService.isPlayerLinked(targetPlayer)) {
            PlayerLogger.error(sender, LanguageHandler.getText(language, "link.mc-not-linked"), (String) null);
            return true;
        }
        
        org.bukkit.entity.Player bukkitPlayer = commandPlayer.getBukkitPlayer();
        final Player finalTargetPlayer = targetPlayer;
        confirmationMenu = new ConfirmationMenu(LanguageHandler.getText(language, "gui-titles.unlink-confirm"), bukkitPlayer, confirmClick -> {
                Player newTargetPlayer = LinkService.unlink(finalTargetPlayer);
                PlayerLogger.info(newTargetPlayer, LanguageHandler.getText(newTargetPlayer.getLanguage(), "link.unlink-success"), (String) null);
                if (!newTargetPlayer.equals(commandPlayer)) {
                    String message = LanguageHandler.replaceMC("link.unlink-set-success", language, finalTargetPlayer);
                    PlayerLogger.info(sender, message, (String) null);
                }
                confirmationMenu.getGui().close(bukkitPlayer);
            }, (cancelClick -> {
                confirmationMenu.getGui().close(bukkitPlayer);
        }));
        confirmationMenu.open();
        return true;
    }

}
