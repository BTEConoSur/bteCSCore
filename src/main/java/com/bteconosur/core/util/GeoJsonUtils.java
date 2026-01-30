package com.bteconosur.core.util;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.configuration.file.YamlConfiguration;
import org.locationtech.jts.algorithm.Orientation;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.geojson.GeoJsonReader;
import org.locationtech.jts.io.geojson.GeoJsonWriter;

import com.bteconosur.core.BTEConoSur;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.db.model.Division;
import com.bteconosur.db.model.Pais;
import com.bteconosur.db.model.RegionDivision;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class GeoJsonUtils {

    private static final YamlConfiguration lang = ConfigHandler.getInstance().getLang();
    private static final BTEConoSur plugin = BTEConoSur.getInstance();

    public static String polygonToGeoJson(Polygon polygon, String fillColor, String strokeColor) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            GeoJsonWriter writer = new GeoJsonWriter();
            writer.setEncodeCRS(false);
            
            List<Coordinate> exteriorCoords = new ArrayList<>();
            for (Coordinate coord : polygon.getExteriorRing().getCoordinates()) {
                double[] geo = TerraUtils.toGeo(coord.x, coord.y);
                double lon = Math.round(geo[0] * 1000000.0) / 1000000.0;
                double lat = Math.round(geo[1] * 1000000.0) / 1000000.0;
                exteriorCoords.add(new Coordinate(lon, lat));
            }
            if (exteriorCoords.size() >= 3 && !exteriorCoords.get(0).equals2D(exteriorCoords.get(exteriorCoords.size() - 1))) {
                exteriorCoords.add(new Coordinate(exteriorCoords.get(0)));
            }
            LinearRing shell = polygon.getFactory().createLinearRing(exteriorCoords.toArray(new Coordinate[0]));
            LinearRing[] holes = null;
            if (polygon.getNumInteriorRing() > 0) {
                holes = new LinearRing[polygon.getNumInteriorRing()];
                for (int h = 0; h < polygon.getNumInteriorRing(); h++) {
                    List<Coordinate> holeCoords = new ArrayList<>();
                    for (Coordinate coord : polygon.getInteriorRingN(h).getCoordinates()) {
                        double[] geo = TerraUtils.toGeo(coord.x, coord.y);
                        double lon = Math.round(geo[0] * 1000000.0) / 1000000.0;
                        double lat = Math.round(geo[1] * 1000000.0) / 1000000.0;
                        holeCoords.add(new Coordinate(lon, lat));
                    }
                    if (holeCoords.size() >= 3 && !holeCoords.get(0).equals2D(holeCoords.get(holeCoords.size() - 1))) {
                        holeCoords.add(new Coordinate(holeCoords.get(0)));
                    }
                    holes[h] = polygon.getFactory().createLinearRing(holeCoords.toArray(new Coordinate[0]));
                }
            }

            Polygon geoPolygon = polygon.getFactory().createPolygon(shell, holes);
            
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
            e.printStackTrace();
            return "{}";
        }
    }

    public static List<Polygon> geoJsonToPolygons(String pathString, String fileName) {
        GeometryFactory gf = new GeometryFactory();
        try {
            File folder = new File(plugin.getDataFolder(), "geojson/" + pathString);
            if (!folder.exists()) {
                folder.mkdirs();
            }

            File file = new File(folder, fileName);
            String content = Files.readString(file.toPath());
            GeoJsonReader reader = new GeoJsonReader();
            List<Polygon> polygons = new ArrayList<>();
            Gson gson = new Gson();
            JsonObject geoJson = gson.fromJson(content, JsonObject.class);

            JsonArray features = geoJson.getAsJsonArray("features");
            //ConsoleLogger.debug("Cantidad de features en el GeoJSON: " + features.size());
            JsonObject feature = features.get(0).getAsJsonObject();
            String geometryJson = gson.toJson(feature.getAsJsonObject("geometry"));
            
            Geometry geom = reader.read(geometryJson);
            
            List<Polygon> geoPolygons = new ArrayList<>();
            if (geom instanceof MultiPolygon) {
                MultiPolygon multiPoly = (MultiPolygon) geom;
                for (int j = 0; j < multiPoly.getNumGeometries(); j++) {
                    geoPolygons.add((Polygon) multiPoly.getGeometryN(j));
                }
            } else if (geom instanceof Polygon) {
                geoPolygons.add((Polygon) geom);
            }
            
            for (Polygon geoPoly : geoPolygons) {
                List<Coordinate> exteriorCoords = new ArrayList<>();
                for (Coordinate coord : geoPoly.getExteriorRing().getCoordinates()) {
                    double[] worldCoord = TerraUtils.toMc(coord.y, coord.x);
                    exteriorCoords.add(new Coordinate(worldCoord[0], worldCoord[1]));
                }
                if (exteriorCoords.size() >= 3 && !exteriorCoords.get(0).equals2D(exteriorCoords.get(exteriorCoords.size() - 1))) {
                    exteriorCoords.add(new Coordinate(exteriorCoords.get(0)));
                }
                if (exteriorCoords.size() < 4) {
                    ConsoleLogger.warn("GeoJSON inválido: Anillo exterior con menos de 4 puntos tras cerrar el anillo.");
                    continue;
                }

                Coordinate[] exteriorArr = exteriorCoords.toArray(new Coordinate[0]);
                if (!Orientation.isCCW(exteriorArr)) {
                    Collections.reverse(exteriorCoords);
                    exteriorArr = exteriorCoords.toArray(new Coordinate[0]);
                }

                LinearRing[] holes = null;
                if (geoPoly.getNumInteriorRing() > 0) {
                    holes = new LinearRing[geoPoly.getNumInteriorRing()];
                    for (int h = 0; h < geoPoly.getNumInteriorRing(); h++) {
                        List<Coordinate> holeCoords = new ArrayList<>();
                        for (Coordinate coord : geoPoly.getInteriorRingN(h).getCoordinates()) {
                            double[] worldCoord = TerraUtils.toMc(coord.y, coord.x);
                            holeCoords.add(new Coordinate(worldCoord[0], worldCoord[1]));
                        }
                        if (holeCoords.size() >= 3 && !holeCoords.get(0).equals2D(holeCoords.get(holeCoords.size() - 1))) {
                            holeCoords.add(new Coordinate(holeCoords.get(0)));
                        }
                        if (holeCoords.size() < 4) {
                            ConsoleLogger.warn("GeoJSON inválido: Anillo interior con menos de 4 puntos tras cerrar el anillo.");
                            holes[h] = null;
                            continue;
                        }
                        Coordinate[] holeArr = holeCoords.toArray(new Coordinate[0]);
                        if (Orientation.isCCW(holeArr)) {
                            Collections.reverse(holeCoords);
                            holeArr = holeCoords.toArray(new Coordinate[0]);
                        }
                        holes[h] = gf.createLinearRing(holeArr);
                    }
                }

                LinearRing shell = gf.createLinearRing(exteriorArr);
                Polygon poly = gf.createPolygon(shell, holes);
                polygons.add(poly);
            }
            return polygons;
        } catch (Exception e) {
            ConsoleLogger.error("Error al deserializar GeoJSON: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static List<Division> geoJsonToDivisions(Pais pais, String path) {
        try {
            String fullPath = "divisions/" + path;
            File folder = new File(plugin.getDataFolder(), "geojson/" + fullPath);
            if (!folder.exists()) {
                return new ArrayList<>();
            }

            File[] files = folder.listFiles();
            List<Division> divisions = new ArrayList<>();
            if (files == null) return divisions;
            for (File file : files) {
                String content = Files.readString(file.toPath());
                
                Gson gson = new Gson();
                JsonObject geoJson = gson.fromJson(content, JsonObject.class);
                JsonArray features = geoJson.getAsJsonArray("features");
                //ConsoleLogger.debug("Cantidad de features en el GeoJSON: " + features.size());
                JsonObject feature = features.get(0).getAsJsonObject();
                JsonObject properties = feature.getAsJsonObject("properties");
                String type = properties.get("gna").getAsString();
                String name = properties.get("nam").getAsString();
                String context = properties.get("contexto").getAsString();                    

                List<Polygon> polygons = geoJsonToPolygons(fullPath, file.getName());
                Set<RegionDivision> regiones = new HashSet<>();
                Division division = new Division(pais, name, type, context);
                for (int g = 0; g < polygons.size(); g++) {
                    String regionName = division.getNombre() + "_" + file.getName() + "_" + (g + 1);
                    regiones.add(new RegionDivision(division, regionName, polygons.get(g)));
                }
                division.setRegiones(new ArrayList<>(regiones));
                divisions.add(division);
            }
            return divisions;
        } catch (Exception e) {
            ConsoleLogger.error("Error al deserializar GeoJSON: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

}