package com.bteconosur.core.tour;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;

import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.menu.HotbarMenu;
import com.bteconosur.core.menu.tour.TourHotbarMenu;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.core.util.SoundUtils;
import com.bteconosur.core.util.TagResolverUtils;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.Tour;
import com.bteconosur.db.model.TourStop;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.db.registry.TourRegistry;
import com.bteconosur.db.util.PlaceholderUtils;
import com.bteconosur.world.WorldManager;

import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

public class TourService {

    private static TourService instance;
    private final Map<UUID, TourSession> activeTours = new HashMap<>();

    public static TourService getInstance() {
        if (instance == null) instance = new TourService();
        return instance; 
    }

    /**
     * Verifica si un jugador está actualmente en un tour.
     * @param player UUID del jugador.
     * @return true si el jugador está en un tour, false de lo contrario.
     */
    public boolean isInTour(UUID player) {
        return activeTours.containsKey(player);
    }

    /**
     * Inicia un tour.
     * @param player jugador que inicia el tour.
     * @param tour tour a iniciar.
     */
    public void startTour(Player player, Tour tour) { 
        if (tour.getParadas().isEmpty()) {
            PlayerLogger.error(player, LanguageHandler.replaceMC("tour.no-stops", player.getLanguage(), tour), (String) null);
            return;
        }

        Location returnLoc = player.getBukkitPlayer().getLocation();

        TourSession session = new TourSession(tour.getId(), returnLoc);
        activeTours.put(player.getUuid(), session);

        new TourHotbarMenu(player).open();

        teleportToCurrentStop(player.getUuid(), session);
    }

    /**
     * Inicia un tour.
     * @param player jugador que inicia el tour.
     * @param tour tour a iniciar.
     * @param startIndex índice de la primera parada.
     */
    public void startTour(Player player, Tour tour, int startIndex) { 
        if (tour.getParadas().isEmpty()) {
            PlayerLogger.error(player, LanguageHandler.replaceMC("tour.no-stops", player.getLanguage(), tour), (String) null);
            return;
        }

        Location returnLoc = player.getBukkitPlayer().getLocation();

        TourSession session = new TourSession(tour.getId(), returnLoc);
        session.setCurrentIndex(startIndex);
        activeTours.put(player.getUuid(), session);

        new TourHotbarMenu(player).open();

        teleportToCurrentStop(player.getUuid(), session);
    }

    /**
     * Termina el tour y lo devuelve a su lugar original.
     * @param playerUuid UUID del jugador que termina el tour.
     */
    public void stopTour(UUID playerUuid) {
        TourSession session = activeTours.remove(playerUuid);
        if (session == null) return;

        Player player = PlayerRegistry.getInstance().get(playerUuid);
        if (player == null) return;

        HotbarMenu.closeActive(playerUuid);
        org.bukkit.entity.Player bukkitPlayer = player.getBukkitPlayer();
        if (bukkitPlayer != null && bukkitPlayer.isOnline()) {
            WorldManager.getInstance().removePlayer(TourRegistry.getInstance().getTourStop(session.getTourId(), session.getCurrentIndex()), playerUuid);
            bukkitPlayer.teleportAsync(session.getReturnLocation());
            Tour tour = TourRegistry.getInstance().get(session.getTourId());
            PlayerLogger.info(player, LanguageHandler.replaceMC("tour.end", player.getLanguage(), tour), (String) null);
        }
    }

    /**
     * Detiene todos los tours activos.
     */
    public void stopAllTours() {
        for (UUID playerUuid : new ArrayList<>(activeTours.keySet())) {
            stopTour(playerUuid);
        }
    }

    /**
     * Avanza a la siguiente parada.
     * @param playerUuid UUID del jugador que avanza.
     */
    public void nextStop(UUID playerUuid) {
        TourSession session = activeTours.get(playerUuid);
        if (session == null) return;

        Player player = PlayerRegistry.getInstance().get(playerUuid);
        
        Tour tour = TourRegistry.getInstance().get(session.getTourId()); 
        if (player == null || tour == null) return;

        if (session.getCurrentIndex() <= tour.getParadas().size()) {
            session.setCurrentIndex(session.getCurrentIndex() + 1);
            updateRegion(session.getCurrentIndex() - 1, session.getCurrentIndex(), playerUuid, tour.getId());            
            teleportToCurrentStop(playerUuid, session);
        } else {
            stopTour(playerUuid);
        }
    }

    private void updateRegion(int previousIndex, int currentIndex, UUID playerUuid, String tourId) {
        TourRegistry tr = TourRegistry.getInstance();
        TourStop currentStop = tr.getTourStop(tourId, currentIndex);
        TourStop previousStop = tr.getTourStop(tourId, previousIndex);
        WorldManager wm = WorldManager.getInstance();
        wm.removePlayer(previousStop, playerUuid);
        wm.addPlayer(currentStop, playerUuid);
    }

