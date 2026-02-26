package com.bteconosur.core.command;

import java.util.List;
import java.util.UUID;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.db.util.PlaceholderUtils;

public class PlayerCommand extends BaseCommand {

    public PlayerCommand() {
        super("player", "<uuid/nombre_reviewer>", "btecs.command.player", CommandMode.PLAYER_ONLY);
        this.addSubcommand(new GenericHelpCommand(this));
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player player = Player.getBTECSPlayer((org.bukkit.entity.Player) sender);
        Language language = player.getLanguage();
        if (args.length != 1) {
            String message = LanguageHandler.getText(language, "help-command-usage").replace("%comando%", getFullCommand());
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
            String message = LanguageHandler.getText(language, "player-not-found").replace("%player%", args[0]);
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        List<String> infoLines = LanguageHandler.getTextList(language, "player-info-command");
        StringBuilder processed = new StringBuilder();
        for (String line : infoLines) {
            if (!processed.isEmpty()) {
                processed.append("\n");
            }
            processed.append(PlaceholderUtils.replaceMC(line, language, targetPlayer).replace("%plugin-prefix%", LanguageHandler.getText(language, "plugin-prefix")));
        }

        PlayerLogger.send(sender, processed.toString(), (String) null);
        return true;
    }

}