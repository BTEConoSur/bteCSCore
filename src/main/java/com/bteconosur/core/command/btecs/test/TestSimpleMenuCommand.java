package com.bteconosur.core.command.btecs.test;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.menu.TestSimpleMenu;

public class TestSimpleMenuCommand extends BaseCommand {

    public TestSimpleMenuCommand() {
        super("simplemenu", "Abre un menú simple de prueba", null, CommandMode.PLAYER_ONLY);
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        
        TestSimpleMenu menu = new TestSimpleMenu(player);
        menu.open();
        return true;
    }
}