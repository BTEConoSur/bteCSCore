package com.bteconosur.core.command.crud.rangousuario;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.DBManager;
import com.bteconosur.db.model.RangoUsuario;

public class GetListRangoUsuarioCommand extends BaseCommand {

    private final YamlConfiguration lang;
    private final DBManager dbManager;

    public GetListRangoUsuarioCommand() {
        super("list", "Obtener lista de todos los rangos de usuario.", "", CommandMode.BOTH);
        
        ConfigHandler configHandler = ConfigHandler.getInstance();
        lang = configHandler.getLang();
        dbManager = DBManager.getInstance();
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        List<RangoUsuario> rangos = dbManager.selectAll(RangoUsuario.class);

        if (rangos.isEmpty()) {
            String message = lang.getString("get-list-empty").replace("%entity%", "Rangos de Usuario");
            PlayerLogger.warn(sender, message, (String) null);
            return true;
        }

        String message = lang.getString("get-list-command.header").replace("%entity%", "Rangos de Usuario");

        String lineFormat = lang.getString("get-list-command.line");
        for (RangoUsuario rango : rangos) {
            String line = lineFormat
                .replace("%id%", String.valueOf(rango.getId()))
                .replace("%details%", rango.getNombre());
            message += "\n" + line;
        }

        String footer = lang.getString("get-list-command.footer");
        if (footer != null && !footer.isEmpty()) message += "\n" + footer;
        PlayerLogger.send(sender, message, (String) null);
        return true;
    }
    
}
