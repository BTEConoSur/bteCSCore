package com.bteconosur.core.listener;

import java.util.Date;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.bteconosur.core.chat.GlobalChatService;
import com.bteconosur.core.chat.ChatService;
import com.bteconosur.core.chat.ChatUtil;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.util.ConfigurationService;
import com.bteconosur.db.PermissionManager;
import com.bteconosur.db.model.Configuration;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.db.registry.RangoUsuarioRegistry;
import com.bteconosur.db.registry.TipoUsuarioRegistry;

import net.dv8tion.jda.api.entities.MessageEmbed;

public class PlayerJoinListener implements Listener {

    private final PlayerRegistry playerRegistry;
    private final TipoUsuarioRegistry tipoUsuarioRegistry;
    private final RangoUsuarioRegistry rangoUsuarioRegistry;
    private final PermissionManager permissionManager;

    public PlayerJoinListener() {
        playerRegistry = PlayerRegistry.getInstance();
        tipoUsuarioRegistry = TipoUsuarioRegistry.getInstance();
        rangoUsuarioRegistry = RangoUsuarioRegistry.getInstance();
        permissionManager = PermissionManager.getInstance();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.joinMessage(null);
        
        Player player;
        if (!playerRegistry.exists(event.getPlayer().getUniqueId())) {
            player = new Player(
                event.getPlayer().getUniqueId(),
                event.getPlayer().getName(),
                new Date(),
                tipoUsuarioRegistry.getVisita(),
                rangoUsuarioRegistry.getNormal()
            );
            player.setConfiguration(new Configuration(player));
            playerRegistry.load(player);
            ConfigurationService.setDefaults(player.getUuid());
        } else {
            player = playerRegistry.get(event.getPlayer().getUniqueId());
            player.setNombre(event.getPlayer().getName());
            playerRegistry.merge(player.getUuid());
        }

        YamlConfiguration config = ConfigHandler.getInstance().getConfig();

        MessageEmbed dsMessage = ChatUtil.getDsPlayerJoined(player.getNombrePublico(), player.getUuid());
        String mcMessage = ChatUtil.getMcPlayerJoined(player.getNombrePublico());

        if (config.getBoolean("discord-player-join-leave")) GlobalChatService.broadcastEmbed(dsMessage, mcMessage);
        else GlobalChatService.broadcastMc(mcMessage);
        GlobalChatService.broadcastMc(mcMessage);
        
        if (player.getConfiguration().getGeneralGlobalChatOnJoin()) ChatService.setChatToGlobal(player);
        else if (ChatService.wasInCountryChat(player)) ChatService.setCountryChat(player);
        else ChatService.setChatToGlobal(player);
        
        permissionManager.checkTipoUsuario(player);
        permissionManager.checkRangoUsuario(player);
    }
}
