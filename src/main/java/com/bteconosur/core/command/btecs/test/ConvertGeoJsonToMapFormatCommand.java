package com.bteconosur.core.command.btecs.test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import com.bteconosur.core.BTEConoSur;
import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.ConsoleLogger;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.core.util.TerraUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ConvertGeoJsonToMapFormatCommand extends BaseCommand {

    private final BTEConoSur plugin;
    private final ObjectMapper mapper;

    public ConvertGeoJsonToMapFormatCommand() {
        super("converttomapformat", "<archivo> [priority] [zIndex] [strokeColor] [fillEnabled] [fillColor]", "btecs.command.btecs.test", CommandMode.CONSOLE_ONLY);
        this.plugin = BTEConoSur.getInstance();
        this.mapper = new ObjectMapper();
        this.addSubcommand(new GenericHelpCommand(this));
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        if (args.length < 1) {
            String message = LanguageHandler.getText(Language.getDefault(), "help-command-usage").replace("%comando%", getFullCommand());
            PlayerLogger.info(sender, message, (String) null);
            return true;
        }

        String fileNameInput = args[0];
        final String fileName = fileNameInput.toLowerCase().endsWith(".geojson") ? fileNameInput : fileNameInput + ".geojson";

        int priority = 10;
        int zIndex = 500;
        int strokeColor = -9650972;
        boolean fillEnabled = true;
        int fillColor = -2140367644;

        try {
            if (args.length > 1) priority = Integer.parseInt(args[1]);
            if (args.length > 2) zIndex = Integer.parseInt(args[2]);
            if (args.length > 3) strokeColor = Integer.parseInt(args[3]);
            if (args.length > 4) fillEnabled = Boolean.parseBoolean(args[4]);
            if (args.length > 5) fillColor = Integer.parseInt(args[5]);
        } catch (NumberFormatException e) {
            PlayerLogger.error(sender, "Error al parsear parámetros numéricos: " + e.getMessage(), (String) null);
            return true;
        }

        final int finalPriority = priority;
        final int finalZIndex = zIndex;
        final int finalStrokeColor = strokeColor;
        final boolean finalFillEnabled = fillEnabled;
        final int finalFillColor = fillColor;

        PlayerLogger.info(sender, "Convirtiendo GeoJSON a formato de mapa en segundo plano...", (String) null);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                File inputFolder = new File(plugin.getDataFolder(), "geojson/input");
                File outputFolder = new File(plugin.getDataFolder(), "geojson/output");

                if (!inputFolder.exists()) {
                    inputFolder.mkdirs();
                    return;
                }

                if (!outputFolder.exists()) {
                    outputFolder.mkdirs();
                }

                File geojsonFile = new File(inputFolder, fileName);
                if (!geojsonFile.exists()) {
                    PlayerLogger.error(sender, "Archivo no encontrado: " + fileName, (String) null);
                    return;
                }

                try {
                    convertGeoJsonFile(geojsonFile, outputFolder, sender, finalPriority, finalZIndex, finalStrokeColor, finalFillEnabled, finalFillColor);
                    PlayerLogger.info(sender, "Conversión completada.", (String) null);
                } catch (Exception e) {
                    ConsoleLogger.error("Error al procesar archivo: " + fileName, e);
                    PlayerLogger.error(sender, "Error procesando " + fileName + ": " + e.getMessage(), (String) null);
                }
            } catch (Exception e) {
                ConsoleLogger.error("Error al convertir GeoJSON a formato de mapa", e);
                PlayerLogger.error(sender, "Error: " + e.getMessage(), (String) null);
            }
        });

        return true;
    }

    private void convertGeoJsonFile(File geojsonFile, File outputFolder, CommandSender sender, int priority, int zIndex, int strokeColor, boolean fillEnabled, int fillColor) throws Exception {
        String content = Files.readString(geojsonFile.toPath());
        JsonNode root = mapper.readTree(content);

        if (!root.has("features")) {
            throw new Exception("GeoJSON no tiene 'features'");
        }

        JsonNode features = root.get("features");
        int processedProvinces = 0;

        for (JsonNode feature : features) {
            JsonNode properties = feature.get("properties");
            String provinceName = extractProvinceName(properties);
            
            
            String key = provinceName.toLowerCase().replaceAll("[^a-z0-9]", "_");
            String label = provinceName;

            JsonNode geometry = feature.get("geometry");
            if (geometry == null || !geometry.has("coordinates")) {
                continue;
            }

            ObjectNode mapJson = mapper.createObjectNode();
            mapJson.put("key", key);
            mapJson.put("label", label);
            mapJson.put("updateInterval", 30);
            mapJson.put("showControls", true);
            mapJson.put("defaultHidden", false);
            mapJson.put("priority", priority);
            mapJson.put("zIndex", zIndex);

            ArrayNode markers = mapper.createArrayNode();
            int polygonCount = 0;

            String geometryType = geometry.get("type").asText();
            
            if ("MultiPolygon".equals(geometryType)) {
                JsonNode coordinates = geometry.get("coordinates");
                
                for (int polyIndex = 0; polyIndex < coordinates.size(); polyIndex++) {
                    JsonNode polygon = coordinates.get(polyIndex);
                    
                    if (polygon.size() > 0) {
                        JsonNode exteriorRing = polygon.get(0);
                        ObjectNode markerJson = createLineMarker(key, provinceName, polyIndex + 1, exteriorRing, strokeColor, fillEnabled, fillColor);
                        if (markerJson != null) {
                            markers.add(markerJson);
                            polygonCount++;
                        }
                    }
                }
            } else if ("Polygon".equals(geometryType)) {
                JsonNode coordinates = geometry.get("coordinates");
                
                if (coordinates.size() > 0) {
                    JsonNode exteriorRing = coordinates.get(0);
                    ObjectNode markerJson = createLineMarker(key, provinceName, 1, exteriorRing, strokeColor, fillEnabled, fillColor);
                    if (markerJson != null) {
                        markers.add(markerJson);
                        polygonCount++;
                    }
                }
            }

            mapJson.set("markers", markers);

            String outputFileName = key + ".json";
            Path outputPath = new File(outputFolder, outputFileName).toPath();
            Files.writeString(outputPath, mapper.writerWithDefaultPrettyPrinter().writeValueAsString(mapJson));

            String message = "Provincia: " + provinceName + " → " + outputFileName + " (" + polygonCount + " polígonos)";
            PlayerLogger.info(sender, message, (String) null);
            
            processedProvinces++;
        }

        String summary = "Archivo " + geojsonFile.getName() + " completado: " + processedProvinces + " provincias procesadas.";
        PlayerLogger.info(sender, summary, (String) null);
    }

    private ObjectNode createLineMarker(String key, String provinceName, int polygonIndex, JsonNode ring, int strokeColor, boolean fillEnabled, int fillColor) {
        List<ObjectNode> points = new ArrayList<>();

        for (JsonNode coord : ring) {
            double lon = coord.get(0).asDouble();
            double lat = coord.get(1).asDouble();

            double[] mcCoords = TerraUtils.toMc(lat, lon);
            
            if (mcCoords != null) {
                ObjectNode point = mapper.createObjectNode();
                point.put("x", (long) mcCoords[0]);
                point.put("z", (long) mcCoords[1]);
                points.add(point);
            }
        }

        if (points.isEmpty()) {
            return null;
        }

        ObjectNode marker = mapper.createObjectNode();
        marker.put("type", "poly");

        ObjectNode data = mapper.createObjectNode();
        data.put("key", key + "_polygon_" + polygonIndex);
        
        // Crear polylines con los puntos
        ArrayNode polylines = mapper.createArrayNode();
        ObjectNode polyline = mapper.createObjectNode();
        polyline.put("key", key + "_polygon_" + polygonIndex + "_outline");
        
        ArrayNode pointsArray = mapper.createArrayNode();
        for (ObjectNode point : points) {
            pointsArray.add(point);
        }
        polyline.set("points", pointsArray);
        polylines.add(polyline);
        
        data.set("polylines", polylines);
        marker.set("data", data);

        // Opciones
        ObjectNode options = mapper.createObjectNode();
        
        ObjectNode stroke = mapper.createObjectNode();
        stroke.put("enabled", true);
        stroke.put("weight", 3);
        stroke.put("color", strokeColor);
        
        ObjectNode fill = mapper.createObjectNode();
        fill.put("enabled", fillEnabled);
        fill.put("color", fillColor);
        
        ObjectNode tooltip = mapper.createObjectNode();
        tooltip.put("content", toTitleCase(provinceName) + " - " + polygonIndex);
        tooltip.put("sticky", true);

        options.set("stroke", stroke);
        options.set("fill", fill);
        options.set("tooltip", tooltip);

        marker.set("options", options);

        return marker;
    }

    /**
     * Convierte un string a Title Case (Primera letra de cada palabra en mayúscula)
     */
    private String toTitleCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        
        // Reemplazar guiones bajos y guiones con espacios
        String text = input.replaceAll("[_-]", " ");
        
        StringBuilder titleCase = new StringBuilder();
        boolean nextTitleCase = true;
        
        for (char c : text.toCharArray()) {
            if (Character.isWhitespace(c)) {
                nextTitleCase = true;
                titleCase.append(c);
            } else if (nextTitleCase) {
                titleCase.append(Character.toTitleCase(c));
                nextTitleCase = false;
            } else {
                titleCase.append(Character.toLowerCase(c));
            }
        }
        
        return titleCase.toString();
    }

    /**
     * Extrae el nombre de la provincia desde las propiedades del feature.
     * Intenta múltiples campos comunes en diferentes países.
     */
    private String extractProvinceName(JsonNode properties) {
        if (properties == null) {
            return "Unknown";
        }

        if (properties.has("fna")) {
            String value = properties.get("fna").asText().trim();
            if (!value.isEmpty()) {
                return value;
            }
        }

        if (properties.has("DPTO_DESC")) {
            String value = properties.get("DPTO_DESC").asText().trim();
            if (!value.isEmpty()) {
                return value;
            }
        }

        if (properties.has("id")) {
            return "bo_" + properties.get("id").asText();
        }

        if (properties.has("name")) {
            String value = properties.get("name").asText().trim();
            if (!value.isEmpty()) {
                return value;
            }
        }

        if (properties.has("nombre")) {
            String value = properties.get("nombre").asText().trim();
            if (!value.isEmpty()) {
                return value;
            }
        }

        return "Unknown";
    }
}

