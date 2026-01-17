package com.bteconosur.core.command.btecs.crud.pais;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.DBManager;
import com.bteconosur.db.model.Pais;
import com.bteconosur.db.model.RegionPais;

public class GetListRegionPaisCommand extends BaseCommand {

    private final YamlConfiguration lang;
    private final DBManager dbManager;

    public GetListRegionPaisCommand() {
        super("listregions", "Obtener lista de regiones de un país.", "<id_pais>", CommandMode.BOTH);
        
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

        Pais pais = dbManager.get(Pais.class, paisId);

        if (pais.getRegiones().isEmpty()) {
            String message = lang.getString("get-list-empty").replace("%entity%", "Regiones de " + pais.getNombre());
            PlayerLogger.warn(sender, message, (String) null);
            return true;
        }

        String message = lang.getString("get-list-command.header").replace("%entity%", "Regiones de " + pais.getNombre());

        String lineFormat = lang.getString("get-list-command.line");
        for (RegionPais region : pais.getRegiones()) {
            String line = lineFormat
                .replace("%id%", String.valueOf(region.getId()))
                .replace("%details%", region.getNombre());
            message += "\n" + line;
        }

        String footer = lang.getString("get-list-command.footer");
        if (footer != null && !footer.isEmpty()) PlayerLogger.info(sender, footer, (String) null);

        PlayerLogger.send(sender, message, (String) null);
        return true;
    }
    
}
