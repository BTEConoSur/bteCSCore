package com.bteconosur.core.command.btecs.test;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.menu.TestPaginatedMenu;

public class TestPaginatedMenuCommand extends BaseCommand {

    public TestPaginatedMenuCommand() {
        super("paginatedmenu", null, "btecs.command.test", CommandMode.PLAYER_ONLY);
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        
        TestPaginatedMenu menu = new TestPaginatedMenu(player);
        menu.open();
        
        return true;
    }
}