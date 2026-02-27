package com.bteconosur.core.command.project;

import java.util.LinkedHashSet;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.menu.project.ProjectInfoMenu;
import com.bteconosur.core.menu.project.ProjectListMenu;
import com.bteconosur.core.menu.project.ProjectManageMenu;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.PermissionManager;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.Proyecto;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.db.registry.ProyectoRegistry;

public class ProjectListCommand extends BaseCommand {

    private ProjectListMenu menu;

    public ProjectListCommand() {
        super("list", "[nombre_jugador]", "btecs.command.project.list", CommandMode.PLAYER_ONLY);
        this.addSubcommand(new GenericHelpCommand(this));
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player commandPlayer = PlayerRegistry.getInstance().get(sender);
        Language language = commandPlayer.getLanguage();

        if (args.length > 1) {
            String message = LanguageHandler.getText(language, "help-command-usage").replace("%comando%", getFullCommand());
            PlayerLogger.info(sender, message, (String) null);
            return true;
        }

        PlayerRegistry playerRegistry = PlayerRegistry.getInstance();
        Player targetPlayer = commandPlayer;
        if (args.length == 1) {
            targetPlayer = playerRegistry.findByName(args[0]);
            if (targetPlayer == null) {
                String message = LanguageHandler.getText(language, "player-not-found").replace("%player%", args[0]);
                PlayerLogger.error(commandPlayer, message, (String) null);
                return true;
            }
        }
        
        LinkedHashSet<Proyecto> proyectos = ProyectoRegistry.getInstance().getByPlayer(targetPlayer);
        PermissionManager pm = PermissionManager.getInstance();
        String title = LanguageHandler.replaceMC("gui-titles.proyectos-list", language, targetPlayer);
        menu = new ProjectListMenu(commandPlayer, title, proyectos, (proyecto, event) -> {
            if (pm.isMiembroOrLider(commandPlayer, proyecto)) {
                new ProjectManageMenu(commandPlayer, proyecto, menu, LanguageHandler.replaceMC("gui-titles.project-manage", language, proyecto)).open();
            } else {
                new ProjectInfoMenu(commandPlayer, proyecto, menu, LanguageHandler.replaceMC("gui-titles.project-info", language, proyecto)).open();
            }
        });
        menu.open();

        return true;
    }

}
