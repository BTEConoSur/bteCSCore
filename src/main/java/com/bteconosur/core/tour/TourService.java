package com.bteconosur.core.tour;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;
import org.locationtech.jts.geom.Point;

import com.bteconosur.core.BTEConoSur;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.menu.HotbarMenu;
import com.bteconosur.core.menu.tour.TourHotbarMenu;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.core.util.RegionUtils;
import com.bteconosur.core.util.SoundUtils;
import com.bteconosur.core.util.TagResolverUtils;
import com.bteconosur.db.model.Division;
import com.bteconosur.db.model.Pais;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.Proyecto;
import com.bteconosur.db.model.Tour;
import com.bteconosur.db.model.TourStop;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.db.registry.ProyectoRegistry;
import com.bteconosur.db.registry.TourRegistry;
import com.bteconosur.db.util.Estado;
import com.bteconosur.db.util.PlaceholderUtils;
import com.bteconosur.world.WorldManager;

import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

public class TourService {

    private static TourService instance;
    private final Map<UUID, TourSession> activeTours = new HashMap<>();
    
    private static final YamlConfiguration config = ConfigHandler.getInstance().getConfig();

    public static TourService getInstance() {
        if (instance == null) {
            instance = new TourService();
            if (config.getBoolean("border-particles.tourstop-enable")) {
                instance.enableParticlesSpawning();
            }
        };
        return instance; 
    }

