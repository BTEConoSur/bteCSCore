package com.bteconosur.core.command.crud.pais;

import java.util.List;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.DBManager;
import com.bteconosur.db.model.Pais;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.RegionPais;
import com.bteconosur.db.registry.PaisRegistry;

public class GetListRegionPaisCommand extends BaseCommand {

    private final DBManager dbManager;

    public GetListRegionPaisCommand() {
        super("listregions", "Obtener lista de regiones de un país.", "<id_pais>", CommandMode.BOTH);
        dbManager = DBManager.getInstance();
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player commandPlayer = Player.getBTECSPlayer((org.bukkit.entity.Player) sender);
        Language language = commandPlayer.getLanguage();
        if (args.length != 1) {
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

        Pais pais = dbManager.get(Pais.class, paisId);
        List<RegionPais> regiones = PaisRegistry.getInstance().getRegions(pais);
        if (regiones == null || regiones.isEmpty()) {
            String message = LanguageHandler.getText(language, "get-list.empty").replace("%entity%", "Regiones de " + pais.getNombre());
            PlayerLogger.warn(sender, message, (String) null);
            return true;
        }

        String message = LanguageHandler.getText(language, "get-list.header").replace("%entity%", "Regiones de " + pais.getNombre());

        String lineFormat = LanguageHandler.getText(language, "get-list.line");
        for (RegionPais region : regiones) {
            String line = lineFormat
                .replace("%id%", String.valueOf(region.getId()))
                .replace("%details%", region.getNombre());
            message += "\n" + line;
        }

        String footer = LanguageHandler.getText(language, "get-list.footer");
        if (footer != null && !footer.isEmpty()) PlayerLogger.info(sender, footer, (String) null);

        PlayerLogger.send(sender, message, (String) null);
        return true;
    }
    
}
