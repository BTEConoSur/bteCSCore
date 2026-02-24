package com.bteconosur.core.command;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.menu.GetMenu;
import com.bteconosur.db.model.Player;

public class GetCommand extends BaseCommand{

    public GetCommand() {
        super("get", "");
        this.addSubcommand(new GenericHelpCommand(this));
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player player = Player.getBTECSPlayer((org.bukkit.entity.Player) sender);
        GetMenu menu = new GetMenu(player, LanguageHandler.getText(player.getLanguage(), "gui-titles.get-menu"));
        menu.open();
        return true;
    }

}
