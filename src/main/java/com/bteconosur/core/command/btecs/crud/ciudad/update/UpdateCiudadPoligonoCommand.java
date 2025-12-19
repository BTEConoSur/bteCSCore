package com.bteconosur.core.command.btecs.crud.ciudad.update;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.locationtech.jts.geom.Polygon;

import com.bteconosur.core.BTEConoSur;
import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.util.ConsoleLogger;
import com.bteconosur.core.util.RegionUtils;
import com.bteconosur.db.DBManager;
import com.bteconosur.db.model.Ciudad;

public class UpdateCiudadPoligonoCommand extends BaseCommand {

    private final YamlConfiguration lang;
    private final DBManager dbManager;

    public UpdateCiudadPoligonoCommand() {
        super("poligono", "Actualizar polígono de una Ciudad con la región seleccionada.", "<id>", CommandMode.PLAYER_ONLY);
        ConfigHandler configHandler = ConfigHandler.getInstance();
        lang = configHandler.getLang();
        dbManager = DBManager.getInstance();
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        if (args.length != 1) {
            String message = lang.getString("help-command-usage").replace("%command%", getFullCommand().replace(" " + command, ""));
            sender.sendMessage(message);
            return true;
        }

        Long id;
        try {
            id = Long.parseLong(args[0]);
        } catch (NumberFormatException ex) {
            String message = lang.getString("crud-not-valid-id").replace("%entity%", "Ciudad").replace("%id%", args[0]);
            sender.sendMessage(message);
            return true;
        }

        if (!dbManager.exists(Ciudad.class, id)) {
            String message = lang.getString("crud-read-not-found").replace("%entity%", "Ciudad").replace("%id%", args[0]);
            sender.sendMessage(message);
            return true;
        }

        Player player = (Player) sender;
        Polygon regionPolygon = RegionUtils.getPolygon(player);
        if (regionPolygon == null) return true;

        Ciudad ciudad = dbManager.get(Ciudad.class, id);
        ciudad.setPoligono(regionPolygon);
        dbManager.merge(ciudad);

        String message = lang.getString("crud-update").replace("%entity%", "Ciudad").replace("%id%", args[0]);
        sender.sendMessage(message);
        return true;
    }

}
