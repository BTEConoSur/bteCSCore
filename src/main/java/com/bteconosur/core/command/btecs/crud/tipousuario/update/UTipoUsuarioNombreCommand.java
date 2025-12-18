package com.bteconosur.core.command.btecs.crud.tipousuario.update;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.db.DBManager;
import com.bteconosur.db.model.TipoUsuario;

public class UTipoUsuarioNombreCommand extends BaseCommand {

    private final YamlConfiguration lang;
    private final DBManager dbManager;

    public UTipoUsuarioNombreCommand() {
        super("nombre", "Actualizar nombre de un TipoUsuario.", "<id> <nuevo_nombre>", CommandMode.BOTH);
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
            String message = lang.getString("crud-not-valid-id").replace("%entity%", "TipoUsuario").replace("%id%", args[0]);
            sender.sendMessage(message);
            return true;
        }

        if (!dbManager.exists(TipoUsuario.class, id)) {
            String message = lang.getString("crud-read-not-found").replace("%entity%", "TipoUsuario").replace("%id%", args[0]);
            sender.sendMessage(message);
            return true;
        }

        String nuevoNombre = args[1];
        if (nuevoNombre.length() > 20) {
            String message = lang.getString("crud-not-valid-name").replace("%entity%", "TipoUsuario").replace("%name%", nuevoNombre).replace("%reason%", "Máximo 20 caracteres.");
            sender.sendMessage(message);
            return true;
        }

        TipoUsuario tipoUsuario = dbManager.get(TipoUsuario.class, id);
        tipoUsuario.setNombre(nuevoNombre);
        dbManager.merge(tipoUsuario);

        String message = lang.getString("crud-update").replace("%entity%", "TipoUsuario").replace("%property%", "nombre");
        sender.sendMessage(message);
        return true;
    }
}
