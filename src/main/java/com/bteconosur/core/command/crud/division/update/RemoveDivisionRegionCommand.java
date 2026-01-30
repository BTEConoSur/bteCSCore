package com.bteconosur.core.command.crud.division.update;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.DBManager;
import com.bteconosur.db.model.Division;
import com.bteconosur.db.model.RegionDivision;

public class RemoveDivisionRegionCommand extends BaseCommand {

    private final YamlConfiguration lang;
    private final DBManager dbManager;

    public RemoveDivisionRegionCommand() {
        super("removeregion", "Eliminar región de una División.", "<id_division> <id_region>", CommandMode.BOTH);
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

        Long divisionId;
        try {
            divisionId = Long.parseLong(args[0]);
        } catch (NumberFormatException ex) {
            String message = lang.getString("crud-not-valid-id").replace("%entity%", "Division").replace("%id%", args[0]);
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        if (!dbManager.exists(Division.class, divisionId)) {
            String message = lang.getString("crud-read-not-found").replace("%entity%", "Division").replace("%id%", args[0]);
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        Long regionId;
        try {
            regionId = Long.parseLong(args[1]);
        } catch (NumberFormatException ex) {
            String message = lang.getString("crud-not-valid-id").replace("%entity%", "RegionDivision").replace("%id%", args[1]);
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        if (!dbManager.exists(RegionDivision.class, regionId)) {
            String message = lang.getString("crud-read-not-found").replace("%entity%", "RegionDivision").replace("%id%", args[1]);
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        RegionDivision region = dbManager.get(RegionDivision.class, regionId);
        if (!region.getDivision().getId().equals(divisionId)) {
            PlayerLogger.error(sender, "La región con ID " + regionId + " no pertenece a la división con ID " + divisionId, (String) null);
            return true;
        }

        Division division = dbManager.get(Division.class, divisionId);
        division.getRegiones().remove(region);
        dbManager.remove(region);
        dbManager.merge(division);

        String message = lang.getString("crud-delete").replace("%entity%", "RegionDivision").replace("%id%", args[1]);
        PlayerLogger.info(sender, message, (String) null);
        return true;
    }

}
