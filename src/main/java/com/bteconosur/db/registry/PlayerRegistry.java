package com.bteconosur.db.registry;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.ConsoleLogger;
import com.bteconosur.db.model.Pais;
import com.bteconosur.db.model.Player;

public class PlayerRegistry extends Registry<UUID, Player> {

    private static PlayerRegistry instance;

    public PlayerRegistry() {
        super();
        ConsoleLogger.info(LanguageHandler.getText("player-registry-initializing"));
        loadedObjects = new ConcurrentHashMap<>();
        List<Player> players = dbManager.selectAll(Player.class);
        if (players != null) for (Player p : players) loadedObjects.put(p.getUuid(), p);
    }

    @Override
    public void load(Player obj) {
        if (obj == null || obj.getUuid() == null) return;
        dbManager.save(obj);
        loadedObjects.put(obj.getUuid(), obj);
    }

    public Player get(CommandSender sender) {
        if (sender == null) return null;
        if (!(sender instanceof org.bukkit.entity.Player)) return null;
        return get(((org.bukkit.entity.Player) sender).getUniqueId());
    }

    public void shutdown() {
        ConsoleLogger.info(LanguageHandler.getText("player-registry-shutting-down"));
        loadedObjects.clear();
        loadedObjects = null;
    }

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

    public List<Player> getReviewers(Pais pais) {
        return loadedObjects.values()
                .stream()
                .filter(player -> player.getPaisesReviewer().contains(pais))
                .collect(Collectors.toList());
    }

    public List<Player> getManagers(Pais pais) {
        return loadedObjects.values()
                .stream()
                .filter(player -> player.getPaisesManager().contains(pais))
                .collect(Collectors.toList());
    }

    public List<Player> getOnlinePlayers() {
        return Bukkit.getOnlinePlayers()
                .stream()
                .map(player -> get(player.getUniqueId()))
                .sorted(Comparator.comparing(Player::getNombrePublico))
                .collect(Collectors.toList());
    }

    public List<Player> getOfflinePlayers() {
        return loadedObjects.values()
                .stream()
                .filter(player -> !isOnline(player.getUuid()))
                .sorted(Comparator.comparing(Player::getFechaUltimaConexion, 
                        Comparator.nullsFirst(Comparator.naturalOrder())).reversed())
                .limit(config.getInt("max-offline-players-list", 100))
                .collect(Collectors.toList());
    }

    public boolean isOnline(UUID uuid) {
        if (uuid == null) return false;
        return Bukkit.getPlayer(uuid) != null;
    }

    public static PlayerRegistry getInstance() {
        if (instance == null) {
            instance = new PlayerRegistry();
        }
        return instance;
    }

}
