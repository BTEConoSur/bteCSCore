package com.bteconosur.core.command.crud.division.update;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.locationtech.jts.geom.Polygon;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.GeoJsonUtils;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.DBManager;
import com.bteconosur.db.model.Division;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.RegionDivision;

public class AddDivisionRegionGeojsonCommand extends BaseCommand {

    private final DBManager dbManager;

    public AddDivisionRegionGeojsonCommand() {
        super("addregionesdivision", "Cargar regiones de una División desde un archivo GeoJSON. Colocar en geojson/divisions.", "<id_division> <archivo_geojson>", CommandMode.BOTH);
        dbManager = DBManager.getInstance();
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player commandPlayer = Player.getBTECSPlayer((org.bukkit.entity.Player) sender);
        Language language = commandPlayer.getLanguage();
        if (args.length != 2) {
            String message = LanguageHandler.getText(language, "help-command-usage").replace("%comando%", getFullCommand().replace(" " + command, ""));
            PlayerLogger.info(sender, message, (String) null);
            return true;
        }

        Long divisionId;
        try {
            divisionId = Long.parseLong(args[0]);
        } catch (NumberFormatException ex) {
            String message = LanguageHandler.getText(language, "crud.not-valid-id").replace("%entity%", "Division").replace("%id%", args[0]);
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        if (!dbManager.exists(Division.class, divisionId)) {
            String message = LanguageHandler.getText(language, "crud.read-not-found").replace("%entity%", "Division").replace("%id%", args[0]);
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        String fileName = args[1];
        List<Polygon> polygons = GeoJsonUtils.geoJsonToPolygons("divisions", fileName);
        if (polygons == null || polygons.isEmpty()) {
            String message = LanguageHandler.getText(language, "crud.not-valid-geojson");
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        Division division = dbManager.get(Division.class, divisionId);

        for (int i = 0; i < polygons.size(); i++) {
            String regionName = division.getNombre() + "_" + fileName + "_" + (i + 1);
            RegionDivision region = new RegionDivision(division, regionName, polygons.get(i));
            dbManager.save(region);
        }

        String message = LanguageHandler.getText(language, "crud.update").replace("%entity%", "Division").replace("%id%", args[0]);
        PlayerLogger.info(sender, message, (String) null);
        return true;
    }

}
