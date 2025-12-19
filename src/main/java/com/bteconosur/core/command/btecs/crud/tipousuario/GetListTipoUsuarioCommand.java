package com.bteconosur.core.command.btecs.crud.tipousuario;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.ConfigHandler;
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
            sender.sendMessage(message);
            return true;
        }

        String header = lang.getString("get-list-command.header").replace("%entity%", "Tipos de Usuario");
        sender.sendMessage(header);

        String lineFormat = lang.getString("get-list-command.line");
        for (TipoUsuario tipo : tipos) {
            String line = lineFormat
                .replace("%id%", String.valueOf(tipo.getId()))
                .replace("%details%", tipo.getNombre());
            sender.sendMessage(line);
        }

        String footer = lang.getString("get-list-command.footer");
        if (footer != null && !footer.isEmpty()) sender.sendMessage(footer);
        

        return true;
    }
    
}