    /**
     * Retrocede a la parada anterior.
     * @param playerUuid UUID del jugador.
     */
     public void previousStop(UUID playerUuid) {
        TourSession session = activeTours.get(playerUuid);
        if (session == null) return;

        Player player = PlayerRegistry.getInstance().get(playerUuid);
        if (player == null) return;

        if (session.getCurrentIndex() > 1) {
            session.setCurrentIndex(session.getCurrentIndex() - 1);
            updateRegion(session.getCurrentIndex() + 1, session.getCurrentIndex(), playerUuid, session.getTourId());
            teleportToCurrentStop(playerUuid, session);
        } else {
            PlayerLogger.warn(player, LanguageHandler.getText(player.getLanguage(), "tour.first"), (String) null);
        }
    }

    /**
     * Ejecuta el teletransporte a la parada actual.
     * @param playerUuid UUID del jugador.
     * @param session Sesión del tour.
     */
    private void teleportToCurrentStop(UUID playerUuid, TourSession session) {
        Player player = PlayerRegistry.getInstance().get(playerUuid);
        TourRegistry tr = TourRegistry.getInstance();
        Tour tour = tr.get(session.getTourId()); 

        if (player == null || tour == null) return;

        org.bukkit.entity.Player bukkitPlayer = player.getBukkitPlayer();
        if (bukkitPlayer == null || !bukkitPlayer.isOnline()) return;

        List<TourStop> paradas = tour.getParadas();
        
        if (session.getCurrentIndex() > paradas.size()) {
            session.setCurrentIndex(paradas.size());
        }
        if (session.getCurrentIndex() < 0) return;

        TourStop stop = tr.getTourStop(tour.getId(), session.getCurrentIndex());

        bukkitPlayer.teleportAsync(stop.getLocation()).thenAccept(success -> {
            if (success) {
                SoundUtils.playSound(bukkitPlayer, "tour-teleport");
                sendTourInfo(player);
            }
        });
    }

    /**
     * Envía la información de la parada actual al jugador.
     * @param player jugador que recibe la información.
     * @param stop parada actual.
     * @param session sesión del tour.
     * @param tour tour actual.
     */
    public void sendTourInfo(Player player) {
        TourSession session = activeTours.get(player.getUuid());
        if (session == null) return;
        TourRegistry tr = TourRegistry.getInstance();
        Tour tour = tr.get(session.getTourId());
        if (tour == null) return;
        TourStop stop = tr.getTourStop(tour.getId(), session.getCurrentIndex());
        if (stop == null) return;
        Language language = player.getLanguage();
        String pluginPrefix = LanguageHandler.getText(language, "plugin-prefix");
        List<String> message1 = LanguageHandler.getTextList(language, "tour.stop.message-1");
        List<String> desc = stop.getDescription(language);
        List<String> message2 = LanguageHandler.getTextList(language, "tour.stop.message-2");
        List<String> lore = new ArrayList<>();

        TagResolver backResolver = TagResolverUtils.getCommandText("backtext", "/tourback", LanguageHandler.getText(language, "tour.stop.backtext"), LanguageHandler.getText(language, "tour.stop.backhover"));
        TagResolver nextResolver = TagResolverUtils.getCommandText("nexttext", "/tournext", LanguageHandler.getText(language, "tour.stop.nexttext"), LanguageHandler.getText(language, "tour.stop.nexthover"));
        TagResolver stopResolver = TagResolverUtils.getCommandText("stoptext", "/tourstop", LanguageHandler.getText(language, "tour.stop.stoptext"), LanguageHandler.getText(language, "tour.stop.stophover"));
        for (String line : message1) {
            lore.add(PlaceholderUtils.replaceMC(line, language, stop).replace("%plugin-prefix%", pluginPrefix));
        }
        for (String line : desc) {
            lore.add(line);
        }
        for (String line : message2) {
            line = PlaceholderUtils.replaceMC(line, language, stop).replace("%plugin-prefix%", pluginPrefix)
                .replace("%currentStop%", String.valueOf(session.getCurrentIndex()))
                .replace("%totalStop%", String.valueOf(tour.getParadas().size()));
            if (session.getCurrentIndex() == 1) line = line.replace("<backtext>", "");
        }

        String message = String.join("\n", lore);
        PlayerLogger.send(player, message, (String) null, backResolver, nextResolver, stopResolver);
    }

    public TourSession getSession(Player player) {
        return activeTours.get(player.getUuid());
    }
}
