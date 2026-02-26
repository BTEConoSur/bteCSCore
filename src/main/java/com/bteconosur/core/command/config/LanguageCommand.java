package com.bteconosur.core.command.config;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.menu.config.LanguageSelectMenu;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PlayerRegistry;

public class LanguageCommand extends BaseCommand {

     public LanguageCommand() {
        super("language", "", "btecs.command.language", CommandMode.PLAYER_ONLY);
        this.addSubcommand(new GenericHelpCommand(this));
        this.addSubcommand(new LanguageSetCommand());
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player player = PlayerRegistry.getInstance().get(((org.bukkit.entity.Player) sender).getUniqueId());
        LanguageSelectMenu menu = new LanguageSelectMenu(player, LanguageHandler.getText(player.getLanguage(), "gui-titles.language-select"));
        menu.open();
        return false;
    }
}
