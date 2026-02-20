package com.bteconosur.core.command.crud.tipousuario;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.DBManager;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.TipoUsuario;

public class CTipoUsuarioCommand extends BaseCommand {

    private final DBManager dbManager;

    public CTipoUsuarioCommand() {
        super("create", "Crear un nuevo Tipo de Usuario.", "<nombre> <cant_proyec_sim> <descripcion>", CommandMode.BOTH);
        dbManager = DBManager.getInstance();
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player commandPlayer = Player.getBTECSPlayer((org.bukkit.entity.Player) sender);
        Language language = commandPlayer.getLanguage();
        if (args.length < 3) {
            String message = LanguageHandler.getText(language, "help-command-usage").replace("%comando%", getFullCommand().replace(" " + command, ""));
            PlayerLogger.info(sender, message, (String) null);
            return true;
        }

        String nombre = args[0];
        Integer cantProyecSim;
        
        if (nombre.length() > 20) {
            String message = LanguageHandler.getText(language, "crud.not-valid-name").replace("%entity%", "Tipo de Usuario").replace("%name%", nombre).replace("%reason%", "Máximo 20 caracteres.");
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        try {
            cantProyecSim = Integer.parseInt(args[1]);
        } catch (NumberFormatException ex) {
            String message = LanguageHandler.getText(language, "crud.not-valid-parse").replace("%entity%", "cant_proyec_sim").replace("%value%", args[1]).replace("%type%", "Integer");
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        StringBuilder descripcionBuilder = new StringBuilder();
        for (int i = 2; i < args.length; i++) {
            if (i > 2) descripcionBuilder.append(" ");
            descripcionBuilder.append(args[i]);
        }
        String descripcion = descripcionBuilder.toString();

        if (descripcion.length() > 500) {
            String message = LanguageHandler.getText(language, "crud.not-valid-description").replace("%entity%", "Tipo de Usuario").replace("%name%", descripcion).replace("%reason%", "Máximo 500 caracteres.");
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        TipoUsuario tipoUsuario = new TipoUsuario(nombre, descripcion, cantProyecSim);
        dbManager.save(tipoUsuario);

        String message = LanguageHandler.getText(language, "crud.create").replace("%entity%", "Tipo de Usuario");
        PlayerLogger.info(sender, message, (String) null);
        return true;
    }
    
}
