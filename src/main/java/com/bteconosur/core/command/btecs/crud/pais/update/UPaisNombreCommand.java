package com.bteconosur.core.command.btecs.crud.pais.update;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.db.DBManager;
import com.bteconosur.db.model.Pais;

public class UPaisNombreCommand extends BaseCommand {

    private final YamlConfiguration lang;
    private final DBManager dbManager;

    public UPaisNombreCommand() {
        super("nombre", "Actualizar nombre de un País.", "<id> <nuevo_nombre>", CommandMode.BOTH);
        ConfigHandler configHandler = ConfigHandler.getInstance();
        lang = configHandler.getLang();
        dbManager = DBManager.getInstance();
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        if (args.length != 2) {
            String message = lang.getString("help-command-usage").replace("%command%", getFullCommand().replace(" " + command, ""));
            sender.sendMessage(message);
            return true;
        }

        Long id;
        try {
            id = Long.parseLong(args[0]);
        } catch (NumberFormatException ex) {
            String message = lang.getString("crud-not-valid-id").replace("%entity%", "Pais").replace("%id%", args[0]);
            sender.sendMessage(message);
            return true;
        }

        if (!dbManager.exists(Pais.class, id)) {
            String message = lang.getString("crud-read-not-found").replace("%entity%", "Pais").replace("%id%", args[0]);
            sender.sendMessage(message);
            return true;
        }

        String nuevoNombre = args[1];
        if (nuevoNombre.length() > 50) {
            String message = lang.getString("crud-not-valid-name").replace("%entity%", "Pais").replace("%name%", nuevoNombre).replace("%reason%", "Máximo 50 caracteres.");
            sender.sendMessage(message);
            return true;
        }

        Pais pais = dbManager.get(Pais.class, id);
        pais.setNombre(nuevoNombre);
        dbManager.merge(pais);

        String message = lang.getString("crud-update").replace("%entity%", "Pais").replace("%property%", "nombre");
        sender.sendMessage(message);
        return true;
    }
}
