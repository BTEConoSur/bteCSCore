package com.bteconosur.core.command.manager;

import java.util.Set;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.ProjectManager;
import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.menu.player.PlayerListMenu;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.PermissionManager;
import com.bteconosur.db.model.Pais;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.Proyecto;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.db.registry.ProyectoRegistry;

public class ManagerAddMemberCommand extends BaseCommand {

    public ManagerAddMemberCommand() {
        super("addmember", "Agregar a un miembro a un proyecto del país.", "<id_proyecto> [nombre_jugador]", CommandMode.PLAYER_ONLY);
        this.addSubcommand(new GenericHelpCommand(this));
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player commandPlayer = PlayerRegistry.getInstance().get(sender);
        Language language = commandPlayer.getLanguage();
        if (args.length > 2 || args.length < 1) {
            String message = LanguageHandler.getText(language, "help-command-usage").replace("%comando%", getFullCommand());
            PlayerLogger.info(commandPlayer, message, (String) null);
            return true;
        }

        PermissionManager permissionManager = PermissionManager.getInstance();

        ProyectoRegistry proyectoRegistry = ProyectoRegistry.getInstance();
        String proyectoId = args[0];
        Proyecto targetProyecto = proyectoRegistry.get(proyectoId);
        if (targetProyecto == null) {
            String message = LanguageHandler.replaceMC("project.not-found-id", language, targetProyecto);
            PlayerLogger.error(commandPlayer, message, (String) null);
            return true;
        }

        Pais pais = targetProyecto.getPais();
        if (!permissionManager.isManager(commandPlayer, pais)) {
            String message = LanguageHandler.replaceMC("manager.not-manager-country", language, pais);
            PlayerLogger.error(commandPlayer, message, (String) null);
            return true;
        }

        ProjectManager projectManager = ProjectManager.getInstance();
        Player targetPlayer = commandPlayer;
        if (args.length == 2) {
            PlayerRegistry playerRegistry = PlayerRegistry.getInstance();
            targetPlayer = playerRegistry.findByName(args[1]);
            if (targetPlayer == null) {
                String message = LanguageHandler.getText(language, "player-not-found").replace("%player%", args[1]);
                PlayerLogger.warn(commandPlayer, message, (String) null);
                return true;
            }
        } else {
            Player lider = projectManager.getLider(targetProyecto);
            Set<Player> miembros = projectManager.getMembers(targetProyecto);
            miembros.add(lider);
            String title = LanguageHandler.replaceMC("gui-titles.select-player-add", language, targetProyecto);
            PlayerListMenu playerListMenu = new PlayerListMenu(commandPlayer, title, miembros, true, (player, event) -> {
                if (permissionManager.isMiembroOrLider(player, targetProyecto)) {
                    String message = LanguageHandler.replaceMC("project.member.already", language, player, targetProyecto);
                    PlayerLogger.error(commandPlayer, message, (String) null);
                    event.getWhoClicked().closeInventory();
                    return;
                }
                ProjectManager.getInstance().joinProject(targetProyecto.getId(), player.getUuid(), commandPlayer.getUuid(), true);
                String successMessage = LanguageHandler.replaceMC("project.member.add.success", language, player, targetProyecto);
                PlayerLogger.info(commandPlayer, successMessage, (String) null);
                event.getWhoClicked().closeInventory();
            });
            playerListMenu.open();
            return true;
        }   
        if (permissionManager.isMiembroOrLider(targetPlayer, targetProyecto)) {
            String message = LanguageHandler.replaceMC("project.member.already", language, targetPlayer, targetProyecto);
            PlayerLogger.error(commandPlayer, message, (String) null);
            return true;
        }
        projectManager.joinProject(targetProyecto.getId(), targetPlayer.getUuid(), commandPlayer.getUuid(), true);
        String successMessage = LanguageHandler.replaceMC("project.member.add.success", language, targetPlayer, targetProyecto);   
        PlayerLogger.info(commandPlayer, successMessage, (String) null);

        return true;
    }

}
