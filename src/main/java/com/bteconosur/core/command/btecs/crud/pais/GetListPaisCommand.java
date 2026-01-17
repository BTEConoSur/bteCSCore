package com.bteconosur.core.command.btecs.crud.pais;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.DBManager;
import com.bteconosur.db.model.Pais;

public class GetListPaisCommand extends BaseCommand {

    private final YamlConfiguration lang;
    private final DBManager dbManager;

    public GetListPaisCommand() {
        super("list", "Obtener lista de todos los países.", "", CommandMode.BOTH);
        
        ConfigHandler configHandler = ConfigHandler.getInstance();
        lang = configHandler.getLang();
        dbManager = DBManager.getInstance();
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        List<Pais> paises = dbManager.selectAll(Pais.class);

        if (paises.isEmpty()) {
            sender.sendMessage(lang.getString("get-list-empty").replace("%entity%", "Países"));
            return true;
        }

        String message = lang.getString("get-list-command.header").replace("%entity%", "Países");

        String lineFormat = lang.getString("get-list-command.line");
        for (Pais pais : paises) {
            String line = lineFormat
                .replace("%id%", String.valueOf(pais.getId()))
                .replace("%details%", pais.getNombre());
            message += "\n" + line;
        }

        String footer = lang.getString("get-list-command.footer");
        if (footer != null && !footer.isEmpty()) message += "\n" + footer;

        PlayerLogger.send(sender, message, (String) null);
        return true;
    }
    
}
