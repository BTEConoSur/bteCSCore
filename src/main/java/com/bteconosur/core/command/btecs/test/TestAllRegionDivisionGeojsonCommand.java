package com.bteconosur.core.command.btecs.test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.locationtech.jts.geom.Polygon;

import com.bteconosur.core.BTEConoSur;
import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.util.GeoJsonUtils;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.DBManager;
import com.bteconosur.db.model.RegionDivision;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class TestAllRegionDivisionGeojsonCommand extends BaseCommand {

    private final YamlConfiguration lang;
    private final DBManager dbManager;
    private final BTEConoSur plugin;

    public TestAllRegionDivisionGeojsonCommand() {
        super("allregiondivisiongeojson", "Crear GeoJSON con todas las regiones de divisiones. Se guarda en geojson/output", null, CommandMode.CONSOLE_ONLY);
        ConfigHandler configHandler = ConfigHandler.getInstance();
        lang = configHandler.getLang();
        dbManager = DBManager.getInstance();
        plugin = BTEConoSur.getInstance();
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        PlayerLogger.info(sender, "Generando GeoJSON de regiones de divisiones en segundo plano...", (String) null);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                List<RegionDivision> regions = dbManager.selectAll(RegionDivision.class);
                
                if (regions == null || regions.isEmpty()) {
                    PlayerLogger.error(sender, "No hay regiones de divisiones en la base de datos.", (String) null);
                    return;
                }

                String borderColor = lang.getString("map.project.border-color");
                String fillColor = lang.getString("map.project.fill-color");
                
                ObjectMapper mapper = new ObjectMapper();
                ObjectNode featureCollection = mapper.createObjectNode();
                featureCollection.put("type", "FeatureCollection");
                ArrayNode features = mapper.createArrayNode();
                
                for (RegionDivision region : regions) {
                    Polygon polygon = region.getPoligono();
                    if (polygon != null) {
                        String geoJson = GeoJsonUtils.polygonToGeoJson(polygon, fillColor, borderColor);
                        ObjectNode feature = mapper.readValue(geoJson, ObjectNode.class);
                        feature.put("id", region.getId());
                        feature.put("nombre", region.getNombre());
                        features.add(feature);
                    }
                }
                
                featureCollection.set("features", features);
                
                File folder = new File(plugin.getDataFolder(), "geojson/output");
                if (!folder.exists()) {
                    folder.mkdirs();
                }

                String fileName = "all_regions_division.geojson";
                Path filePath = new File(folder, fileName).toPath();
                Files.writeString(filePath, mapper.writerWithDefaultPrettyPrinter().writeValueAsString(featureCollection));

                String message = "GeoJSON guardado en: " + filePath.toString() + " (" + regions.size() + " regiones)";
                PlayerLogger.info(sender, message, (String) null);
            } catch (Exception e) {
                PlayerLogger.error(sender, "Error al guardar GeoJSON: " + e.getMessage(), (String) null);
            }
        });

        return true;
    }

}
