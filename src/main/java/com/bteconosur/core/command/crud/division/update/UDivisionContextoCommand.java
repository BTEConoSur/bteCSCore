package com.bteconosur.core.command.crud.division.update;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.DBManager;
import com.bteconosur.db.model.Division;

public class UDivisionContextoCommand extends BaseCommand {

    private final YamlConfiguration lang;
    private final DBManager dbManager;

    public UDivisionContextoCommand() {
        super("contexto", "Actualizar contexto de una Division.", "<id> <nuevo_contexto>", CommandMode.BOTH);
        ConfigHandler configHandler = ConfigHandler.getInstance();
        lang = configHandler.getLang();
        dbManager = DBManager.getInstance();
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            String message = lang.getString("help-command-usage").replace("%command%", getFullCommand().replace(" " + command, ""));
            PlayerLogger.info(sender, message, (String) null);
            return true;
        }

        Long id;
        try {
            id = Long.parseLong(args[0]);
        } catch (NumberFormatException ex) {
            String message = lang.getString("crud-not-valid-id").replace("%entity%", "Division").replace("%id%", args[0]);
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        if (!dbManager.exists(Division.class, id)) {
            String message = lang.getString("crud-read-not-found").replace("%entity%", "Division").replace("%id%", args[0]);
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        StringBuilder contextoBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            if (i > 1) contextoBuilder.append(" ");
            contextoBuilder.append(args[i]);
        }
        String nuevoContexto = contextoBuilder.toString();

        if (nuevoContexto.length() > 100) {
            PlayerLogger.error(sender, lang.getString("invalid-project-description"), (String) null);
            return true;
        }

        Division division = dbManager.get(Division.class, id);
        division.setContexto(nuevoContexto);
        dbManager.merge(division);

        String message = lang.getString("crud-update").replace("%entity%", "Division").replace("%id%", args[0]);
        PlayerLogger.info(sender, message, (String) null);
        return true;
    }

}
