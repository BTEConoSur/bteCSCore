package com.bteconosur.core.util;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PlayerRegistry;

public class TourService {

    private static List<UUID> activeTours = new ArrayList<>();

    public static boolean isTourActive(UUID playerId) {
        return activeTours.contains(playerId);
    }

    public static void startTour(UUID playerId) {
        if (!activeTours.contains(playerId)) {
            activeTours.add(playerId);
        }
        Player player = PlayerRegistry.getInstance().get(playerId);
        PlayerLogger.info(player, "Tour empezado", (String) null);
    }

    public static void endTour(UUID playerId) {
        activeTours.remove(playerId);
        Player player = PlayerRegistry.getInstance().get(playerId);
        PlayerLogger.info(player, "Tour terminado", (String) null);
    }

}
