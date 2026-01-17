package com.bteconosur.core.command.btecs.crud.player;

import java.util.Date;
import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.DBManager;
import com.bteconosur.db.model.Configuration;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.RangoUsuario;
import com.bteconosur.db.model.TipoUsuario;

public class CPlayerCommand extends BaseCommand {

    private final YamlConfiguration lang;
    private final DBManager dbManager;

    public CPlayerCommand() {
        super("create", "Crear un nuevo Player. <fecha_ingreso> con formato UNIX ms.", "<uuid> <nombre> <fecha_ingreso> <id_tipo_usuario> <id_rango_usuario>", CommandMode.BOTH);

        ConfigHandler configHandler = ConfigHandler.getInstance();
        lang = configHandler.getLang();
        dbManager = DBManager.getInstance();
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        if (args.length != 5) {
            String message = lang.getString("help-command-usage").replace("%command%", getFullCommand().replace(" " + command, ""));
            PlayerLogger.info(sender, message, (String) null);
            return true;
        }

        UUID uuid;
        Long tipoId;
        Long rangoId;
        Long fechaIngreso;
    
        try{
            uuid = UUID.fromString(args[0]);
        } catch (IllegalArgumentException exception){
            String message = lang.getString("crud-not-valid-id").replace("%entity%", "Player").replace("%id%", args[0]);
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        try {
            tipoId = Long.parseLong(args[3]);
        } catch (NumberFormatException ex) {
            String message = lang.getString("crud-not-valid-parse").replace("%entity%", "TipoUsuario").replace("%value%", args[3]).replace("%type%", "Long");
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        try {
            rangoId = Long.parseLong(args[4]);
        } catch (NumberFormatException ex) {
            String message = lang.getString("crud-not-valid-parse").replace("%entity%", "RangoUsuario").replace("%value%", args[4]).replace("%type%", "Long");
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        try {
            fechaIngreso = Long.parseLong(args[2]);
        } catch (NumberFormatException ex) {
            String message = lang.getString("crud-not-valid-parse").replace("%entity%", "fecha_ingreso").replace("%value%", args[2]).replace("%type%", "Long");
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        if (args[1].length() > 16) {
            String message = lang.getString("crud-not-valid-name").replace("%entity%", "Player").replace("%name%", args[1]).replace("%reason%", "Máximo 16 caracteres.");
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        if(dbManager.exists(Player.class, uuid)) {
            String message = lang.getString("crud-already-exists").replace("%entity%", "Player").replace("%id%", args[0]);
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }
        
        if(!dbManager.exists(TipoUsuario.class, tipoId)) {
            String message = lang.getString("crud-read-not-found").replace("%entity%", "TipoUsuario").replace("%id%", args[3]);
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        if(!dbManager.exists(RangoUsuario.class, rangoId)) {
            String message = lang.getString("crud-read-not-found").replace("%entity%", "RangoUsuario").replace("%id%", args[4]);
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        TipoUsuario tipoUsuario = dbManager.get(TipoUsuario.class, tipoId);
        RangoUsuario rangoUsuario = dbManager.get(RangoUsuario.class, rangoId);

        Player player = new Player(uuid, args[1], new Date(fechaIngreso), tipoUsuario, rangoUsuario);
        player.setConfiguration(new Configuration(player));
        dbManager.save(player);

        String message = lang.getString("crud-create").replace("%entity%", "Player");
        PlayerLogger.info(sender, message, (String) null);
        return true;
    }
    
}
