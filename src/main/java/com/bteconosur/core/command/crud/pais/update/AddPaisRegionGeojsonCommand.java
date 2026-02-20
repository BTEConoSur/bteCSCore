package com.bteconosur.core.command.crud.pais.update;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.locationtech.jts.geom.Polygon;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.GeoJsonUtils;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.DBManager;
import com.bteconosur.db.model.Pais;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.RegionPais;

public class AddPaisRegionGeojsonCommand extends BaseCommand {

    private final DBManager dbManager;

    public AddPaisRegionGeojsonCommand() {
        super("addregiones", "Cargar regiones de un País desde un archivo GeoJSON. Colocar en geojson/countries.", "<id_pais> <archivo_geojson>", CommandMode.BOTH);
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

        Long paisId;
        try {
            paisId = Long.parseLong(args[0]);
        } catch (NumberFormatException ex) {
            String message = LanguageHandler.getText(language, "crud.not-valid-id").replace("%entity%", "Pais").replace("%id%", args[0]);
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        if (!dbManager.exists(Pais.class, paisId)) {
            String message = LanguageHandler.getText(language, "crud.read-not-found").replace("%entity%", "Pais").replace("%id%", args[0]);
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        String fileName = args[1];
        List<Polygon> polygons = GeoJsonUtils.geoJsonToPolygons("countries", fileName);
        if (polygons == null || polygons.isEmpty()) {
            PlayerLogger.error(sender, LanguageHandler.getText(language, "crud.not-valid-geojson"), (String) null);
            return true;
        }

        Pais pais = dbManager.get(Pais.class, paisId);

        for (int i = 0; i < polygons.size(); i++) {
            String regionName = pais.getNombre() + "_" + fileName + "_" + (i + 1);
            RegionPais region = new RegionPais(pais, regionName, polygons.get(i));
            dbManager.save(region);
        }

        String message = LanguageHandler.getText(language, "crud.update").replace("%entity%", "Pais").replace("%id%", args[0]);
        PlayerLogger.info(sender, message, (String) null);
        return true;
    }


}
