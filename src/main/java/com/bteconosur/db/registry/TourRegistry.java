package com.bteconosur.db.registry;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.locationtech.jts.geom.Polygon;

import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.ConsoleLogger;
import com.bteconosur.db.model.Tour;
import com.bteconosur.db.model.TourStop;
import com.bteconosur.world.WorldManager;

public class TourRegistry extends Registry<String, Tour> {

    private static TourRegistry instance;

    public TourRegistry() {
        super();
        ConsoleLogger.info(LanguageHandler.getText("tour-registry-initializing"));
        loadedObjects = new ConcurrentHashMap<>();
        List<Tour> tours = dbManager.selectAll(Tour.class);
        if (tours != null) {
            for (Tour tour : tours) {
                if (tour.getId() != null) {
                    loadedObjects.put(tour.getId(), tour);
                }
            }
        }
    }

    /**
     * Carga un tour en persistencia y memoria.
     *
     * @param obj tour a cargar.
     */
    @Override
    public void load(Tour obj) {
        if (obj == null || obj.getId() == null) return;
        dbManager.save(obj);
        loadedObjects.put(obj.getId(), obj);
    }

    /**
     * Obtiene un tour por id.
     *
     * @param id id del tour.
     * @return tour encontrado, o {@code null}.
     */
    public Tour get(String id) {
        for (Tour tour : loadedObjects.values()) {
            if (tour.getId().equalsIgnoreCase(id)) {
                return tour;
            }
        }
        return null;
    }

    /**
     * Obtiene los ids de las paradas de un tour.
     *
     * @param tourId id del tour.
     * @return lista de ids de paradas, o lista vacía si no se encontró el tour.
     */
    public List<String> getTourStopIds(String tourId) {
        Tour tour = get(tourId);
        if (tour == null) return List.of();
        return tour.getParadas().stream()
            .map(TourStop::getId)
            .toList();
    }

    /**
     * Obtiene todas las paradas de todos los tours.
     *
     * @return lista de todas las paradas.
     */
    public List<TourStop> getAllTourStops() {
        return loadedObjects.values().stream()
            .flatMap(tour -> tour.getParadas().stream())
            .toList();
    }

    /**
     * Crea una nueva parada para un tour.
     *
     * @param tourId id del tour.
     * @param paradaId id de la parada.
     * @param orden orden de la parada.
     * @param location ubicación de la parada.
     * @param poligono polígono de la parada.
     * @return la nueva parada creada, o {@code null} si no se pudo crear.
     */
    public TourStop createTourParada(String tourId, String paradaId, int orden, Location location, Polygon poligono) {
        Tour tour = get(tourId);
        if (tour == null) return null;

        List<TourStop> paradas = tour.getParadas();
        int nuevoOrden = Math.max(1, Math.min(orden, paradas.size() + 1));

        for (TourStop otraParada : paradas) {
            if (otraParada.getOrden() >= nuevoOrden) {
                otraParada.setOrden(otraParada.getOrden() + 1);
            }
        }

        TourStop parada = new TourStop(tour, paradaId, nuevoOrden, location, poligono);
        tour.addParada(parada);
        paradas.sort(Comparator.comparingInt(TourStop::getOrden));
        merge(tour.getId());
        WorldManager.getInstance().createRegion(parada);
        return parada;
    }

    /**
     * Edita el orden de una parada de un tour.
     *
     * @param tourId id del tour.
     * @param paradaId id de la parada.
     * @param orden nuevo orden de la parada.
     */
    public TourStop editTourParada(String tourId, String paradaId, int orden) {
        Tour tour = get(tourId);
        if (tour == null) return null;

        List<TourStop> paradas = tour.getParadas();
        TourStop parada = tour.getParada(paradaId);
        if (parada == null) return null;

        int ordenActual = parada.getOrden();
        int nuevoOrden = Math.max(1, Math.min(orden, paradas.size()));
        if (ordenActual == nuevoOrden) return parada;

        if (nuevoOrden > ordenActual) {
            for (TourStop otraParada : paradas) {
                if (otraParada == parada) continue;
                int ordenOtra = otraParada.getOrden();
                if (ordenOtra > ordenActual && ordenOtra <= nuevoOrden) {
                    otraParada.setOrden(ordenOtra - 1);
                }
            }
        } else {
            for (TourStop otraParada : paradas) {
                if (otraParada == parada) continue;
                int ordenOtra = otraParada.getOrden();
                if (ordenOtra >= nuevoOrden && ordenOtra < ordenActual) {
                    otraParada.setOrden(ordenOtra + 1);
                }
            }
        }

        parada.setOrden(nuevoOrden);
        paradas.sort(Comparator.comparingInt(TourStop::getOrden));

        merge(tour.getId());
        return parada;
    }

    /**
     * Edita la ubicación de una parada de un tour.
     *
     * @param tourId id del tour.
     * @param paradaId id de la parada.
     * @param location nueva ubicación de la parada.
     */
    public TourStop editTourParada(String tourId, String paradaId, Location location) {
        Tour tour = get(tourId);
        if (tour == null) return null;
        TourStop parada = tour.getParada(paradaId);
        if (parada == null) return null;
        parada.setLocation(location);
        merge(parada.getTour().getId());
        return parada;
    }

    /**
     * Edita el polígono de una parada de un tour.
     *
     * @param tourId id del tour.
     * @param paradaId id de la parada.
     * @param poligono nuevo polígono de la parada.
     */
    public TourStop editTourParada(String tourId, String paradaId, Polygon poligono) {
        Tour tour = get(tourId);
        if (tour == null) return null;
        TourStop parada = tour.getParada(paradaId);
        if (parada == null) return null;
        parada.setPoligono(poligono);
        merge(parada.getTour().getId());
        WorldManager.getInstance().updateRegion(parada);
        return parada;
    }

    /**
     * Elimina una parada de un tour.
     *
     * @param tourId id del tour.
     * @param paradaId id de la parada.
     */
    public void removeTourParada(String tourId, String paradaId) {
        Tour tour = get(tourId);
        if (tour == null) return;

        List<TourStop> paradas = tour.getParadas();
        TourStop parada = tour.getParada(paradaId);
        if (parada == null) return;

        int ordenEliminado = parada.getOrden();
        tour.removeParada(parada);

        for (TourStop otraParada : paradas) {
            if (otraParada.getOrden() > ordenEliminado) {
                otraParada.setOrden(otraParada.getOrden() - 1);
            }
        }

        paradas.sort(Comparator.comparingInt(TourStop::getOrden));
        merge(tour.getId());
        WorldManager.getInstance().removeRegion(parada);
    }

    /**
     * Cierra el registro y limpia su cache en memoria.
     */
    public void shutdown() {
        ConsoleLogger.info(LanguageHandler.getText("tour-registry-shutting-down"));
        loadedObjects.clear();
        loadedObjects = null;
    }

    public static TourRegistry getInstance() {
        if (instance == null) {
            instance = new TourRegistry();
        }
        return instance;
    }
}
