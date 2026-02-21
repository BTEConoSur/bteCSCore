package com.bteconosur.core.command.project;

import java.util.Set;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.ProjectManager;
import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.menu.player.PlayerListMenu;
import com.bteconosur.core.util.MenuUtils;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.PermissionManager;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.Proyecto;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.db.registry.ProyectoRegistry;
import com.bteconosur.db.util.Estado;

public class ProjectTransferCommand extends BaseCommand {

    public ProjectTransferCommand() {
        super("transfer", "Transferir el liderazgo de un proyecto a un Miembro.", "<id_proyecto> [nombre_jugador]", CommandMode.PLAYER_ONLY);
        this.addSubcommand(new GenericHelpCommand(this));
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player commandPlayer = PlayerRegistry.getInstance().get(sender);
        Language language = commandPlayer.getLanguage();
        PermissionManager permissionManager = PermissionManager.getInstance();
        if (args.length > 2 || args.length < 1) {
            String message = LanguageHandler.getText(language, "help-command-usage").replace("%comando%", getFullCommand());
            PlayerLogger.info(commandPlayer, message, (String) null);
            return true;
        }

        ProyectoRegistry proyectoRegistry = ProyectoRegistry.getInstance();
        String proyectoId = args[0];
        Proyecto targetProyecto = proyectoRegistry.get(proyectoId);
        if (targetProyecto == null) {
            PlayerLogger.warn(commandPlayer, LanguageHandler.getText(language, "project.not-found-id").replace("%search%", args[0]), (String) null);   
            return true;
        }
        if (!permissionManager.isLider(commandPlayer, targetProyecto)) {
            PlayerLogger.error(commandPlayer, LanguageHandler.replaceMC("project.leader.not-leader", language, targetProyecto), (String) null);   
            return true;
        }

        if (targetProyecto.getEstado() != Estado.ACTIVO && targetProyecto.getEstado() != Estado.EDITANDO) {
            String message = LanguageHandler.replaceMC("project.not-active-editing", language, targetProyecto);
            PlayerLogger.warn(commandPlayer, message, (String) null);   
            return true;
        }

        ProjectManager projectManager = ProjectManager.getInstance();
        ProyectoRegistry pr = ProyectoRegistry.getInstance(); 
        Player targetPlayer = commandPlayer;
        if (args.length == 2) {
            PlayerRegistry playerRegistry = PlayerRegistry.getInstance();
            targetPlayer = playerRegistry.findByName(args[1]);
            if (targetPlayer == null) {
                String message = LanguageHandler.getText(language, "player-not-registered").replace("%player%", args[1]);
                PlayerLogger.warn(commandPlayer, message, (String) null);
                return true;
            }
        } else {
            Set<Player> miembros = projectManager.getMembers(targetProyecto);
            String title = LanguageHandler.replaceMC("gui-titles.select-leader", language, targetProyecto);
            PlayerListMenu playerListMenu = new PlayerListMenu(commandPlayer, title, miembros, false, MenuUtils.PlayerContext.MIEMBRO, (player, event) -> {
                event.getWhoClicked().closeInventory(); 
                if (permissionManager.isLider(player, targetProyecto)) {
                    String message = LanguageHandler.replaceMC("project.leader.other-already", language, targetProyecto);
                    PlayerLogger.error(commandPlayer, message, (String) null);
                    return;
                }
                if (!permissionManager.isMiembro(player, targetProyecto)) {
                    String message = LanguageHandler.replaceMC("project.member.not-member", language, player, targetProyecto);
                    PlayerLogger.error(commandPlayer, message, (String) null);
                    
                    return;
                }
                int activeProjects = pr.getCounts(player)[1];
                int maxActiveProjects = player.getTipoUsuario().getCantProyecSim();
                if (activeProjects >= maxActiveProjects) {
                    String message = LanguageHandler.replaceMC("project.leader.max-active-projects-transfer", language, player).replace("%maxProjects%", String.valueOf(maxActiveProjects)).replace("%currentProjects%", String.valueOf(activeProjects)).replace("%player%", player.getNombre());
                    PlayerLogger.error(commandPlayer, message, (String) null);
                    return;
                }
                ProjectManager.getInstance().switchLeader(targetProyecto.getId(), player.getUuid(), commandPlayer.getUuid());
                String successMessage = LanguageHandler.replaceMC("project.leader.switch.success", language, player, targetProyecto);
                PlayerLogger.info(commandPlayer, successMessage, (String) null);
            });
            playerListMenu.open();
            return true;
        }
        if (permissionManager.isLider(targetPlayer, targetProyecto)) {
            String message = LanguageHandler.replaceMC("project.leader.other-already", language, targetProyecto);
            PlayerLogger.error(commandPlayer, message, (String) null);
            return true;
        }
        if (!permissionManager.isMiembro(targetPlayer, targetProyecto)) {
            String message = LanguageHandler.replaceMC("project.member.not-member", language, targetPlayer, targetProyecto);
            PlayerLogger.error(commandPlayer, message, (String) null);   
            return true;
        }

        
        int activeProjects = pr.getCounts(targetPlayer)[1];
        int maxActiveProjects = targetPlayer.getTipoUsuario().getCantProyecSim();
        if (activeProjects >= maxActiveProjects) {
            String message = LanguageHandler.replaceMC("project.leader.max-active-projects-transfer", language, targetPlayer).replace("%maxProjects%", String.valueOf(maxActiveProjects)).replace("%currentProjects%", String.valueOf(activeProjects)).replace("%player%", targetPlayer.getNombre());
            PlayerLogger.error(commandPlayer, message, (String) null);
            return true;
        }

        projectManager.switchLeader(targetProyecto.getId(), targetPlayer.getUuid(), commandPlayer.getUuid());
        String successMessage = LanguageHandler.replaceMC("project.leader.switch.success", language, targetPlayer, targetProyecto);
        PlayerLogger.info(commandPlayer, successMessage, (String) null);

        return true;
    }

}
