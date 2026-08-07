package com.bteconosur.core.command.preset;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PlayerRegistry;

public class PresetCommand extends BaseCommand {

    public PresetCommand() {
        super("preset", "<subcomando>", "btecs.command.preset", CommandMode.PLAYER_ONLY);
        this.addSubcommand(new PresetCreateCommand());
        this.addSubcommand(new PresetRemoveCommand());
        this.addSubcommand(new PresetListCommand());
        this.addSubcommand(new PresetEditCommand());
        this.addSubcommand(new PresetAddCommand());
        this.addSubcommand(new PresetSeeCommand());
        this.addSubcommand(new GenericHelpCommand(this));
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player commandPlayer = PlayerRegistry.getInstance().get(sender);
        String message = LanguageHandler.getText(commandPlayer.getLanguage(), "help-command-usage").replace("%comando%", getFullCommand());
        PlayerLogger.info(sender, message, (String) null);
        return true;
    }

}