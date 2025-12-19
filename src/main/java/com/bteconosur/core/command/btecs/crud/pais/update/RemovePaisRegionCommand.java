package com.bteconosur.core.command.btecs.crud.pais.update;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.BTEConoSur;
import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.util.ConsoleLogger;
import com.bteconosur.db.DBManager;
import com.bteconosur.db.model.Pais;
import com.bteconosur.db.model.RegionPais;

public class RemovePaisRegionCommand extends BaseCommand {

    private final YamlConfiguration lang;
    private final DBManager dbManager;
    private final ConsoleLogger logger;

    public RemovePaisRegionCommand() {
        super("removeregion", "Eliminar región de un País.", "<id_pais> <id_region>", CommandMode.BOTH);
        ConfigHandler configHandler = ConfigHandler.getInstance();
        lang = configHandler.getLang();
        dbManager = DBManager.getInstance();
        logger = BTEConoSur.getConsoleLogger();
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        if (args.length != 2) {
            String message = lang.getString("help-command-usage").replace("%command%", getFullCommand().replace(" " + command, ""));
            sender.sendMessage(message);
            return true;
        }

        Long paisId;
        try {
            paisId = Long.parseLong(args[0]);
        } catch (NumberFormatException ex) {
            String message = lang.getString("crud-not-valid-id").replace("%entity%", "Pais").replace("%id%", args[0]);
            sender.sendMessage(message);
            return true;
        }

        if (!dbManager.exists(Pais.class, paisId)) {
            String message = lang.getString("crud-read-not-found").replace("%entity%", "Pais").replace("%id%", args[0]);
            sender.sendMessage(message);
            return true;
        }

        Long regionId;
        try {
            regionId = Long.parseLong(args[1]);
        } catch (NumberFormatException ex) {
            String message = lang.getString("crud-not-valid-id").replace("%entity%", "RegionPais").replace("%id%", args[1]);
            sender.sendMessage(message);
            return true;
        }

        if (!dbManager.exists(RegionPais.class, regionId)) {
            String message = lang.getString("crud-read-not-found").replace("%entity%", "RegionPais").replace("%id%", args[1]);
            sender.sendMessage(message);
            return true;
        }

        RegionPais region = dbManager.get(RegionPais.class, regionId);
        if (!region.getPais().getId().equals(paisId)) {
            sender.sendMessage("La región con ID " + regionId + " no pertenece al país con ID " + paisId);
            return true;
        }

        Pais pais = dbManager.get(Pais.class, paisId);
        pais.getRegiones().remove(region);
        dbManager.remove(region);
        dbManager.merge(pais);

        String message = lang.getString("crud-delete").replace("%entity%", "RegionPais").replace("%id%", args[1]);
        sender.sendMessage(message);
        return true;
    }

}
