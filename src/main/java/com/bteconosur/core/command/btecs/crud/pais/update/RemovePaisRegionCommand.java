package com.bteconosur.core.command.btecs.crud.pais.update;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.DBManager;
import com.bteconosur.db.model.Pais;
import com.bteconosur.db.model.RegionPais;

public class RemovePaisRegionCommand extends BaseCommand {

    private final YamlConfiguration lang;
    private final DBManager dbManager;

    public RemovePaisRegionCommand() {
        super("removeregion", "Eliminar región de un País.", "<id_pais> <id_region>", CommandMode.BOTH);
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

        Long regionId;
        try {
            regionId = Long.parseLong(args[1]);
        } catch (NumberFormatException ex) {
            String message = lang.getString("crud-not-valid-id").replace("%entity%", "RegionPais").replace("%id%", args[1]);
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        if (!dbManager.exists(RegionPais.class, regionId)) {
            String message = lang.getString("crud-read-not-found").replace("%entity%", "RegionPais").replace("%id%", args[1]);
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        RegionPais region = dbManager.get(RegionPais.class, regionId);
        if (!region.getPais().getId().equals(paisId)) {
            PlayerLogger.error(sender, "La región con ID " + regionId + " no pertenece al país con ID " + paisId, (String) null);
            return true;
        }

        Pais pais = dbManager.get(Pais.class, paisId);
        pais.getRegiones().remove(region);
        dbManager.remove(region);
        dbManager.merge(pais);

        String message = lang.getString("crud-delete").replace("%entity%", "RegionPais").replace("%id%", args[1]);
        PlayerLogger.info(sender, message, (String) null);
        return true;
    }

}
