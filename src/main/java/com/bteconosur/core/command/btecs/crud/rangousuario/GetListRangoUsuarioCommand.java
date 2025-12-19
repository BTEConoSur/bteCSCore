package com.bteconosur.core.command.btecs.crud.rangousuario;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.ConfigHandler;
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
            sender.sendMessage(message);
            return true;
        }

        String header = lang.getString("get-list-command.header").replace("%entity%", "Rangos de Usuario");
        sender.sendMessage(header);

        String lineFormat = lang.getString("get-list-command.line");
        for (RangoUsuario rango : rangos) {
            String line = lineFormat
                .replace("%id%", String.valueOf(rango.getId()))
                .replace("%details%", rango.getNombre());
            sender.sendMessage(line);
        }

        String footer = lang.getString("get-list-command.footer");
        if (footer != null && !footer.isEmpty()) sender.sendMessage(footer);

        return true;
    }
    
}
