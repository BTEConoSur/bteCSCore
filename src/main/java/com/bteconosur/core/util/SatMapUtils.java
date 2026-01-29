package com.bteconosur.core.util;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.configuration.file.YamlConfiguration;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.geojson.GeoJsonWriter;

import com.bteconosur.core.BTEConoSur;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.db.model.Proyecto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sk89q.worldedit.util.net.HttpRequest;

public class SatMapUtils {

    private static final YamlConfiguration config = ConfigHandler.getInstance().getConfig();
    private static final YamlConfiguration lang = ConfigHandler.getInstance().getLang();
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

    //TODO: que pasa si no se puede descargar la imagen (limite alcanzado, error de red, etc)
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
            URL mapUrl = new URL(createMapSatLink(proyecto.getPoligono(), otrosProyectos.stream().map(Proyecto::getPoligono).toList()));
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
                Files.copy(contextFile.toPath(), imageFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
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
    
    private static String createMapSatLink(Polygon proyecto, List<Polygon> otrosProyectos) {
        try {
            String link = config.getString("map-link");
            link = link.replace("%token%", config.getString("mapbox-access-token"));

            double padding = lang.getDouble("map.padding");
            
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
            
            String borderColor = lang.getString("map.project.border-color").replace("#", "%23");
            String fillColor = lang.getString("map.project.fill-color").replace("#", "%23");
            String proyectoGeoJson = polygonToGeoJson(proyecto, fillColor, borderColor);
            String fullGeoJsonOverlay = "geojson(" + proyectoGeoJson + ")";
            //String encodedProyecto = encodeForUri(proyectoGeoJson);
            if (otrosProyectos != null && !otrosProyectos.isEmpty()) {
                StringBuilder otrosGeoJson = new StringBuilder();
                for (Polygon p : otrosProyectos) {
                    borderColor = lang.getString("map.others-projects.border-color").replace("#", "%23");
                    fillColor = lang.getString("map.others-projects.fill-color").replace("#", "%23");
                    String otroGeoJson = polygonToGeoJson(p, fillColor, borderColor);
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

    private static String polygonToGeoJson(Polygon polygon, String fillColor, String strokeColor) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            GeoJsonWriter writer = new GeoJsonWriter();
            writer.setEncodeCRS(false);
            
            Coordinate[] coords = polygon.getExteriorRing().getCoordinates();
            List<Coordinate> geoCoords = new ArrayList<>();
            
            for (Coordinate coord : coords) {
                double[] geo = TerraUtils.toGeo(coord.x, coord.y);
                double lon = Math.round(geo[0] * 1000000.0) / 1000000.0;
                double lat = Math.round(geo[1] * 1000000.0) / 1000000.0;
                geoCoords.add(new Coordinate(lon, lat));
            }
            
            Polygon geoPolygon = polygon.getFactory().createPolygon(geoCoords.toArray(new Coordinate[0]));
            
            String geometryJson = writer.write(geoPolygon);
            
            ObjectNode feature = mapper.createObjectNode();
            feature.put("type", "Feature");
            feature.set("geometry", mapper.readTree(geometryJson));
            
            ObjectNode properties = mapper.createObjectNode();
            properties.put("stroke", strokeColor);
            properties.put("stroke-width", lang.getInt("map.border-width"));
            properties.put("stroke-opacity", lang.getDouble("map.border-opacity"));
            properties.put("fill", fillColor);
            properties.put("fill-opacity", lang.getDouble("map.fill-opacity"));
            feature.set("properties", properties);
            
            return mapper.writeValueAsString(feature);
        } catch (Exception e) {
            ConsoleLogger.error("Error al serializar GeoJSON: " + e.getMessage());
            return "{}";
        }
    }

}
