package com.bteconosur.core.command.preset;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.core.menu.preset.PresetListMenu;
import com.bteconosur.core.util.PlayerLogger;

public class PresetListCommand extends BaseCommand {

    public PresetListCommand() {
        super("list", "[nombre_jugador]", "btecs.command.preset", CommandMode.PLAYER_ONLY);
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player commandPlayer = PlayerRegistry.getInstance().get(sender);
        Language language = commandPlayer.getLanguage();

        if (args.length > 1) {
            String message = LanguageHandler.getText(language, "help-command-usage").replace("%comando%", getFullCommand());
            PlayerLogger.info(sender, message, (String) null);
            return true;
        }

        PlayerRegistry playerRegistry = PlayerRegistry.getInstance();
        if (args.length == 1) {
            Player targetPlayer = playerRegistry.findByName(args[0]);
            if (targetPlayer == null) {
                String message = LanguageHandler.getText(language, "player-not-found").replace("%player%", args[0]);
                PlayerLogger.error(commandPlayer, message, (String) null);
                return true;
            }
            if (!commandPlayer.equals(targetPlayer)) {
                if (targetPlayer.getPresets().isEmpty()) {
                    String message = LanguageHandler.replaceMC("preset.empty-other", language, targetPlayer);
                    PlayerLogger.info(commandPlayer, message, (String) null);
                    return true;
                }
                PresetListMenu menu = new PresetListMenu(commandPlayer, targetPlayer);
                menu.open();
                return true;
            }
        }

        if (commandPlayer.getPresets().isEmpty()) {
            String message = LanguageHandler.getText(language, "preset.empty");
            PlayerLogger.info(sender, message, (String) null);
            return true;
        }
        PresetListMenu menu = new PresetListMenu(commandPlayer);
        menu.open();
        return true;
    }

}
