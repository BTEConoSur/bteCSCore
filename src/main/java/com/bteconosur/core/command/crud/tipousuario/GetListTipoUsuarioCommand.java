package com.bteconosur.core.command.crud.tipousuario;

import java.util.List;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.DBManager;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.TipoUsuario;

public class GetListTipoUsuarioCommand extends BaseCommand {

    private final DBManager dbManager;

    public GetListTipoUsuarioCommand() {
        super("list", "Obtener lista de todos los tipos de usuario.", "", CommandMode.BOTH);
        dbManager = DBManager.getInstance();
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        List<TipoUsuario> tipos = dbManager.selectAll(TipoUsuario.class);
        Player commandPlayer = Player.getBTECSPlayer((org.bukkit.entity.Player) sender);
        Language language = commandPlayer.getLanguage();
        if (tipos.isEmpty()) {
            String message = LanguageHandler.getText(language, "get-list.empty").replace("%entity%", "Tipos de Usuario");
            PlayerLogger.warn(sender, message, (String) null);
            return true;
        }

        String message = LanguageHandler.getText(language, "get-list.header").replace("%entity%", "Tipos de Usuario");

        String lineFormat = LanguageHandler.getText(language, "get-list.line");
        for (TipoUsuario tipo : tipos) {
            String line = lineFormat
                .replace("%id%", String.valueOf(tipo.getId()))
                .replace("%details%", tipo.getNombre());
            message += "\n" + line;
        }

        String footer = LanguageHandler.getText(language, "get-list.footer");
        if (footer != null && !footer.isEmpty()) message += "\n" + footer;
        PlayerLogger.send(sender, message, (String) null);

        return true;
    }
    
}
