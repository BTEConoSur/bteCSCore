package com.bteconosur.world.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.world.WorldManager;

/**
 * Listener que gestiona el movimiento y teletransporte de jugadores.
 * Controla cambios de capa, títulos de ubicación y límites de países.
 */
public class MovingListeners implements Listener {

    private final WorldManager worldManager;

    public MovingListeners() {
        this.worldManager = WorldManager.getInstance();
    }

    /**
     * Maneja el movimiento del jugador, verificando cambios de capa, títulos y límites de países.
     * Cancela el movimiento si el jugador intenta salir de los límites permitidos.
     * 
     * @param event Evento de movimiento del jugador
     */
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        worldManager.checkMove(event.getTo(), event.getPlayer());
        worldManager.checkTitles(event.getTo(), event.getPlayer());
        if (!worldManager.checkPaisMove(event.getFrom(), event.getTo(), event.getPlayer())) {
            event.setCancelled(true);
            Player player = Player.getBTECSPlayer(event.getPlayer());
            PlayerLogger.warn(player, LanguageHandler.getText(player.getLanguage(), "cant-leave-paises"), (String) null);
        }
    }

    /**
     * Maneja el teletransporte del jugador, actualizando la última ubicación y verificando límites.
     * Cancela el teletransporte si el jugador intenta salir de los límites permitidos.
     * 
     * @param event Evento de teletransporte del jugador
     */
    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        PlayerRegistry.updateLastLocation(event.getPlayer().getUniqueId(), event.getFrom());
        if (event.getFrom().getWorld() != event.getTo().getWorld()) return;
        worldManager.checkMove(event.getTo(), event.getPlayer());
        if (!worldManager.checkPaisMove(event.getFrom(), event.getTo(), event.getPlayer())) {
            event.setCancelled(true);
            Player player = Player.getBTECSPlayer(event.getPlayer());
            PlayerLogger.warn(player, LanguageHandler.getText(player.getLanguage(), "cant-leave-paises"), (String) null);
        }
    }

    /**
     * Limpia los datos del jugador cuando se desconecta.
     * Remueve la última ubicación y cancela tareas programadas.
     * 
     * @param event Evento de desconexión del jugador
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        PlayerRegistry.removeLastLocation(event.getPlayer().getUniqueId());
        worldManager.getBTEWorld().clearPlayerTasks(event.getPlayer().getUniqueId()); 
    }

}
