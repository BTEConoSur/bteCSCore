package com.bteconosur.core.listener;

import java.util.Date;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import com.bteconosur.db.DBManager;
import com.bteconosur.db.model.Player;

public class PlayerLeaveListener implements Listener {

    private final DBManager dbManager;

    public PlayerLeaveListener() {
        this.dbManager = DBManager.getInstance();
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        if (dbManager.exists(Player.class, event.getPlayer().getUniqueId())) {
            Player player = dbManager.get(Player.class, event.getPlayer().getUniqueId());
            player.setFechaUltimaConexion(new Date());
            dbManager.merge(player);
        }
    }
}
