package com.bteconosur.core.command.crud.division;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.DBManager;
import com.bteconosur.db.model.Division;
import com.bteconosur.db.model.Pais;
import com.bteconosur.db.model.Player;

public class CDivisionCommand extends BaseCommand {

    private final DBManager dbManager;

    public CDivisionCommand() {
        super("create", "Crear una nueva Division. Se crea sin poligono.", "<nombre> <nam> <gna> <id_pais> <contexto>", CommandMode.BOTH);
        dbManager = DBManager.getInstance();
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player commandPlayer = Player.getBTECSPlayer((org.bukkit.entity.Player) sender);
        Language language = commandPlayer.getLanguage();
        if (args.length != 5) {
            String message = LanguageHandler.getText(language, "help-command-usage").replace("%comando%", getFullCommand().replace(" " + command, ""));
            PlayerLogger.info(sender, message, (String) null);
            return true;
        }

        String nombre = args[0];
        String nombrePublico = args[1];
        String tipoDivision = args[2];
        String contexto;
                
        if (nombre.length() > 100) {
            String message = LanguageHandler.getText(language, "crud.not-valid-name").replace("%entity%", "Division").replace("%name%", nombre).replace("%reason%", "Máximo 100 caracteres.");
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        if (nombrePublico.length() > 100) {
            String message = LanguageHandler.getText(language, "crud.not-valid-name").replace("%entity%", "Division").replace("%name%", nombrePublico).replace("%reason%", "Máximo 100 caracteres.");
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        if (tipoDivision.length() > 100) {
            String message = LanguageHandler.getText(language, "crud.not-valid-type").replace("%entity%", "Division").replace("%type%", tipoDivision).replace("%reason%", "Máximo 50 caracteres.");
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        Long paisId;
        try {
            paisId = Long.parseLong(args[3]);
        } catch (NumberFormatException ex) {
            String message = LanguageHandler.getText(language, "crud.not-valid-id").replace("%entity%", "Division").replace("%id%", args[3]);
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        if (!dbManager.exists(Pais.class, paisId)) {
            String message = LanguageHandler.getText(language, "crud.read-not-found").replace("%entity%", "Division").replace("%id%", args[3]);
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        StringBuilder contextoBuilder = new StringBuilder();
        for (int i = 4; i < args.length; i++) {
            if (i > 4) contextoBuilder.append(" ");
            contextoBuilder.append(args[i]);
        }
        contexto = contextoBuilder.toString();

        if (contexto.length() > 100) {
            PlayerLogger.error(sender, LanguageHandler.getText(language, "invalid-project-description"), (String) null);
            return true;
        }

        Pais pais = dbManager.get(Pais.class, paisId);
        Division division = new Division(pais, nombrePublico, nombre, tipoDivision, tipoDivision + " " + nombre , contexto);
        dbManager.save(division);

        String message = LanguageHandler.getText(language, "crud.create").replace("%entity%", "Division");
        PlayerLogger.info(sender, message, (String) null);
        return true;
    }
    
}
