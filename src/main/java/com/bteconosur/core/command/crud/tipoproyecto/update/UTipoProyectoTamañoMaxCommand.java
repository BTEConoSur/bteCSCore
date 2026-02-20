package com.bteconosur.core.command.crud.tipoproyecto.update;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.DBManager;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.TipoProyecto;

public class UTipoProyectoTamañoMaxCommand extends BaseCommand {

    private final DBManager dbManager;

    public UTipoProyectoTamañoMaxCommand() {
        super("tamañomax", "Actualizar tamaño máximo de un Tipo deProyecto.", "<id> <tamaño>", CommandMode.BOTH);
        dbManager = DBManager.getInstance();
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player commandPlayer = Player.getBTECSPlayer((org.bukkit.entity.Player) sender);
        Language language = commandPlayer.getLanguage();
        if (args.length != 2) {
            String message = LanguageHandler.getText(language, "help-command-usage").replace("%comando%", getFullCommand().replace(" " + command, ""));
            PlayerLogger.info(sender, message, (String) null);
            return true;
        }

        Long id;
        try {
            id = Long.parseLong(args[0]);
        } catch (NumberFormatException ex) {
            String message = LanguageHandler.getText(language, "crud.not-valid-id").replace("%entity%", "Tipo de Proyecto").replace("%id%", args[0]);
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        if (!dbManager.exists(TipoProyecto.class, id)) {
            String message = LanguageHandler.getText(language, "crud.read-not-found").replace("%entity%", "Tipo de Proyecto").replace("%id%", args[0]);
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        Integer nuevoTamano;
        try {
            nuevoTamano = Integer.parseInt(args[1]);
        } catch (NumberFormatException ex) {
            String message = LanguageHandler.getText(language, "crud.not-valid-parse").replace("%entity%", "tamaño_max").replace("%value%", args[1]).replace("%type%", "Integer");
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        TipoProyecto tipoProyecto = dbManager.get(TipoProyecto.class, id);
        
        if (nuevoTamano < tipoProyecto.getTamanoMin()) {
            String message = LanguageHandler.getText(language, "crud.not-valid-name").replace("%entity%", "Tipo de Proyecto").replace("%name%", "Tamaño máximo").replace("%reason%", "No puede ser menor al tamaño mínimo.");
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        tipoProyecto.setTamanoMax(nuevoTamano);
        dbManager.merge(tipoProyecto);

        String message = LanguageHandler.getText(language, "crud.update").replace("%entity%", "Tipo de Proyecto").replace("%id%", args[0]);
        PlayerLogger.info(sender, message, (String) null);
        return true;
    }
}
