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

public class GetListPaisCommand extends BaseCommand {

    private final DBManager dbManager;

    public GetListPaisCommand() {
        super("list", "Obtener lista de todos los países.", "", CommandMode.BOTH);
        dbManager = DBManager.getInstance();
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player commandPlayer = Player.getBTECSPlayer((org.bukkit.entity.Player) sender);
        Language language = commandPlayer.getLanguage();
        List<Pais> paises = dbManager.selectAll(Pais.class);

        if (paises.isEmpty()) {
            sender.sendMessage(LanguageHandler.getText(language, "get-list.empty").replace("%entity%", "Países"));
            return true;
        }

        String message = LanguageHandler.getText(language, "get-list.header").replace("%entity%", "Países");

        String lineFormat = LanguageHandler.getText(language, "get-list.line");
        for (Pais pais : paises) {
            String line = lineFormat
                .replace("%id%", String.valueOf(pais.getId()))
                .replace("%details%", pais.getNombre());
            message += "\n" + line;
        }

        String footer = LanguageHandler.getText(language, "get-list.footer");
        if (footer != null && !footer.isEmpty()) message += "\n" + footer;

        PlayerLogger.send(sender, message, (String) null);
        return true;
    }
    
}
