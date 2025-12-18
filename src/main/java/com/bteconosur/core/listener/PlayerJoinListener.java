package com.bteconosur.core.listener;

import java.util.Date;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.bteconosur.db.DBManager;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.TipoUsuario;
import com.bteconosur.db.registry.PlayerRegistry;

public class PlayerJoinListener implements Listener {

    private final DBManager dbManager;
    private final PlayerRegistry playerRegistry;

    public PlayerJoinListener() {
        dbManager = DBManager.getInstance();
        playerRegistry = PlayerRegistry.getInstance();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!dbManager.exists(TipoUsuario.class, 2)) {
            dbManager.save(new TipoUsuario("Default", "Testeo", 10));
        }
        TipoUsuario tipoUsuario = dbManager.get(TipoUsuario.class, 2); // TipoUsuario por defecto);

        if (!playerRegistry.exists(event.getPlayer().getUniqueId())) {
            Player newPlayer = new Player(
                event.getPlayer().getUniqueId(),
                event.getPlayer().getName(),
                new Date(),
                tipoUsuario
            );
            playerRegistry.load(newPlayer);
        } else {
            Player player = playerRegistry.get(event.getPlayer().getUniqueId());
            player.setNombre(event.getPlayer().getName());
            playerRegistry.merge(player.getUuid());
        }
    }
}
