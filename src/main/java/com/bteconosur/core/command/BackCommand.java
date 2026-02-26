package com.bteconosur.core.command;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;

import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PlayerRegistry;

public class BackCommand extends BaseCommand {

    public BackCommand() {
        super("back", "", "btecs.command.back", CommandMode.PLAYER_ONLY);
        this.addSubcommand(new GenericHelpCommand(this));
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        PlayerRegistry pr = PlayerRegistry.getInstance();
        Player commandPlayer = pr.get(sender);
        Language language = commandPlayer.getLanguage();
        org.bukkit.entity.Player bukkitPlayer = (org.bukkit.entity.Player) sender;
        Location lastLocation = PlayerRegistry.getLastLocation(commandPlayer.getUuid());
        if (lastLocation == null) {
            PlayerLogger.error(commandPlayer, LanguageHandler.getText(language, "back.no-location"), (String) null);
            return true;
        }
        bukkitPlayer.teleport(lastLocation);
        PlayerRegistry.removeLastLocation(commandPlayer.getUuid());
        PlayerLogger.info(commandPlayer, LanguageHandler.getText(language, "back.teleported"), (String) null);
        return true;
    }

}
