package com.bteconosur.core.command.manager;

import org.bukkit.command.CommandSender;
import org.locationtech.jts.geom.Polygon;

import com.bteconosur.core.ProjectManager;
import com.bteconosur.core.chat.ChatUtil;
import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.core.util.RegionUtils;
import com.bteconosur.db.PermissionManager;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PlayerRegistry;

public class ManagerCreateCommand extends BaseCommand {

     public ManagerCreateCommand() {
        super("create", "[nombre]", "btecs.command.project.create", CommandMode.PLAYER_ONLY);
        this.addSubcommand(new GenericHelpCommand(this));
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        String nombre = null;

        Player commandPlayer = PlayerRegistry.getInstance().get(sender);
        Language language = commandPlayer.getLanguage();

        if (args.length >= 1) {
            StringBuilder nombreBuilder = new StringBuilder();
            for (int i = 0; i < args.length; i++) {
                if (i > 0) nombreBuilder.append(" ");
                nombreBuilder.append(args[i]);
            }
            nombre = nombreBuilder.toString();
            if (nombre.length() > 50) {
                PlayerLogger.error(commandPlayer, LanguageHandler.getText(language, "invalid-project-name"), (String) null);
                return true;
            }
            if (nombre.matches(".*<[^>]+>.*")) {
                PlayerLogger.error(sender, LanguageHandler.getText(language, "invalid-regex"), (String) null);
                return true;
            }
            if (ChatUtil.hasBannedChars(nombre)) {
                PlayerLogger.error(sender, LanguageHandler.getText(language, "invalid-chars"), (String) null);
                return true;
            }
        }

        Polygon regionPolygon = RegionUtils.getPolygon(sender);
        if (regionPolygon == null) return true;
        ProjectManager.getInstance().createManagerProject(nombre, regionPolygon, commandPlayer, language);
        return true;
    }

    @Override
    protected boolean customPermissionCheck(CommandSender sender) {
        Player commandPlayer = PlayerRegistry.getInstance().get(((org.bukkit.entity.Player) sender).getUniqueId());
        return PermissionManager.getInstance().isManager(commandPlayer);
    }

}
