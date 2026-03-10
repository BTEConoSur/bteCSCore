package com.bteconosur.world.listener;

import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import com.bteconosur.core.config.ConfigHandler;
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
    private static final YamlConfiguration config = ConfigHandler.getInstance().getConfig();

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
        org.bukkit.entity.Player bukkitPlayer = event.getPlayer();
        worldManager.checkMove(event.getTo(), bukkitPlayer);
        worldManager.checkTitles(event.getTo(), bukkitPlayer);
        if (!worldManager.checkPaisMove(event.getFrom(), event.getTo(), bukkitPlayer)) {
            event.setCancelled(true);
            Player player = Player.getBTECSPlayer(event.getPlayer());
            PlayerLogger.warn(player, LanguageHandler.getText(player.getLanguage(), "cant-leave-paises"), (String) null);
        }
        if (bukkitPlayer.getY() <= bukkitPlayer.getWorld().getMinHeight() || bukkitPlayer.getY() >= bukkitPlayer.getWorld().getMaxHeight()) {
            bukkitPlayer.teleport(WorldManager.getInstance().getBTEWorld().getMultiverseApi().getWorldManager().getLoadedWorld(config.getString("lobby.world")).get().getSpawnLocation());
            Player player = Player.getBTECSPlayer(bukkitPlayer);
            PlayerLogger.warn(player, LanguageHandler.getText(player.getLanguage(), "world-height-limit"), (String) null);
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
        org.bukkit.entity.Player bukkitPlayer = event.getPlayer();
        PlayerRegistry.updateLastLocation(bukkitPlayer.getUniqueId(), event.getFrom());
        if (event.getFrom().getWorld() != event.getTo().getWorld()) return;
        worldManager.checkMove(event.getTo(), bukkitPlayer);
        if (!worldManager.checkPaisMove(event.getFrom(), event.getTo(), bukkitPlayer)) {
            event.setCancelled(true);
            Player player = Player.getBTECSPlayer(bukkitPlayer);
            PlayerLogger.warn(player, LanguageHandler.getText(player.getLanguage(), "cant-leave-paises"), (String) null);
        }
        if (!bukkitPlayer.isFlying()) {
            event.setTo(new Location(event.getTo().getWorld(), event.getTo().getX(), event.getTo().getY() + 0.1, event.getTo().getZ(), event.getTo().getYaw(), event.getTo().getPitch()));
            bukkitPlayer.setFlying(true);
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
