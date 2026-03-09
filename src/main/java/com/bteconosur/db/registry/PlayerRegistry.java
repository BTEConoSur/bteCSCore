package com.bteconosur.db.registry;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;

import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.ConsoleLogger;
import com.bteconosur.db.model.Division;
import com.bteconosur.db.model.Pais;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.Pwarp;

/**
 * Registro de jugadores cargados y utilidades de consulta por estado.
 */
public class PlayerRegistry extends Registry<UUID, Player> {

    private static PlayerRegistry instance;

    private static Map<UUID, Location> lastPlayerLocations = new ConcurrentHashMap<>(); 

    /**
     * Inicializa el registro y carga jugadores persistidos.
     */
    public PlayerRegistry() {
        super();
        ConsoleLogger.info(LanguageHandler.getText("player-registry-initializing"));
        loadedObjects = new ConcurrentHashMap<>();
        List<Player> players = dbManager.selectAll(Player.class);
        if (players != null) for (Player p : players) loadedObjects.put(p.getUuid(), p);
    }

    /**
     * Carga un jugador en persistencia y memoria.
     *
     * @param obj jugador a cargar.
     */
    @Override
    public void load(Player obj) {
        if (obj == null || obj.getUuid() == null) return;
        dbManager.save(obj);
        loadedObjects.put(obj.getUuid(), obj);
    }

    /**
     * Obtiene el jugador interno asociado a un emisor de comando.
     *
     * @param sender emisor de comando.
     * @return jugador encontrado, o {@code null} si no aplica.
     */
    public Player get(CommandSender sender) {
        if (sender == null) return null;
        if (!(sender instanceof org.bukkit.entity.Player)) return null;
        return get(((org.bukkit.entity.Player) sender).getUniqueId());
    }

    /**
     * Cierra el registro y limpia la cache en memoria.
     */
    public void shutdown() {
        ConsoleLogger.info(LanguageHandler.getText("player-registry-shutting-down"));
        loadedObjects.clear();
        loadedObjects = null;
    }

    /**
     * Elimina la última ubicación almacenada de un jugador.
     *
     * @param uuid uuid del jugador.
     */
    public static void removeLastLocation(UUID uuid) {
        if (uuid == null) return;
        lastPlayerLocations.remove(uuid);
    }

    /**
     * Actualiza la última ubicación conocida de un jugador.
     *
     * @param uuid uuid del jugador.
     * @param location ubicación a guardar.
     */
    public static void updateLastLocation(UUID uuid, Location location) {
        if (uuid == null || location == null) return;
        lastPlayerLocations.put(uuid, location);
    }

    /**
     * Obtiene la última ubicación conocida de un jugador.
     *
     * @param uuid uuid del jugador.
     * @return ubicación almacenada, o {@code null}.
     */
    public static Location getLastLocation(UUID uuid) {
        if (uuid == null) return null;
        return lastPlayerLocations.get(uuid);
    }

    /**
     * Busca un jugador por su ID de usuario de Discord.
     *
     * @param discordUserId id de usuario de Discord.
     * @return jugador encontrado, o {@code null}.
     */
    public Player findByDiscordId(Long discordUserId) {
        if (discordUserId == null) return null;
        for (Player cached : loadedObjects.values()) {
            if (discordUserId.equals(cached.getDsIdUsuario())) return cached;
        }

        List<Player> results = dbManager.findByProperty(Player.class, "dsIdUsuario", discordUserId);
        if (results == null || results.isEmpty()) return null;

        Player found = results.get(0);
        loadedObjects.put(found.getUuid(), found);
        return found;
    }

    /**
     * Busca un jugador por nombre de Minecraft.
     *
     * @param playerName nombre del jugador.
     * @return jugador encontrado, o {@code null}.
     */
    public Player findByName(String playerName) {
        if (playerName == null) return null;
        for (Player cached : loadedObjects.values()) {
            if (playerName.equals(cached.getNombre())) return cached;
        }

        List<Player> results = dbManager.findByProperty(Player.class, "nombre", playerName);
        if (results == null || results.isEmpty()) return null;

        Player found = results.get(0);
        loadedObjects.put(found.getUuid(), found);
        return found;
    }

