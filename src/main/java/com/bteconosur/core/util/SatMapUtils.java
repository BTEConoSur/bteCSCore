package com.bteconosur.core.util;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.YearMonth;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.configuration.file.YamlConfiguration;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Polygon;

import com.bteconosur.core.BTEConoSur;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.db.model.Proyecto;
import com.sk89q.worldedit.util.net.HttpRequest;

/**
 * Utilidad para descarga de imágenes de mapas satélite desde MapBox.
 * Gestiona descargas de imágenes para proyectos, respeta límites de requests
 * mensuales y proporciona imágenes por defecto cuando no está disponible.
 */
public class SatMapUtils {

    private static final YamlConfiguration config = ConfigHandler.getInstance().getConfig();
    private static final YamlConfiguration secret = ConfigHandler.getInstance().getSecret();
    private static final BTEConoSur plugin = BTEConoSur.getInstance();
    private static final Object requestsLock = new Object();

    /**
     * Reinicia el contador de requests a MapBox si ha cambiado el mes.
     */
    private static void checkReset() {
        YamlConfiguration data = ConfigHandler.getInstance().getData();
        String currentMonth = YearMonth.now().toString();
        String lastMonth = data.getString("mapbox-requests-month", "");
        
        if (!currentMonth.equals(lastMonth)) {
            synchronized (requestsLock) {
                data.set("mapbox-requests", 0);
                data.set("mapbox-requests-month", currentMonth);
                ConfigHandler.getInstance().save();
                ConsoleLogger.info("Contador de requests de MapBox reiniciado para " + currentMonth);
            }
        }
    }
    
    /**
     * Verifica si se ha alcanzado el límite mensual de requests a MapBox.
     *
     * @return {@code true} si hay requests disponibles, {@code false} si se alcanzó el límite.
     */
    private static boolean checkMonthlyRequests() {
        YamlConfiguration data = ConfigHandler.getInstance().getData();
        int currentRequests = data.getInt("mapbox-requests") + 1;
        if (currentRequests >= config.getInt("mapbox-month-limit")) {
            ConsoleLogger.warn("Límite mensual de requests a MapBox alcanzado. No se podrá descargar la imagen del mapa.");
            return false;
        }
        return true;
    }
    
    /**
     * Incrementa el contador de requests mensuales a MapBox.
     */
    private static void incrementMonthlyRequests() {
        synchronized (requestsLock) {
            YamlConfiguration data = ConfigHandler.getInstance().getData();
            int current = data.getInt("mapbox-requests");
            data.set("mapbox-requests", current + 1);
            ConfigHandler.getInstance().save();
        }
    }

    /**
     * Descarga la imagen satélite de un proyecto desde MapBox.
     *
     * @param proyecto proyecto cuya imagen se descargará.
     * @return archivo descargado o {@code null} si no se pudo descargar.
     */
    public static File downloadImage(Proyecto proyecto) {
        File file = downloadContext(proyecto, Set.of());
        if (file != null) {
            File createFolder = new File(BTEConoSur.getInstance().getDataFolder(), "images/create");
            File contextFile = new File(createFolder, proyecto.getId() + ".png");
            if (contextFile.exists()) {
                contextFile.delete();
            }
        }
        return file;
    }

