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

public class SatMapUtils {

    private static final YamlConfiguration config = ConfigHandler.getInstance().getConfig();
    private static final YamlConfiguration secret = ConfigHandler.getInstance().getSecret();
    private static final BTEConoSur plugin = BTEConoSur.getInstance();
    private static final Object requestsLock = new Object();

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
    
    private static boolean checkMonthlyRequests() {
        YamlConfiguration data = ConfigHandler.getInstance().getData();
        int currentRequests = data.getInt("mapbox-requests") + 1;
        if (currentRequests >= config.getInt("mapbox-month-limit")) {
            ConsoleLogger.warn("Límite mensual de requests a MapBox alcanzado. No se podrá descargar la imagen del mapa.");
            return false;
        }
        return true;
    }
    
    private static void incrementMonthlyRequests() {
        synchronized (requestsLock) {
            YamlConfiguration data = ConfigHandler.getInstance().getData();
            int current = data.getInt("mapbox-requests");
            data.set("mapbox-requests", current + 1);
            ConfigHandler.getInstance().save();
        }
    }

    public static File downloadContext(Proyecto proyecto, Set<Proyecto> otrosProyectos) {
        try {
            checkReset();
            
            File folder = new File(plugin.getDataFolder(), "images/projects");
            if (!folder.exists()) {
                folder.mkdirs();
            }
            
            File contextFile = new File(folder, proyecto.getId() + "_context.png");
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
            ConsoleLogger.error("Error al descargar imagen del mapa: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

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
            ConsoleLogger.error("Error al descargar imagen del mapa: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

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
            ConsoleLogger.error("Error al copiar imagen del mapa: " + e.getMessage());
            e.printStackTrace();
        }
    }

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
            ConsoleLogger.error("Error al eliminar imagen de redefinición: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
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
            ConsoleLogger.error("Error al crear enlace del mapa (con otros proyectos): " + e.getMessage());
            e.printStackTrace();
            return "";
        }
    }

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
            ConsoleLogger.error("Error al crear enlace del mapa (con otros proyectos): " + e.getMessage());
            e.printStackTrace();
            return "";
        }
    }

}
