package com.bteconosur.core.command;

import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.model.Player;

public class AssetsCommand extends BaseCommand {

    public AssetsCommand() {
        super("assets", "", "btecs.command.assets", CommandMode.PLAYER_ONLY);
        this.addSubcommand(new GenericHelpCommand(this));
    }

    @Override
    protected boolean onCommand(org.bukkit.command.CommandSender sender, String[] args) {
        Player player = Player.getBTECSPlayer((org.bukkit.entity.Player) sender);
        org.bukkit.entity.Player bukkitPlayer = (org.bukkit.entity.Player) sender;
        PlayerLogger.info(player, LanguageHandler.getText(player.getLanguage(), "assets-teleport"), (String) null);
        bukkitPlayer.performCommand("/warp assets");
        return true;
    }

}