    /**
     * Descarga la imagen con contexto de otros proyectos incluidos.
     *
     * @param proyecto proyecto principal cuya imagen se descargará.
     * @param otrosProyectos conjunto de otros proyectos a mostrar como contexto.
     * @return archivo descargado o {@code null} si no se pudo descargar.
     */
    public static File downloadContext(Proyecto proyecto, Set<Proyecto> otrosProyectos) {
        try {
            checkReset();
            
            File folder = new File(plugin.getDataFolder(), "images/projects");
            if (!folder.exists()) {
                folder.mkdirs();
            }

            File createFolder = new File(plugin.getDataFolder(), "images/create");
            if (!createFolder.exists()) {
                createFolder.mkdirs();
            }
            
            File contextFile = new File(createFolder, proyecto.getId() + ".png");
            File imageFile = new File(folder, proyecto.getId() + ".png");
            File defaultFile = new File(plugin.getDataFolder(), "images/projects/default_map.png");
            //URL mapUrl = URI.create(createMapSatLink(proyecto.getPoligono(), otrosProyectos.stream().map(Proyecto::getPoligono).toList())).toURL();
            @SuppressWarnings("deprecation")
            URL mapUrl = new URL(createMapSatLink(proyecto.getPoligono(), otrosProyectos.stream().map(Proyecto::getPoligono).collect(Collectors.toSet())));
            if (!checkMonthlyRequests()) {
                Files.copy(defaultFile.toPath(), imageFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                Files.copy(defaultFile.toPath(), contextFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                return contextFile;
            };

            try (InputStream is = HttpRequest.get(mapUrl).execute().getInputStream()) {
                Files.copy(is, contextFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                incrementMonthlyRequests();
            }

            if (otrosProyectos == null || otrosProyectos.isEmpty()) {
                Files.copy(contextFile.toPath(), imageFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } else {
                @SuppressWarnings("deprecation")
                URL imageUrl = new URL(createMapSatLink(proyecto.getPoligono(), null));
                if (!checkMonthlyRequests()) return null;
                try (InputStream is = HttpRequest.get(imageUrl).execute().getInputStream()) {
                    Files.copy(is, imageFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    incrementMonthlyRequests();
                }
                ConsoleLogger.debug("Imagen principal descargada: " + imageFile.getAbsolutePath());
            }
            
            ConsoleLogger.debug("Imagen de contexto descargada: " + contextFile.getAbsolutePath());
            return contextFile;
        } catch (Exception e) {
            ConsoleLogger.error("Error al descargar imagen del mapa: ", e);
            return null;
        }
    }

    /**
     * Descarga una imagen de contexto para redefinición de proyecto,
     * superponiendo el polígono anterior, el nuevo y los solapamientos.
     *
     * @param proyectoId identificador del proyecto.
     * @param oldPolygon polígono actual del proyecto.
     * @param newPolygon nuevo polígono propuesto.
     * @param overlapping polígonos superpuestos de otros proyectos.
     * @return archivo de imagen generado, o {@code null} si ocurre un error.
     */
    public static File downloadRedefineContext(String proyectoId, Polygon oldPolygon, Polygon newPolygon, Set<Polygon> overlapping) {
        try {
            File redefineFolder = new File(plugin.getDataFolder(), "images/redefine");
            if (!redefineFolder.exists()) {
                redefineFolder.mkdirs();
            }
            
            File contextFile = new File(redefineFolder, proyectoId + ".png");
            File defaultFile = new File(plugin.getDataFolder(), "images/projects/default_map.png");
            @SuppressWarnings("deprecation")
            URL mapUrl = new URL(createMapSatLink(oldPolygon, newPolygon, overlapping));
            if (!checkMonthlyRequests()) {
                Files.copy(defaultFile.toPath(), contextFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                return contextFile;
            };

            try (InputStream is = HttpRequest.get(mapUrl).execute().getInputStream()) {
                Files.copy(is, contextFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                incrementMonthlyRequests();
            }
            
            ConsoleLogger.debug("Imagen de contexto descargada: " + contextFile.getAbsolutePath());
            return contextFile;
        } catch (Exception e) {
            ConsoleLogger.error("Error al descargar imagen del mapa: ", e);
            return null;
        }
    }

    /**
     * Reemplaza la imagen principal del proyecto por una nueva captura satelital
     * luego de finalizar una redefinición.
     *
     * @param proyecto proyecto cuya imagen principal se actualizará.
     */
    public static void switchRedefineImage(Proyecto proyecto) {
        try {
            File redefineFolder = new File(plugin.getDataFolder(), "images/redefine");
            if (!redefineFolder.exists()) {
                redefineFolder.mkdirs();
            }
            File projectsFolder = new File(plugin.getDataFolder(), "images/projects");
            if (!projectsFolder.exists()) {
                projectsFolder.mkdirs();
            }
            File imageFile = new File(projectsFolder, proyecto.getId() + ".png");
            File redefineContextFile = new File(redefineFolder, proyecto.getId() + ".png");
            File defaultFile = new File(plugin.getDataFolder(), "images/projects/default_map.png");
            Files.delete(redefineContextFile.toPath());
            if (!checkMonthlyRequests()) {
                Files.copy(defaultFile.toPath(), imageFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                return;
            }
            @SuppressWarnings("deprecation")
            URL imageUrl = new URL(createMapSatLink(proyecto.getPoligono(), null));
            if (!checkMonthlyRequests()) return;
            try (InputStream is = HttpRequest.get(imageUrl).execute().getInputStream()) {
                Files.copy(is, imageFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                incrementMonthlyRequests();
            }
            ConsoleLogger.debug("Imagen principal descargada: " + imageFile.getAbsolutePath());  
        } catch (Exception e) {
            ConsoleLogger.error("Error al copiar imagen del mapa: ", e);
        }
    }

    /**
     * Elimina la imagen temporal de redefinición asociada a un proyecto.
     *
     * @param proyecto proyecto cuya imagen temporal se eliminará.
     */
    public static void deleteRedefineImage(Proyecto proyecto) {
        try {
            File redefineFolder = new File(plugin.getDataFolder(), "images/redefine");
            if (!redefineFolder.exists()) {
                redefineFolder.mkdirs();
            }
            File redefineContextFile = new File(redefineFolder, proyecto.getId() + ".png");
            if (redefineContextFile.exists()) {
                Files.delete(redefineContextFile.toPath());
                ConsoleLogger.debug("Imagen de contexto de redefinición eliminada: " + redefineContextFile.getAbsolutePath());
            }
        } catch (Exception e) {
            ConsoleLogger.error("Error al eliminar imagen de redefinición: ", e);
        }
    }
    
    /**
     * Construye el enlace de MapBox para renderizar el polígono del proyecto
     * y, opcionalmente, polígonos de contexto de otros proyectos.
     *
     * @param proyecto polígono principal del proyecto.
     * @param otrosProyectos conjunto opcional de polígonos de otros proyectos.
     * @return URL completa para solicitar la imagen satelital, o cadena vacía si ocurre un error.
     */
    private static String createMapSatLink(Polygon proyecto, Set<Polygon> otrosProyectos) {
        try {
            String link = config.getString("map-link");
            link = link.replace("%token%", secret.getString("mapbox-access-token"));

            double padding = config.getDouble("map.padding");
            
            Envelope env = proyecto.getEnvelopeInternal();
            double[] minLatLon = TerraUtils.toGeo(env.getMinX(), env.getMinY());
            double[] maxLatLon = TerraUtils.toGeo(env.getMaxX(), env.getMaxY());
            double lonMin = minLatLon[0];
            double lonMax = maxLatLon[0];
            double latMin = minLatLon[1];
            double latMax = maxLatLon[1];
            
            if (lonMin > lonMax) {
                double temp = lonMin;
                lonMin = lonMax;
                lonMax = temp;
            }
            if (latMin > latMax) {
                double temp = latMin;
                latMin = latMax;
                latMax = temp;
            }
            
            double lonPadding = (lonMax - lonMin) * padding;
            double latPadding = (latMax - latMin) * padding;

            link = link.replace("%minLon%", String.valueOf(lonMin - lonPadding));
            link = link.replace("%minLat%", String.valueOf(latMin - latPadding));
            link = link.replace("%maxLon%", String.valueOf(lonMax + lonPadding));
            link = link.replace("%maxLat%", String.valueOf(latMax + latPadding));
            
            String borderColor = config.getString("map.project.border-color").replace("#", "%23");
            String fillColor = config.getString("map.project.fill-color").replace("#", "%23");
            String proyectoGeoJson = GeoJsonUtils.polygonToGeoJson(proyecto, fillColor, borderColor);
            String fullGeoJsonOverlay = "geojson(" + proyectoGeoJson + ")";
            borderColor = config.getString("map.others-projects.border-color").replace("#", "%23");
            fillColor = config.getString("map.others-projects.fill-color").replace("#", "%23");
            if (otrosProyectos != null && !otrosProyectos.isEmpty()) {
                StringBuilder otrosGeoJson = new StringBuilder();
                for (Polygon p : otrosProyectos) {
                    String otroGeoJson = GeoJsonUtils.polygonToGeoJson(p, fillColor, borderColor);
                    //String encodedOtro = encodeForUri(otroGeoJson);
                    otrosGeoJson.append(",geojson(").append(otroGeoJson).append(")");
                }
                fullGeoJsonOverlay += otrosGeoJson.toString();
            }
            return link.replace("%geojson%", fullGeoJsonOverlay);
        } catch (Exception e) {
            ConsoleLogger.error("Error al crear enlace del mapa (con otros proyectos): ", e);
            return "";
        }
    }

    /**
     * Construye el enlace de MapBox para un escenario de redefinición,
     * incluyendo polígono actual, nuevo polígono y superposiciones de contexto.
     *
     * @param proyecto polígono actual del proyecto.
     * @param newPolygon nuevo polígono propuesto.
     * @param otrosProyectos conjunto opcional de polígonos superpuestos.
     * @return URL completa para solicitar la imagen satelital, o cadena vacía si ocurre un error.
     */
    private static String createMapSatLink(Polygon proyecto, Polygon newPolygon, Set<Polygon> otrosProyectos) {
        try {
            String link = config.getString("map-link");
            link = link.replace("%token%", secret.getString("mapbox-access-token"));

            double padding = config.getDouble("map.padding");
            
            Envelope env = newPolygon.getEnvelopeInternal();
            double[] minLatLon = TerraUtils.toGeo(env.getMinX(), env.getMinY());
            double[] maxLatLon = TerraUtils.toGeo(env.getMaxX(), env.getMaxY());
            double lonMin = minLatLon[0];
            double lonMax = maxLatLon[0];
            double latMin = minLatLon[1];
            double latMax = maxLatLon[1];
            
            if (lonMin > lonMax) {
                double temp = lonMin;
                lonMin = lonMax;
                lonMax = temp;
            }
            if (latMin > latMax) {
                double temp = latMin;
                latMin = latMax;
                latMax = temp;
            }
            
            double lonPadding = (lonMax - lonMin) * padding;
            double latPadding = (latMax - latMin) * padding;

            link = link.replace("%minLon%", String.valueOf(lonMin - lonPadding));
            link = link.replace("%minLat%", String.valueOf(latMin - latPadding));
            link = link.replace("%maxLon%", String.valueOf(lonMax + lonPadding));
            link = link.replace("%maxLat%", String.valueOf(latMax + latPadding));
            
            String fullGeoJsonOverlay = "geojson(" + GeoJsonUtils.polygonToGeoJson(proyecto, 
                config.getString("map.project.border-color").replace("#", "%23"), 
                config.getString("map.project.fill-color").replace("#", "%23")) + ")"; 
            fullGeoJsonOverlay += ",geojson(" + GeoJsonUtils.polygonToGeoJson(newPolygon, 
                config.getString("map.redefine.border-color").replace("#", "%23"), 
                config.getString("map.redefine.fill-color").replace("#", "%23")) + ")"; 
            //String encodedProyecto = encodeForUri(proyectoGeoJson);
            String borderColor = config.getString("map.others-projects.border-color").replace("#", "%23");
            String fillColor = config.getString("map.others-projects.fill-color").replace("#", "%23");
            if (otrosProyectos != null && !otrosProyectos.isEmpty()) {
                StringBuilder otrosGeoJson = new StringBuilder();
                for (Polygon p : otrosProyectos) {
                    String otroGeoJson = GeoJsonUtils.polygonToGeoJson(p, fillColor, borderColor);
                    //String encodedOtro = encodeForUri(otroGeoJson);
                    otrosGeoJson.append(",geojson(").append(otroGeoJson).append(")");
                }
                fullGeoJsonOverlay += otrosGeoJson.toString();
            }
            return link.replace("%geojson%", fullGeoJsonOverlay);
        } catch (Exception e) {
            ConsoleLogger.error("Error al crear enlace del mapa (con otros proyectos): ", e );
            return "";
        }
    }

}
