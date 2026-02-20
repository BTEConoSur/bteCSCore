package com.bteconosur.core.command.crud.rangousuario;

import java.util.List;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.DBManager;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.RangoUsuario;

public class GetListRangoUsuarioCommand extends BaseCommand {

    private final DBManager dbManager;

    public GetListRangoUsuarioCommand() {
        super("list", "Obtener lista de todos los rangos de usuario.", "", CommandMode.BOTH);
        dbManager = DBManager.getInstance();
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        List<RangoUsuario> rangos = dbManager.selectAll(RangoUsuario.class);
        Player commandPlayer = Player.getBTECSPlayer((org.bukkit.entity.Player) sender);
        Language language = commandPlayer.getLanguage();
        if (rangos.isEmpty()) {
            String message = LanguageHandler.getText(language, "get-list.empty").replace("%entity%", "Rangos de Usuario");
            PlayerLogger.warn(sender, message, (String) null);
            return true;
        }

        String message = LanguageHandler.getText(language, "get-list.header").replace("%entity%", "Rangos de Usuario");

        String lineFormat = LanguageHandler.getText(language, "get-list.line");
        for (RangoUsuario rango : rangos) {
            String line = lineFormat
                .replace("%id%", String.valueOf(rango.getId()))
                .replace("%details%", rango.getNombre());
            message += "\n" + line;
        }

        String footer = LanguageHandler.getText(language, "get-list.footer");
        if (footer != null && !footer.isEmpty()) message += "\n" + footer;
        PlayerLogger.send(sender, message, (String) null);
        return true;
    }
    
}