    /**
     * Activa el renderizado periódico de partículas de bordes de paradas de tour.
     */
    private void enableParticlesSpawning() {
        long periodTicks = ConfigHandler.getInstance().getConfig().getLong("border-particles.spawn-period");
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : PlayerRegistry.getInstance().getOnlinePlayers()) {
                    if (!player.getConfiguration().getGeneralPaisBorder()) continue;
                    if (!isInTour(player.getUuid())) continue;
                    TourSession session = activeTours.get(player.getUuid());
                    if (session == null) continue;
                    TourStop stop = TourRegistry.getInstance().getTourStop(session.getTourId(), session.getCurrentIndex());
                    if (stop == null || stop.getPoligono() == null) continue;
                    RegionUtils.spawnBorderParticles(player.getBukkitPlayer(), stop.getPoligono(), config.getString("border-particles.tourstop-particle"), 0.5);
                }
            }
        }.runTaskTimer(BTEConoSur.getInstance(), 0L, periodTicks);
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

        if (isInTour(player.getUuid())) {
            PlayerLogger.error(player, LanguageHandler.getText(player.getLanguage(), "tour.in-tour"), (String) null);
            return;
        }

        Location returnLoc = player.getBukkitPlayer().getLocation();

        TourSession session = new TourSession(tour.getId(), returnLoc);
        activeTours.put(player.getUuid(), session);

        new TourHotbarMenu(player, false, tour.getParadas().size() == 1).open();
        WorldManager wm = WorldManager.getInstance();
        TourStop stop = TourRegistry.getInstance().getTourStop(tour.getId(), 1);
        wm.addPlayer(stop, player.getUuid());
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

        if (isInTour(player.getUuid())) {
            PlayerLogger.error(player, LanguageHandler.getText(player.getLanguage(), "tour.in-tour"), (String) null);
            return;
        }

        Location returnLoc = player.getBukkitPlayer().getLocation();

        TourSession session = new TourSession(tour.getId(), returnLoc);
        session.setCurrentIndex(startIndex);
        activeTours.put(player.getUuid(), session);

        new TourHotbarMenu(player, startIndex != 1, startIndex == tour.getParadas().size()).open();
        WorldManager wm = WorldManager.getInstance();
        TourStop stop = TourRegistry.getInstance().getTourStop(tour.getId(), startIndex);
        wm.addPlayer(stop, player.getUuid());
        teleportToCurrentStop(player.getUuid(), session);
    }

    /**
     * Inicia un tour de proyectos.
     * @param player jugador que inicia el tour.
     * @param division división con proyectos a recorrer.
     */
    public void startProjectTour(Player player, Division division) {
        if (isInTour(player.getUuid())) {
            PlayerLogger.error(player, LanguageHandler.getText(player.getLanguage(), "tour.in-tour"), (String) null);
            return;
        }
        List<Proyecto> proyectos = ProyectoRegistry.getInstance().getByDivision(division).stream()
                .filter(p -> p.getEstado() == Estado.COMPLETADO)
                .collect(Collectors.toList());
        if (proyectos.isEmpty()) {
            PlayerLogger.warn(player, LanguageHandler.replaceMC("tour.no-division", player.getLanguage(), division), (String) null);
            return;
        }
        startProjectTour(player, proyectos);
    }

    /**
     * Inicia un tour de proyectos.
     * @param player jugador que inicia el tour.
     * @param pais país con proyectos a recorrer.
     */
    public void startProjectTour(Player player, Pais pais) {
        if (isInTour(player.getUuid())) {
            PlayerLogger.error(player, LanguageHandler.getText(player.getLanguage(), "tour.in-tour"), (String) null);
            return;
        }

        List<Proyecto> proyectos;
        ProyectoRegistry pr = ProyectoRegistry.getInstance();
        if (pais == null) {
            proyectos = new ArrayList<>(pr.getCompleted());
        } else {
            proyectos = ProyectoRegistry.getInstance().getByPais(pais).stream()
                .filter(p -> p.getEstado() == Estado.COMPLETADO)
                .collect(Collectors.toList());
        }
        if (proyectos.isEmpty()) {
            String message = LanguageHandler.replaceMC("tour.no-país", player.getLanguage(), pais);
            if (pais == null) {
                message = message.replace("%pais.nombrePublico%", LanguageHandler.getText(player.getLanguage(), "placeholder.tour.international"));
            }
            PlayerLogger.warn(player, message, (String) null);
            return;
        }
        startProjectTour(player, proyectos);
    }

    /**
     * Inicia un tour de proyectos.
     * @param player jugador que inicia el tour.
     * @param proyectos lista de proyectos a recorrer.
     */
    private void startProjectTour(Player player, List<Proyecto> proyectos) {
        Collections.shuffle(proyectos);
        List<String> projectIds = proyectos.stream().map(Proyecto::getId).collect(Collectors.toList());

        Location returnLoc = player.getBukkitPlayer().getLocation();
        TourSession session = new TourSession(projectIds, returnLoc);
        activeTours.put(player.getUuid(), session);

        new TourHotbarMenu(player, false, projectIds.size() == 1).open();
                
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
            if (session.isProjectTour()) { 
                PlayerLogger.info(player, LanguageHandler.getText(player.getLanguage(), "tour.end-project"), (String) null);   
            } else {
                TourStop stop = TourRegistry.getInstance().getTourStop(session.getTourId(), session.getCurrentIndex());
                if (stop != null) WorldManager.getInstance().removePlayer(stop, playerUuid);
                Tour tour = TourRegistry.getInstance().get(session.getTourId());
                if (tour != null) {
                    PlayerLogger.info(player, LanguageHandler.replaceMC("tour.end", player.getLanguage(), tour), (String) null);
                }
            }
            bukkitPlayer.teleportAsync(session.getReturnLocation());
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
        
        if (player == null) return;

        if (session.getCurrentIndex() < getTotalStops(session)) {
            session.setCurrentIndex(session.getCurrentIndex() + 1);
            updateRegion(session.getCurrentIndex() - 1, session.getCurrentIndex(), playerUuid, session);
            teleportToCurrentStop(playerUuid, session);
        } else {
            stopTour(playerUuid);
        }
    }

    /**
     * Avanza a la primera parada.
     * @param playerUuid UUID del jugador que avanza.
     */
    public void goFirst(UUID playerUuid) {
        TourSession session = activeTours.get(playerUuid);
        if (session == null) return;

        Player player = PlayerRegistry.getInstance().get(playerUuid);
        if (player == null) return;

        if (session.getCurrentIndex() > 1) {
            updateRegion(session.getCurrentIndex(), 1, playerUuid, session);
            session.setCurrentIndex(1);
            teleportToCurrentStop(playerUuid, session);
        } else {
            PlayerLogger.warn(player, LanguageHandler.getText(player.getLanguage(), "tour.first"), (String) null);
        }
    }

     private void updateRegion(int prevIndex, int newIndex, UUID playerUuid, TourSession session) {
        WorldManager wm = WorldManager.getInstance();
        if (!session.isProjectTour()) {
            TourRegistry tr = TourRegistry.getInstance();
            TourStop prevStop = tr.getTourStop(session.getTourId(), prevIndex);
            TourStop newStop = tr.getTourStop(session.getTourId(), newIndex);
            
            if (prevStop != null) wm.removePlayer(prevStop, playerUuid);
            if (newStop != null) wm.addPlayer(newStop, playerUuid);
        }
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
            updateRegion(session.getCurrentIndex() + 1, session.getCurrentIndex(), playerUuid, session);
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
        if (player == null) return;

        org.bukkit.entity.Player bukkitPlayer = player.getBukkitPlayer();
        if (bukkitPlayer == null || !bukkitPlayer.isOnline()) return;

        Location loc;
        
        if (session.isProjectTour()) {
            Proyecto proyecto = ProyectoRegistry.getInstance().get(session.getCurrentProjectId());
            if (proyecto == null) {
                nextStop(playerUuid); 
                return;
            }
            Point centroid = proyecto.getPoligono().getCentroid();
            double x = Math.floor(centroid.getX());
            double z = Math.floor(centroid.getY());
            
            org.bukkit.World world = WorldManager.getInstance().getBTEWorld().getLabelWorld(x, z).getBukkitWorld();
            int highestY = world.getHighestBlockYAt((int) x, (int) z);
            
            loc = new Location(world, x + 0.5, highestY + 1, z + 0.5, bukkitPlayer.getLocation().getYaw(), bukkitPlayer.getLocation().getPitch());
        } else {
            TourStop stop = TourRegistry.getInstance().getTourStop(session.getTourId(), session.getCurrentIndex());
            if (stop == null) return;
            loc = stop.getLocation();
        }
        bukkitPlayer.teleportAsync(loc).thenAccept(success -> {
            if (success) {
                SoundUtils.playSound(bukkitPlayer, "tour-teleport");
                TourHotbarMenu.updateBackButton(player.getLanguage(), playerUuid, session.getCurrentIndex() != 1);
                TourHotbarMenu.updateLastButton(player.getLanguage(), playerUuid, session.getCurrentIndex() == getTotalStops(session));
                sendTourInfo(player);
            }
        });
    }

     /**
     * Obtiene el número total de paradas en el tour.
     * @param session sesión del tour.
     * @return número total de paradas.
     */
    public int getTotalStops(TourSession session) {
        if (session.isProjectTour()) return session.getProjectIds().size();
        
        Tour tour = TourRegistry.getInstance().get(session.getTourId());
        return tour != null ? tour.getParadas().size() : 0;
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
        TourStop stop = null;
        Tour tour = null;
        if (!session.isProjectTour()) {
            tour = tr.get(session.getTourId());
            if (tour == null) return;
            stop = tr.getTourStop(tour.getId(), session.getCurrentIndex());
            if (stop == null) return;
        }
        Language language = player.getLanguage();
        String pluginPrefix = LanguageHandler.getText(language, "plugin-prefix");
        String message1Key = session.isProjectTour() ? "tour.stop.message-proyecto-1" : "tour.stop.message-1";
        List<String> message1 = LanguageHandler.getTextList(language, message1Key);
        List<String> message2 = LanguageHandler.getTextList(language, "tour.stop.message-2");

        TagResolver backResolver = TagResolverUtils.getCommandText("backtext", "/tourback", LanguageHandler.getText(language, "tour.stop.backtext"), LanguageHandler.getText(language, "tour.stop.backhover"));
        TagResolver nextResolver = TagResolverUtils.getCommandText("nexttext", "/tournext", LanguageHandler.getText(language, "tour.stop.nexttext"), LanguageHandler.getText(language, "tour.stop.nexthover"));
        TagResolver stopResolver = TagResolverUtils.getCommandText("stoptext", "/tourstop", LanguageHandler.getText(language, "tour.stop.stoptext"), LanguageHandler.getText(language, "tour.stop.stophover"));
        TagResolver firstResolver = TagResolverUtils.getCommandText("firsttext", "/tourfirst", LanguageHandler.getText(language, "tour.stop.firsttext"), LanguageHandler.getText(language, "tour.stop.firsthover"));
        TagResolver lastResolver = TagResolverUtils.getCommandText("lasttext", "/tourstop", LanguageHandler.getText(language, "tour.stop.lasttext"), LanguageHandler.getText(language, "tour.stop.lasthover"));

        ProyectoRegistry pr = ProyectoRegistry.getInstance();
        Proyecto proyecto = session.isProjectTour() ? pr.get(session.getCurrentProjectId()) : null;
        for (String line : message1) {
            String message;
            if (session.isProjectTour()) {
                message = PlaceholderUtils.replaceMC(line, language, proyecto).replace("%plugin-prefix%", pluginPrefix);
            } else {
                message = PlaceholderUtils.replaceMC(line, language, stop).replace("%plugin-prefix%", pluginPrefix);
            }
            PlayerLogger.send(player, message, (String) null);
        }
        
        if (!session.isProjectTour()) {
            List<String> desc = stop.getDescription(language);
            for (String line : desc) {
                PlayerLogger.send(player, line, (String) null);
            }
        }
        for (String line : message2) {
            line = PlaceholderUtils.replaceMC(line, language, stop).replace("%plugin-prefix%", pluginPrefix)
                .replace("%currentStop%", String.valueOf(session.getCurrentIndex()))
                .replace("%totalStop%", String.valueOf(getTotalStops(session)));
            if (session.getCurrentIndex() == 1) line = line.replace("<backtext> ", "").replace("<firsttext>", "");
            if (session.getCurrentIndex() == getTotalStops(session)) line = line.replace("<nexttext>", "");
            else line = line.replace("<lasttext>", "");
               
            PlayerLogger.send(player, line, (String) null, backResolver, nextResolver, stopResolver, firstResolver, lastResolver);
        }

    }

    public TourSession getSession(Player player) {
        return activeTours.get(player.getUuid());
    }
}
