package com.bteconosur.core.command.btecs.defaultgroup;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.PermissionManager;
import com.bteconosur.db.model.Player;

public class BTECSDefaultAddPermisoCommand extends BaseCommand {

    public BTECSDefaultAddPermisoCommand() {
        super("addpermiso", "<permiso>", "btecs.command.btecs.defaultgroup", CommandMode.BOTH);
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player commandPlayer = null;
        if (sender instanceof org.bukkit.entity.Player) commandPlayer = Player.getBTECSPlayer((org.bukkit.entity.Player) sender);
        Language language = commandPlayer != null ? commandPlayer.getLanguage() : Language.getDefault();

        if (args.length != 1) {
            String message = LanguageHandler.getText(language, "help-command-usage").replace("%comando%", getFullCommand().replace(" " + command, ""));
            PlayerLogger.info(sender, message, (String) null);
            return true;
        }

        String permisoNombre = args[0];
        if (permisoNombre.length() > 100) {
            String message = LanguageHandler.getText(language, "crud.not-valid-name").replace("%entity%", "Nodo Permiso").replace("%name%", permisoNombre).replace("%reason%", "Máximo 100 caracteres.");
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        PermissionManager pm = PermissionManager.getInstance();
        if (pm.hasPermissionInDefault(permisoNombre)) {
            String message = LanguageHandler.getText(language, "lp-default-permission-already").replace("%permiso%", permisoNombre);
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        boolean modified = pm.addPermissionToDefault(permisoNombre);
        if (!modified) {
            String message = LanguageHandler.getText(language, "lp-default-permission-add-failed").replace("%permiso%", permisoNombre);
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        String message = LanguageHandler.getText(language, "lp-default-permission-added").replace("%permiso%", permisoNombre);
        PlayerLogger.info(sender, message, (String) null);
        return true;
    }
}