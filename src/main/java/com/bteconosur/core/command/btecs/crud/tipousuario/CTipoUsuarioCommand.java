package com.bteconosur.core.command.btecs.crud.tipousuario;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.db.DBManager;
import com.bteconosur.db.model.TipoUsuario;

public class CTipoUsuarioCommand extends BaseCommand {

    private final YamlConfiguration lang;
    private final DBManager dbManager;

    public CTipoUsuarioCommand() {
        super("create", "Crear un nuevo TipoUsuario.", "<nombre> <cant_proyec_sim> <descripcion...>", CommandMode.BOTH);

        ConfigHandler configHandler = ConfigHandler.getInstance();
        lang = configHandler.getLang();
        dbManager = DBManager.getInstance();
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        if (args.length < 3) {
            String message = lang.getString("help-command-usage").replace("%command%", getFullCommand().replace(" " + command, ""));
            sender.sendMessage(message);
            return true;
        }

        String nombre = args[0];
        Integer cantProyecSim;
        
        if (nombre.length() > 20) {
            String message = lang.getString("crud-not-valid-name").replace("%entity%", "TipoUsuario").replace("%name%", nombre).replace("%reason%", "Máximo 20 caracteres.");
            sender.sendMessage(message);
            return true;
        }

        try {
            cantProyecSim = Integer.parseInt(args[1]);
        } catch (NumberFormatException ex) {
            String message = lang.getString("crud-not-valid-parse").replace("%entity%", "cant_proyec_sim").replace("%value%", args[1]).replace("%type%", "Integer");
            sender.sendMessage(message);
            return true;
        }

        StringBuilder descripcionBuilder = new StringBuilder();
        for (int i = 2; i < args.length; i++) {
            if (i > 2) descripcionBuilder.append(" ");
            descripcionBuilder.append(args[i]);
        }
        String descripcion = descripcionBuilder.toString();

        if (descripcion.length() > 500) {
            String message = lang.getString("crud-not-valid-name").replace("%entity%", "TipoUsuario").replace("%name%", descripcion).replace("%reason%", "Máximo 500 caracteres.");
            sender.sendMessage(message);
            return true;
        }

        TipoUsuario tipoUsuario = new TipoUsuario(nombre, descripcion, cantProyecSim);
        dbManager.save(tipoUsuario);

        String message = lang.getString("crud-create").replace("%entity%", "TipoUsuario");
        sender.sendMessage(message);
        return true;
    }
    
}
