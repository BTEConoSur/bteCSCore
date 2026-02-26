package com.bteconosur.core.command;

import org.bukkit.command.CommandSender;
import org.mvplugins.multiverse.core.MultiverseCoreApi;

import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.model.Player;
import com.bteconosur.world.WorldManager;

public class LobbyCommand extends BaseCommand {

    public LobbyCommand() {
        super("lobby", "", "btecs.command.lobby", CommandMode.PLAYER_ONLY);
        this.addSubcommand(new GenericHelpCommand(this));
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player player = Player.getBTECSPlayer((org.bukkit.entity.Player) sender);
        org.bukkit.entity.Player bukkitPlayer = (org.bukkit.entity.Player) sender;
        MultiverseCoreApi multiverseApi = WorldManager.getInstance().getBTEWorld().getMultiverseApi();
        PlayerLogger.info(player, LanguageHandler.getText(player.getLanguage(), "lobby-teleport"), (String) null);
        bukkitPlayer.teleport(multiverseApi.getWorldManager().getLoadedWorld(config.getString("lobby.world")).get().getSpawnLocation());
        return true;
    }

}
