package com.bteconosur.core.command.crud.tipoproyecto;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.DBManager;
import com.bteconosur.db.model.TipoProyecto;

public class GetListTipoProyectoCommand extends BaseCommand {

    private final YamlConfiguration lang;
    private final DBManager dbManager;

    public GetListTipoProyectoCommand() {
        super("list", "Obtener lista de todos los tipos de proyecto.", "", CommandMode.BOTH);
        
        ConfigHandler configHandler = ConfigHandler.getInstance();
        lang = configHandler.getLang();
        dbManager = DBManager.getInstance();
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        List<TipoProyecto> tipos = dbManager.selectAll(TipoProyecto.class);

        if (tipos.isEmpty()) {
            String message = lang.getString("get-list-empty").replace("%entity%", "Tipos de Proyecto");
            PlayerLogger.warn(sender, message, (String) null);
            return true;
        }

        String message = lang.getString("get-list-command.header").replace("%entity%", "Tipos de Proyecto");

        String lineFormat = lang.getString("get-list-command.line");
        for (TipoProyecto tipo : tipos) {
            String line = lineFormat
                .replace("%id%", String.valueOf(tipo.getId()))
                .replace("%details%", tipo.getNombre());
            message += "\n" + line;
        }

        String footer = lang.getString("get-list-command.footer");
        if (footer != null && !footer.isEmpty()) message += "\n" + footer;
        PlayerLogger.send(sender, message, (String) null);

        return true;
    }
    
}
