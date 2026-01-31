package com.bteconosur.core.command.crud.division.update;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.locationtech.jts.geom.Polygon;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.core.util.RegionUtils;
import com.bteconosur.db.DBManager;
import com.bteconosur.db.model.Division;

public class UpdateDivisionPoligonoCommand extends BaseCommand {

    private final YamlConfiguration lang;
    private final DBManager dbManager;

    public UpdateDivisionPoligonoCommand() {
        super("poligono", "Actualizar polígono de una Division con la región seleccionada.", "<id>", CommandMode.PLAYER_ONLY);
        ConfigHandler configHandler = ConfigHandler.getInstance();
        lang = configHandler.getLang();
        dbManager = DBManager.getInstance();
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        if (args.length != 1) {
            String message = lang.getString("help-command-usage").replace("%command%", getFullCommand().replace(" " + command, ""));
            PlayerLogger.info(sender, message, (String) null);
            return true;
        }

        Long id;
        try {
            id = Long.parseLong(args[0]);
        } catch (NumberFormatException ex) {
            String message = lang.getString("crud-not-valid-id").replace("%entity%", "Division").replace("%id%", args[0]);
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        if (!dbManager.exists(Division.class, id)) {
            String message = lang.getString("crud-read-not-found").replace("%entity%", "Division").replace("%id%", args[0]);
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        Player player = (Player) sender;
        Polygon regionPolygon = RegionUtils.getPolygon(player);
        if (regionPolygon == null) return true;

        Division division = dbManager.get(Division.class, id);
        //division.set(regionPolygon);
        dbManager.merge(division);
        //TODO: poligono desde geojson
        String message = lang.getString("crud-update").replace("%entity%", "Division").replace("%id%", args[0]);
        PlayerLogger.info(sender, message, (String) null);
        return true;
    }

}
