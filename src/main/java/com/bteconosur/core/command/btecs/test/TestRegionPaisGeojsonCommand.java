package com.bteconosur.core.command.btecs.test;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.locationtech.jts.geom.Polygon;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import com.bteconosur.core.BTEConoSur;
import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.ConsoleLogger;
import com.bteconosur.core.util.GeoJsonUtils;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.DBManager;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.RegionPais;

public class TestRegionPaisGeojsonCommand extends BaseCommand {

    private final YamlConfiguration config;
    private final DBManager dbManager;
    private final BTEConoSur plugin;

    public TestRegionPaisGeojsonCommand() {
        super("regionpaisgeojson", "<id_region>", "btecs.command.test", CommandMode.BOTH);
        ConfigHandler configHandler = ConfigHandler.getInstance();
        config = configHandler.getConfig();
        dbManager = DBManager.getInstance();
        plugin = BTEConoSur.getInstance();
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player commandPlayer = null;
        if (sender instanceof org.bukkit.entity.Player) commandPlayer = Player.getBTECSPlayer((org.bukkit.entity.Player) sender);
        Language language = commandPlayer != null ? commandPlayer.getLanguage() : Language.getDefault();
        if (args.length != 1) {
            String message = LanguageHandler.getText(language, "help-command-usage").replace("%comando%", getFullCommand().replace(" " + command, ""));
            PlayerLogger.info(sender, message, (String) null);
            return true;
        }

        Long regionId;
        try {
            regionId = Long.parseLong(args[0]);
        } catch (NumberFormatException ex) {
            String message = LanguageHandler.getText(language, "crud.not-valid-id").replace("%entity%", "RegionPais").replace("%id%", args[0]);
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        if (!dbManager.exists(RegionPais.class, regionId)) {
            String message = LanguageHandler.getText(language, "crud.read-not-found").replace("%entity%", "RegionPais").replace("%id%", args[0]);
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        RegionPais region = dbManager.get(RegionPais.class, regionId);
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

            String fileName = "region_" + regionId + ".geojson";
            Path filePath = new File(folder, fileName).toPath();
            Files.writeString(filePath, geoJson);

            String message = "GeoJSON guardado en: " + filePath.toString();
            PlayerLogger.info(sender, message, (String) null);
        } catch (Exception e) {
            ConsoleLogger.error("Error al generar GeoJSON de la región de país con ID " + regionId, e);
            PlayerLogger.error(sender, "Error al guardar GeoJSON: " + e.getMessage(), (String) null);
        }
        return true; 
    }

}