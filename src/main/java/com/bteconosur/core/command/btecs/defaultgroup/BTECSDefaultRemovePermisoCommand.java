package com.bteconosur.core.command.btecs.defaultgroup;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.PermissionManager;
import com.bteconosur.db.model.Player;

public class BTECSDefaultRemovePermisoCommand extends BaseCommand {

    public BTECSDefaultRemovePermisoCommand() {
        super("removepermiso", "<permiso>", "btecs.command.btecs.defaultgroup", CommandMode.BOTH);
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
        boolean modified = PermissionManager.getInstance().removePermissionFromDefault(permisoNombre);
        if (!modified) {
            String message = LanguageHandler.getText(language, "lp-default-permission-not-found").replace("%permiso%", permisoNombre);
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        String message = LanguageHandler.getText(language, "lp-default-permission-removed").replace("%permiso%", permisoNombre);
        PlayerLogger.info(sender, message, (String) null);
        return true;
    }
}