    /**
     * Obtiene los reviewers de un país.
     *
     * @param pais país de referencia.
     * @return lista de reviewers.
     */
    public List<Player> getReviewers(Pais pais) {
        return loadedObjects.values()
                .stream()
                .filter(player -> player.getPaisesReviewer().contains(pais))
                .collect(Collectors.toList());
    }

    /**
     * Obtiene los managers de un país.
     *
     * @param pais país de referencia.
     * @return lista de managers.
     */
    public List<Player> getManagers(Pais pais) {
        return loadedObjects.values()
                .stream()
                .filter(player -> player.getPaisesManager().contains(pais))
                .collect(Collectors.toList());
    }

    /**
     * Obtiene los jugadores conectados ordenados por nombre público.
     *
     * @return lista de jugadores conectados.
     */
    public List<Player> getOnlinePlayers() {
        return Bukkit.getOnlinePlayers()
                .stream()
                .map(player -> get(player.getUniqueId()))
                .sorted(Comparator.comparing(Player::getNombrePublico))
                .collect(Collectors.toList());
    }

    /**
     * Obtiene la cantidad total de jugadores conectados.
     *
     * @return cantidad de jugadores online.
     */
    public int getOnlinePlayersCount() {
        return Bukkit.getOnlinePlayers().size();
    }

    /**
     * Obtiene la cantidad de jugadores conectados dentro de un país.
     *
     * @param pais país de referencia.
     * @return cantidad de jugadores online en dicho país.
     */
    public int getOnlinePlayersCount(Pais pais) {
        PaisRegistry pr = PaisRegistry.getInstance();
        int count = 0;
        for (Player player : getOnlinePlayers()) {
            Division division = pr.findDivisionByPlayer(player.getUuid());
            if (division == null) continue;
            if (division.getPais().equals(pais)) count++;
        }
        return count;
    }

    /**
     * Obtiene jugadores desconectados ordenados por última conexión.
     *
     * @return lista de jugadores offline limitada por configuración.
     */
    public List<Player> getOfflinePlayers() {
        return loadedObjects.values()
                .stream()
                .filter(player -> !isOnline(player.getUuid()))
                .sorted(Comparator.comparing(Player::getFechaUltimaConexion, 
                        Comparator.nullsFirst(Comparator.naturalOrder())).reversed())
                .limit(config.getInt("max-offline-players-list", 100))
                .collect(Collectors.toList());
    }

    /**
     * Crea un pwarp para un jugador.
     *
     * @param uuid uuid del jugador.
     * @param nombreWarp nombre del pwarp.
     * @param loc ubicación del pwarp.
     */
    public void createPwarp(UUID uuid, String nombreWarp, Location loc) {
        Player player = get(uuid);
        if (player == null) return;
        Pwarp newPwarp = new Pwarp(
            player.getUuid(), 
            nombreWarp,
            player,
            loc.getX(), loc.getY(), loc.getZ(),
            loc.getYaw(), loc.getPitch()
        );
        player.addPwarp(newPwarp);
        PlayerRegistry.getInstance().merge(player.getUuid());
    }

    /**
     * Elimina un pwarp de un jugador por nombre.
     *
     * @param uuid uuid del jugador.
     * @param nombreWarp nombre del pwarp.
     */
    public void removePwarp(UUID uuid, String nombreWarp) {
        Player player = get(uuid);
        if (player == null) return;
        Pwarp pwarp = player.getPwarp(nombreWarp);
        player.removePwarp(pwarp);
        PlayerRegistry.getInstance().merge(player.getUuid());
    }

    /**
     * Verifica si un jugador está conectado al servidor.
     *
     * @param uuid uuid del jugador.
     * @return {@code true} si está online.
     */
    public boolean isOnline(UUID uuid) {
        if (uuid == null) return false;
        return Bukkit.getPlayer(uuid) != null;
    }

    /**
     * Obtiene la instancia singleton de {@code PlayerRegistry}.
     *
     * @return instancia única del registro.
     */
    public static PlayerRegistry getInstance() {
        if (instance == null) {
            instance = new PlayerRegistry();
        }
        return instance;
    }

}
