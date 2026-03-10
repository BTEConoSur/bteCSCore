package com.bteconosur.core.command.btecs.test;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bteconosur.core.command.BaseCommand;


public class TestGenericCommand extends BaseCommand {

    public TestGenericCommand() {
        super("generic", null, "btecs.command.btecs.test", CommandMode.PLAYER_ONLY);
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        if (!player.isFlying()) {
            player.teleport(new Location(player.getWorld(), player.getX(), player.getY() + 0.5, player.getZ()));
            player.setFlying(true);
        }
        return true;
    }



}