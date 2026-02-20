package com.bteconosur.core.command.crud.pais.update;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import com.bteconosur.core.BTEConoSur;
import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.GeoJsonUtils;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.DBManager;
import com.bteconosur.db.model.Division;
import com.bteconosur.db.model.Pais;
import com.bteconosur.db.model.Player;

public class AddDivisionesGeojsonCommand extends BaseCommand {

    private final DBManager dbManager;

    public AddDivisionesGeojsonCommand() {
        super("adddivisiones", "Cargar divisiones de un País desde archivos GeoJSON.", "<id_pais> <carpeta_geojson>", CommandMode.CONSOLE_ONLY);
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

        String path = args[1];
        Pais pais = dbManager.get(Pais.class, paisId);
        
        PlayerLogger.info(sender, LanguageHandler.replaceMC("crud.loading-division", language, pais), (String) null);
        
        Bukkit.getScheduler().runTaskAsynchronously(BTEConoSur.getInstance(), () -> {
            try {
                List<Division> divisions = GeoJsonUtils.geoJsonToDivisions(pais, path);
                
                if (divisions == null || divisions.isEmpty()) {
                    PlayerLogger.error(sender, LanguageHandler.getText(language, "crud.no-divisions").replace("%path%", path), (String) null);
                    return;
                }

                for (Division division : divisions) {
                    dbManager.save(division);
                }

                String message = LanguageHandler.getText(language, "crud.update").replace("%entity%", "Pais").replace("%id%", args[0]);
                PlayerLogger.info(sender, message + LanguageHandler.getText(language, "crud.loaded-division").replace("%count%", String.valueOf(divisions.size())), (String) null);
            } catch (Exception e) {
                PlayerLogger.error(sender, LanguageHandler.getText(language, "crud.division-exception").replace("%error%", e.getMessage()), (String) null);
                e.printStackTrace();
            }
        });
        return true;
    }

}
