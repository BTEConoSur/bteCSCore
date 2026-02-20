package com.bteconosur.core.command.btecs.test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.locationtech.jts.geom.Polygon;

import com.bteconosur.core.BTEConoSur;
import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.GeoJsonUtils;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.DBManager;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.RegionDivision;

public class TestRegionDivisionGeojsonCommand extends BaseCommand {

    private final DBManager dbManager;
    private final BTEConoSur plugin;
    private final YamlConfiguration config;

    public TestRegionDivisionGeojsonCommand() {
        super("regiondivisiongeojson", "Crear GeoJSON de una región de división por ID. Se guarda en geojson/output", "<id_region>", CommandMode.BOTH);
        config = ConfigHandler.getInstance().getConfig();
        dbManager = DBManager.getInstance();
        plugin = BTEConoSur.getInstance();
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player player = Player.getBTECSPlayer((org.bukkit.entity.Player) sender);
        Language language = player.getLanguage();
        if (args.length != 1) {
            String message = LanguageHandler.getText(language, "help-command-usage").replace("%comando%", getFullCommand().replace(" " + command, ""));
            PlayerLogger.info(sender, message, (String) null);
            return true;
        }

        Long regionId;
        try {
            regionId = Long.parseLong(args[0]);
        } catch (NumberFormatException ex) {
            String message = LanguageHandler.getText(language, "crud.not-valid-id").replace("%entity%", "RegionDivision").replace("%id%", args[0]);
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        if (!dbManager.exists(RegionDivision.class, regionId)) {
            String message = LanguageHandler.getText(language, "crud.read-not-found").replace("%entity%", "RegionDivision").replace("%id%", args[0]);
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        RegionDivision region = dbManager.get(RegionDivision.class, regionId);
        Polygon polygon = region.getPoligono();
        if (polygon == null) {
            PlayerLogger.error(sender, LanguageHandler.getText(language, "crud.not-valid-region"), (String) null);
            return true;
        }

        String borderColor = config.getString("map.project.border-color");
        String fillColor = config.getString("map.project.fill-color");
        String geoJson = GeoJsonUtils.polygonToGeoJson(polygon, fillColor, borderColor);

        try {
            File folder = new File(plugin.getDataFolder(), "geojson/output");
            if (!folder.exists()) {
                folder.mkdirs();
            }

            String fileName = "regiondivision_" + regionId + ".geojson";
            Path filePath = new File(folder, fileName).toPath();
            Files.writeString(filePath, geoJson);

            String message = "GeoJSON guardado en: " + filePath.toString();
            PlayerLogger.info(sender, message, (String) null);
        } catch (Exception e) {
            PlayerLogger.error(sender, "Error al guardar GeoJSON: " + e.getMessage(), (String) null);
        }
        return true; 
    }

}
