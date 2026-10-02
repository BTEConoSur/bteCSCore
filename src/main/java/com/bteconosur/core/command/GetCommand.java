package com.bteconosur.core.command;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.menu.GetMenu;
import com.bteconosur.core.menu.HotbarMenu;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.model.Player;

public class GetCommand extends BaseCommand{

    public GetCommand() {
        super("get", "", "btecs.command.get", CommandMode.PLAYER_ONLY);
        this.addSubcommand(new GenericHelpCommand(this));
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player player = Player.getBTECSPlayer((org.bukkit.entity.Player) sender);
        if (HotbarMenu.hasActive(player.getUuid())) {
            PlayerLogger.warn(sender, LanguageHandler.getText(player.getLanguage(), "invalid-command-moment"), (String) null);
            return true;
        }
        GetMenu menu = new GetMenu(player, LanguageHandler.getText(player.getLanguage(), "gui-titles.get-menu"));
        menu.open();
        return true;
    }

}
