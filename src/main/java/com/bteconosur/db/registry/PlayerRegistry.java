package com.bteconosur.db.registry;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import com.bteconosur.db.model.Player;

public class PlayerRegistry extends Registry<UUID, Player> {

    private static PlayerRegistry instance;

    public PlayerRegistry() {
        super(Player.class);
        logger.info(lang.getString("player-registry-initializing"));
        loadedObjects = new HashMap<>();
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

    public static PlayerRegistry getInstance() {
        if (instance == null) {
            instance = new PlayerRegistry();
        }
        return instance;
    }

}
