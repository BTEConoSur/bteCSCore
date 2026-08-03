package com.bteconosur.core.command.btecs.defaultgroup;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.PermissionManager;
import com.bteconosur.db.model.Player;

public class BTECSDefaultListPermisosCommand extends BaseCommand {

    public BTECSDefaultListPermisosCommand() {
        super("listpermisos", null, "btecs.command.btecs.defaultgroup", CommandMode.BOTH);
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player commandPlayer = null;
        if (sender instanceof org.bukkit.entity.Player) commandPlayer = Player.getBTECSPlayer((org.bukkit.entity.Player) sender);
        Language language = commandPlayer != null ? commandPlayer.getLanguage() : Language.getDefault();

        if (args.length != 0) {
            String message = LanguageHandler.getText(language, "help-command-usage").replace("%comando%", getFullCommand());
            PlayerLogger.info(sender, message, (String) null);
            return true;
        }

        List<String> permisos = PermissionManager.getInstance().getDefaultPermissions();
        if (permisos.isEmpty()) {
            String emptyMsg = LanguageHandler.getText(language, "get-list.empty").replace("%entity%", "permisos");
            PlayerLogger.warn(sender, emptyMsg, (String) null);
            return true;
        }

        String lista = permisos.stream().collect(Collectors.joining(", "));
        String message = LanguageHandler.getText(language, "crud.permisos-list").replace("%permisos%", lista);
        PlayerLogger.info(sender, message, (String) null);
        return true;
    }
}