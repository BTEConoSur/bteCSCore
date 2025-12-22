package com.bteconosur.db.registry;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.bteconosur.db.model.Player;

public class PlayerRegistry extends Registry<UUID, Player> {

    private static PlayerRegistry instance;

    public PlayerRegistry() {
        super(Player.class);
        logger.info(lang.getString("player-registry-initializing"));
        loadedObjects = new ConcurrentHashMap<>();
        List<Player> players = dbManager.selectAll(Player.class);
        if (players != null) for (Player p : players) loadedObjects.put(p.getUuid(), p);
    }

    @Override
    public void load(Player obj) {
        if (obj == null || obj.getUuid() == null) return;
        loadedObjects.put(obj.getUuid(), obj);
        dbManager.save(obj);
    }

    public void shutdown() {
        logger.info(lang.getString("player-registry-shutting-down"));
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

    public static PlayerRegistry getInstance() {
        if (instance == null) {
            instance = new PlayerRegistry();
        }
        return instance;
    }

}
