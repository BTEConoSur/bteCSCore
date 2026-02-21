package com.bteconosur.core.command.manager;

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
import com.bteconosur.db.model.Pais;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.Proyecto;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.db.registry.ProyectoRegistry;
import com.bteconosur.db.util.Estado;

public class ManagerTransferCommand extends BaseCommand {

    public ManagerTransferCommand() {
        super("transfer", "Transferir el liderazgo de un proyecto del país.", "<id_proyecto> [nombre_jugador]", CommandMode.PLAYER_ONLY);
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
            PlayerLogger.warn(commandPlayer, LanguageHandler.getText(language, "project.not-found-id").replace("%search%", args[0]), (String) null);   
            return true;
        }

        Pais pais = targetProyecto.getPais();
        if (!permissionManager.isManager(commandPlayer, pais)) {
            PlayerLogger.error(commandPlayer, LanguageHandler.replaceMC("manager.not-manager-country", language, pais), (String) null);   
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
            if (targetProyecto.getEstado() == Estado.ABANDONADO) {
                String title =LanguageHandler.replaceMC("gui-titles.select-leader", language, targetProyecto);
                PlayerListMenu playerListMenu = new PlayerListMenu(commandPlayer, title, (player, clickEvent) -> {
                    clickEvent.getWhoClicked().closeInventory();
                    ProjectManager.getInstance().switchLeader(targetProyecto.getId(), player.getUuid(), commandPlayer.getUuid());
                    String successMessage = LanguageHandler.replaceMC("project.leader.switch.success", language, player, targetProyecto);   
                    PlayerLogger.info(commandPlayer, successMessage, (String) null);
                });
                playerListMenu.open();
                return true;
            }
            Set<Player> miembros = projectManager.getMembers(targetProyecto);
            String title = LanguageHandler.replaceMC("gui-titles.select-member", language, targetProyecto);
            PlayerListMenu playerListMenu = new PlayerListMenu(commandPlayer, title, miembros, false, MenuUtils.PlayerContext.MIEMBRO, (player, event) -> {
                if (permissionManager.isLider(player, targetProyecto)) {
                    String message = LanguageHandler.replaceMC("project.leader.other-already", language, player, targetProyecto);
                    PlayerLogger.error(commandPlayer, message, (String) null);
                    event.getWhoClicked().closeInventory();
                    return;
                }
                if (!permissionManager.isMiembro(player, targetProyecto)) {
                    String message = LanguageHandler.replaceMC("project.member.not-member", language, player, targetProyecto);
                    PlayerLogger.error(commandPlayer, message, (String) null);
                    event.getWhoClicked().closeInventory(); 
                    return;
                }
                ProjectManager.getInstance().switchLeader(proyectoId, player.getUuid(), commandPlayer.getUuid());
                String successMessage = LanguageHandler.replaceMC("project.leader.switch.success", language, player, targetProyecto);   
                PlayerLogger.info(commandPlayer, successMessage, (String) null);
                event.getWhoClicked().closeInventory();
            });
            playerListMenu.open();
            return true;
        }   
        if (permissionManager.isLider(targetPlayer, targetProyecto)) {
            String message = LanguageHandler.replaceMC("project.leader.other-already", language, targetPlayer, targetProyecto);
            PlayerLogger.error(commandPlayer, message, (String) null);
            return true;
        }
        if (!permissionManager.isMiembro(targetPlayer, targetProyecto)) {
            String message = LanguageHandler.replaceMC("project.member.not-member", language, targetPlayer, targetProyecto);
            PlayerLogger.error(commandPlayer, message, (String) null);   
            return true;
        }

        projectManager.switchLeader(proyectoId, targetPlayer.getUuid(), commandPlayer.getUuid());
        String successMessage = LanguageHandler.replaceMC("project.leader.switch.success", language, targetPlayer, targetProyecto);   
        PlayerLogger.info(commandPlayer, successMessage, (String) null);

        return true;
    }

}
