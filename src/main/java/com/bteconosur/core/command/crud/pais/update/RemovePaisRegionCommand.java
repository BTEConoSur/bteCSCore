package com.bteconosur.core.command.crud.pais.update;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.DBManager;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.RegionPais;

public class RemovePaisRegionCommand extends BaseCommand {

    private final DBManager dbManager;

    public RemovePaisRegionCommand() {
        super("removeregion", "Eliminar región de un País.", "<id_region>", CommandMode.BOTH);
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

        Long regionId;
        try {
            regionId = Long.parseLong(args[0]);
        } catch (NumberFormatException ex) {
            String message = LanguageHandler.getText(language, "crud.not-valid-id").replace("%entity%", "RegionPais").replace("%id%", args[0]);
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        if (!dbManager.exists(RegionPais.class, regionId)) {
            String message = LanguageHandler.getText(language, "crud.read-not-found").replace("%entity%", "RegionPais").replace("%id%", args[0]);
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        RegionPais region = dbManager.get(RegionPais.class, regionId);
        dbManager.remove(region);

        String message = LanguageHandler.getText(language, "crud.delete").replace("%entity%", "RegionPais").replace("%id%", args[1]);
        PlayerLogger.info(sender, message, (String) null);
        return true;
    }

}
// TODO: probar esto