package com.bteconosur.core.command.crud.tipoproyecto;

import java.util.List;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.DBManager;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.TipoProyecto;

public class GetListTipoProyectoCommand extends BaseCommand {

    private final DBManager dbManager;

    public GetListTipoProyectoCommand() {
        super("list", "Obtener lista de todos los tipos de proyecto.", "", CommandMode.BOTH);
        dbManager = DBManager.getInstance();
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        List<TipoProyecto> tipos = dbManager.selectAll(TipoProyecto.class);
        Player commandPlayer = Player.getBTECSPlayer((org.bukkit.entity.Player) sender);
        Language language = commandPlayer.getLanguage();
        if (tipos.isEmpty()) {
            String message = LanguageHandler.getText(language, "get-list.empty").replace("%entity%", "Tipos de Proyecto");
            PlayerLogger.warn(sender, message, (String) null);
            return true;
        }

        String message = LanguageHandler.getText(language, "get-list.header").replace("%entity%", "Tipos de Proyecto");

        String lineFormat = LanguageHandler.getText(language, "get-list.line");
        for (TipoProyecto tipo : tipos) {
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
