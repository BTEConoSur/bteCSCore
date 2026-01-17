package com.bteconosur.core.command.btecs.crud.player.update;

import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.DBManager;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.TipoUsuario;

public class UPlayerTipoUsuarioCommand extends BaseCommand {

    private final YamlConfiguration lang;
    private final DBManager dbManager;

    public UPlayerTipoUsuarioCommand() {
        super("tipousuario", "Actualizar tipo de usuario de un Player.", "<uuid> <id_tipo_usuario>", CommandMode.BOTH);
        ConfigHandler configHandler = ConfigHandler.getInstance();
        lang = configHandler.getLang();
        dbManager = DBManager.getInstance();
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        if (args.length != 2) {
            String message = lang.getString("help-command-usage").replace("%command%", getFullCommand().replace(" " + command, ""));
            PlayerLogger.info(sender, message, (String) null);
            return true;
        }

        UUID uuid;
        long tipoId;
        try {
            uuid = UUID.fromString(args[0]);
        } catch (IllegalArgumentException ex) {
            String message = lang.getString("crud-not-valid-id").replace("%entity%", "Player").replace("%id%", args[0]);
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        try {
            tipoId = Long.parseLong(args[1]);
        } catch (NumberFormatException ex) {
            String message = lang.getString("crud-not-valid-parse").replace("%entity%", "TipoUsuario").replace("%value%", args[1]).replace("%type%", "Long");
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        if (!dbManager.exists(Player.class, uuid)) {
            String message = lang.getString("crud-read-not-found").replace("%entity%", "Player").replace("%id%", args[0]);
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        if (!dbManager.exists(TipoUsuario.class, tipoId)) {
            String message = lang.getString("crud-read-not-found").replace("%entity%", "TipoUsuario").replace("%id%", args[1]);
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        Player player = dbManager.get(Player.class, uuid);
        TipoUsuario tipo = dbManager.get(TipoUsuario.class, tipoId);
        player.setTipoUsuario(tipo);
        dbManager.merge(player);

        String message = lang.getString("crud-update").replace("%entity%", "Player").replace("%id%", args[0]);
        PlayerLogger.info(sender, message, (String) null, player);
        return true;
    }
}
