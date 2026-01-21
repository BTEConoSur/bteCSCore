package com.bteconosur.core.command.config;

import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.menu.ConfirmationMenu;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.discord.util.LinkService;

public class UnlinkSetCommand extends BaseCommand {

    private final YamlConfiguration lang;
    private ConfirmationMenu confirmationMenu;

    public UnlinkSetCommand() {
        super("set", "Desvincular la cuenta de Discord de un jugador.", "<uuid/nombre>", "btecs.command.unlink.set");
        lang = ConfigHandler.getInstance().getLang();
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        if (args.length != 1) {
            String message = lang.getString("help-command-usage").replace("%command%", getFullCommand().replace(" " + command, ""));
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
            String message = lang.getString("player-not-registered").replace("%player%", args[0]);
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        if (!LinkService.isPlayerLinked(targetPlayer)) {
            PlayerLogger.error(sender, lang.getString("minecraft-not-linked"), (String) null);
            return true;
        }
        
        Player commandPlayer = playerRegistry.get(((org.bukkit.entity.Player) sender).getUniqueId());

        org.bukkit.entity.Player bukkitPlayer = commandPlayer.getBukkitPlayer();
        
        final Player finalTargetPlayer = targetPlayer;
        confirmationMenu = new ConfirmationMenu(lang.getString("gui-titles.unlink-confirm"), bukkitPlayer, confirmClick -> {
                Player newTargetPlayer = LinkService.unlink(finalTargetPlayer);
                PlayerLogger.info(newTargetPlayer, lang.getString("minecraft-unlink-success"), (String) null);
                if (newTargetPlayer != commandPlayer) {
                    String message = lang.getString("unlink-set-success").replace("%player%", newTargetPlayer.getNombre());
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
