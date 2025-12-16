package com.bteconosur.core.listener;

import java.util.Date;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.bteconosur.db.DBManager;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.TipoUsuario;

public class PlayerJoinListener implements Listener {

    private final DBManager dbManager;

    public PlayerJoinListener() {
        this.dbManager = DBManager.getInstance();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!dbManager.exists(TipoUsuario.class, 2)) {
            dbManager.save(new TipoUsuario("Default", "Testeo", 10));
        }
        TipoUsuario tipoUsuario = dbManager.get(TipoUsuario.class, 2); // TipoUsuario por defecto);

        if (!dbManager.exists(Player.class, event.getPlayer().getUniqueId())) {
            Date now = new Date();
            Player newPlayer = new Player(
                event.getPlayer().getUniqueId(),
                event.getPlayer().getName(),
                now,
                tipoUsuario
            );
            newPlayer.setNombrePublico(event.getPlayer().getName());
            newPlayer.setFechaIngreso(now);
            dbManager.save(newPlayer);
        } else {
            Player player = dbManager.get(Player.class, event.getPlayer().getUniqueId());
            player.setNombre(event.getPlayer().getName());
            dbManager.merge(player  );
        }
    }
}
