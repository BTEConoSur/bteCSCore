package com.bteconosur.core.command.btecs.crud.tipousuario;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.BTEConoSur;
import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.db.DBManager;
import com.bteconosur.db.model.TipoUsuario;

public class DTipoUsuarioCommand extends BaseCommand {

    private final YamlConfiguration lang;
    private final DBManager dbManager;

    public DTipoUsuarioCommand() {
        super("delete", "Eliminar un TipoUsuario.", "<id>", CommandMode.BOTH);
        ConfigHandler configHandler = ConfigHandler.getInstance();
        lang = configHandler.getLang();
        dbManager = DBManager.getInstance();
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        // TODO: Enviar por sistema de notificaciones
        if (args.length != 1) {
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

        TipoUsuario tipoUsuario = dbManager.get(TipoUsuario.class, id);
        if (tipoUsuario != null) {
            dbManager.remove(tipoUsuario);
        }

        String message = lang.getString("crud-delete").replace("%entity%", "TipoUsuario").replace("%id%", args[0]);
        BTEConoSur.getConsoleLogger().debug(message, tipoUsuario);
        sender.sendMessage(message);
        return true;
    }
}
