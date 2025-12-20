package com.bteconosur.core.listener;

import java.util.Date;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.bteconosur.db.PermissionManager;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.db.registry.RangoUsuarioRegistry;
import com.bteconosur.db.registry.TipoUsuarioRegistry;

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
        Player player;
        if (!playerRegistry.exists(event.getPlayer().getUniqueId())) {
            player = new Player(
                event.getPlayer().getUniqueId(),
                event.getPlayer().getName(),
                new Date(),
                tipoUsuarioRegistry.getVisita(),
                rangoUsuarioRegistry.getNormal()
            );
            playerRegistry.load(player);
        } else {
            player = playerRegistry.get(event.getPlayer().getUniqueId());
            player.setNombre(event.getPlayer().getName());
            playerRegistry.merge(player.getUuid());
        }

        permissionManager.checkTipoUsuario(player);
        permissionManager.checkRangoUsuario(player);
    }
}
