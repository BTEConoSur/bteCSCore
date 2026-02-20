package com.bteconosur.core.command.config;

import java.util.UUID;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.menu.config.PromoteMenu;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PlayerRegistry;

public class PromoteCommand extends BaseCommand {

    public PromoteCommand() {
        super("promote", "Cambiar el Rango de Usuario de un jugador.", "<uuid/nombre_jugador>", "btecs.command.promote", CommandMode.PLAYER_ONLY);
        this.addSubcommand(new GenericHelpCommand(this));
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

        String title = LanguageHandler.replaceMC("gui-titles.promote", language, targetPlayer);
        PromoteMenu menu = new PromoteMenu(commandPlayer, title);
        menu.setBTECSPlayer(targetPlayer);
        menu.open();

        return true;
    }
}
