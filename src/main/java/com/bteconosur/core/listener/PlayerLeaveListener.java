package com.bteconosur.core.listener;

import java.util.Date;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import com.bteconosur.core.chat.ChatService;
import com.bteconosur.core.chat.ChatUtil;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PlayerRegistry;

public class PlayerLeaveListener implements Listener {

    private final PlayerRegistry playerRegistry;

    public PlayerLeaveListener() {
        playerRegistry = PlayerRegistry.getInstance();
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        event.quitMessage(null);
        if (playerRegistry.exists(event.getPlayer().getUniqueId())) {
            Player player = playerRegistry.get(event.getPlayer().getUniqueId());
            player.setFechaUltimaConexion(new Date());
            playerRegistry.merge(player.getUuid());
            
            YamlConfiguration config = ConfigHandler.getInstance().getConfig();
            if (config.getBoolean("discord-player-join-leave")) ChatService.broadcastEmbed(
                ChatUtil.getDsPlayerLeft(player.getNombrePublico(), player.getUuid()),
                ChatUtil.getMcPlayerLeft(player.getNombrePublico())
            );
        }
    }
}
