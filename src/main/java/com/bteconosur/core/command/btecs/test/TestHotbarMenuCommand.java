package com.bteconosur.core.command.btecs.test;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.menu.TestHotbarMenu;

public class TestHotbarMenuCommand extends BaseCommand {

    public TestHotbarMenuCommand() {
        super("hotbar", null, "btecs.command.btecs.test", CommandMode.PLAYER_ONLY);
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        new TestHotbarMenu(player).open();;
        return true;
    }

}
