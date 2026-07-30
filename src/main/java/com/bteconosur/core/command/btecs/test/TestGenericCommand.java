package com.bteconosur.core.command.btecs.test;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bteconosur.core.command.BaseCommand;

import net.kyori.adventure.text.Component;


public class TestGenericCommand extends BaseCommand {

    public TestGenericCommand() {
        super("generic", null, "btecs.command.btecs.test", CommandMode.PLAYER_ONLY);
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        player.playerListName(Component.text("§b[Admin] " + player.getName()));
        return true;
    }



}