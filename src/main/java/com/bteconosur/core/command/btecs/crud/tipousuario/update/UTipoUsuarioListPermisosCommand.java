package com.bteconosur.core.command.btecs.crud.tipousuario.update;

import java.util.stream.Collectors;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.db.DBManager;
import com.bteconosur.db.model.TipoUsuario;

public class UTipoUsuarioListPermisosCommand extends BaseCommand {

    private final YamlConfiguration lang;
    private final DBManager dbManager;

    public UTipoUsuarioListPermisosCommand() {
        super("listpermisos", "Listar permisos de un TipoUsuario.", "<id_tipo>", CommandMode.BOTH);
        ConfigHandler configHandler = ConfigHandler.getInstance();
        lang = configHandler.getLang();
        dbManager = DBManager.getInstance();
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
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

        TipoUsuario tipo = dbManager.get(TipoUsuario.class, id);
        if (tipo == null) {
            String message = lang.getString("crud-read-not-found").replace("%entity%", "TipoUsuario").replace("%id%", args[0]);
            sender.sendMessage(message);
            return true;
        }

        if (tipo.getPermisos().isEmpty()) {
            String emptyMsg = lang.getString("get-list-empty").replace("%entity%", "permisos");
            sender.sendMessage(emptyMsg);
            return true;
        }

        String lista = tipo.getPermisos().stream()
                .map(p -> p.getNombre())
                .sorted()
                .collect(Collectors.joining(", "));
        sender.sendMessage("Permisos: " + lista);
        return true;
    }
}
