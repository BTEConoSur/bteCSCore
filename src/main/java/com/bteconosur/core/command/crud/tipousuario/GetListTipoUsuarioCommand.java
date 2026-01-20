package com.bteconosur.core.command.crud.tipousuario;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.DBManager;
import com.bteconosur.db.model.TipoUsuario;

public class GetListTipoUsuarioCommand extends BaseCommand {

    private final YamlConfiguration lang;
    private final DBManager dbManager;

    public GetListTipoUsuarioCommand() {
        super("list", "Obtener lista de todos los tipos de usuario.", "", CommandMode.BOTH);
        
        ConfigHandler configHandler = ConfigHandler.getInstance();
        lang = configHandler.getLang();
        dbManager = DBManager.getInstance();
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        List<TipoUsuario> tipos = dbManager.selectAll(TipoUsuario.class);

        if (tipos.isEmpty()) {
            String message = lang.getString("get-list-empty").replace("%entity%", "Tipos de Usuario");
            PlayerLogger.warn(sender, message, (String) null);
            return true;
        }

        String message = lang.getString("get-list-command.header").replace("%entity%", "Tipos de Usuario");

        String lineFormat = lang.getString("get-list-command.line");
        for (TipoUsuario tipo : tipos) {
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
