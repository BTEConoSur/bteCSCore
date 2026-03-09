package com.bteconosur.core.command;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.menu.HelpVisitarMenu;
import com.bteconosur.db.model.Player;

public class HelpVisitarCommand extends BaseCommand {

    public HelpVisitarCommand() {
        super("visitar", "", "btecs.command.help", CommandMode.PLAYER_ONLY);
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player player = Player.getBTECSPlayer((org.bukkit.entity.Player) sender);
        org.bukkit.entity.Player bukkitPlayer = (org.bukkit.entity.Player) sender;
        HelpVisitarMenu menu = new HelpVisitarMenu(bukkitPlayer, LanguageHandler.getText(player.getLanguage(), "gui-titles.help-visitar"));
        menu.open();
        return true;
    }

}
