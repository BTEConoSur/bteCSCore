package com.bteconosur.core.command.crud.pais.update;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.locationtech.jts.geom.Polygon;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.util.GeoJsonUtils;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.DBManager;
import com.bteconosur.db.model.Pais;
import com.bteconosur.db.model.RegionPais;

public class UPaisGeoJsonCommand extends BaseCommand {

    private final YamlConfiguration lang;
    private final DBManager dbManager;

    public UPaisGeoJsonCommand() {
        super("geojson", "Cargar regiones de un País desde un archivo GeoJSON.", "<id_pais> <archivo_geojson>", CommandMode.BOTH);
        ConfigHandler configHandler = ConfigHandler.getInstance();
        lang = configHandler.getLang();
        dbManager = DBManager.getInstance();
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        if (args.length != 2) {
            String message = lang.getString("help-command-usage").replace("%command%", getFullCommand().replace(" " + command, ""));
            PlayerLogger.info(sender, message, (String) null);
            return true;
        }

        Long paisId;
        try {
            paisId = Long.parseLong(args[0]);
        } catch (NumberFormatException ex) {
            String message = lang.getString("crud-not-valid-id").replace("%entity%", "Pais").replace("%id%", args[0]);
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        if (!dbManager.exists(Pais.class, paisId)) {
            String message = lang.getString("crud-read-not-found").replace("%entity%", "Pais").replace("%id%", args[0]);
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        String fileName = args[1];
        List<Polygon> polygons = GeoJsonUtils.geoJsonToCountry(fileName);
        if (polygons == null || polygons.isEmpty()) {
            PlayerLogger.error(sender, "No se pudo cargar el GeoJSON o no contiene polígonos.", (String) null);
            return true;
        }

        Pais pais = dbManager.get(Pais.class, paisId);

        for (int i = 0; i < polygons.size(); i++) {
            String regionName = pais.getNombre() + "_" + fileName + "_" + (i + 1);
            RegionPais region = new RegionPais(pais, regionName, polygons.get(i));
            dbManager.save(region);
        }

        String message = lang.getString("crud-update").replace("%entity%", "Pais").replace("%id%", args[0]);
        PlayerLogger.info(sender, message, (String) null);
        return true;
    }


}
