package com.bteconosur.core.command.crud.pais.update;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.BTEConoSur;
import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.util.GeoJsonUtils;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.DBManager;
import com.bteconosur.db.model.Division;
import com.bteconosur.db.model.Pais;

public class AddDivisionesGeojsonCommand extends BaseCommand {

    private final YamlConfiguration lang;
    private final DBManager dbManager;

    public AddDivisionesGeojsonCommand() {
        super("adddivisiones", "Cargar divisiones de un País desde archivos GeoJSON.", "<id_pais> <carpeta_geojson>", CommandMode.CONSOLE_ONLY);
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

        String path = args[1];
        Pais pais = dbManager.get(Pais.class, paisId);
        
        PlayerLogger.info(sender, "Cargando divisiones de pais " + pais.getNombre() + " en segundo plano...", (String) null);
        
        Bukkit.getScheduler().runTaskAsynchronously(BTEConoSur.getInstance(), () -> {
            try {
                List<Division> divisions = GeoJsonUtils.geoJsonToDivisions(pais, path);
                
                if (divisions == null || divisions.isEmpty()) {
                    PlayerLogger.error(sender, "No se pudo cargar los GeoJSON o \"" + path + "\" no contiene divisiones.", (String) null);
                    return;
                }

                for (Division division : divisions) {
                    dbManager.save(division);
                }

                String message = lang.getString("crud-update").replace("%entity%", "Pais").replace("%id%", args[0]);
                PlayerLogger.info(sender, message + " (" + divisions.size() + " divisiones cargadas)", (String) null);
            } catch (Exception e) {
                PlayerLogger.error(sender, "Error al cargar divisiones: " + e.getMessage(), (String) null);
                e.printStackTrace();
            }
        });
        return true;
    }

}
