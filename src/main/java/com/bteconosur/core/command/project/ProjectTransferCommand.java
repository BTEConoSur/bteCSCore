package com.bteconosur.core.command.project;

import java.util.Set;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.ProjectManager;
import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.config.ConfigHandler;
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

    private final YamlConfiguration lang;

    public ProjectTransferCommand() {
        super("transfer", "Transferir el liderazgo de un proyecto a un Miembro.", "<id_proyecto> [nombre_jugador]", CommandMode.PLAYER_ONLY);
        this.addSubcommand(new GenericHelpCommand(this));
        ConfigHandler configHandler = ConfigHandler.getInstance();
        lang = configHandler.getLang();
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player commandPlayer = PlayerRegistry.getInstance().get(sender);
        PermissionManager permissionManager = PermissionManager.getInstance();
        if (args.length > 2 || args.length < 1) {
            String message = lang.getString("help-command-usage").replace("%command%", getFullCommand());
            PlayerLogger.info(commandPlayer, message, (String) null);
            return true;
        }

        ProyectoRegistry proyectoRegistry = ProyectoRegistry.getInstance();
        String proyectoId = args[0];
        Proyecto targetProyecto = proyectoRegistry.get(proyectoId);
        if (targetProyecto == null) {
            PlayerLogger.warn(commandPlayer, lang.getString("no-project-found-with-id").replace("%proyectoId%", proyectoId), (String) null);   
            return true;
        }
        if (!permissionManager.isLider(commandPlayer, targetProyecto)) {
            PlayerLogger.error(commandPlayer, lang.getString("not-a-leader-project").replace("%proyectoId%", targetProyecto.getId()), (String) null);   
            return true;
        }

        if (targetProyecto.getEstado() != Estado.ACTIVO && targetProyecto.getEstado() != Estado.EDITANDO) {
            String message = lang.getString("not-a-active-editing-project").replace("%proyectoId%", targetProyecto.getId());
            PlayerLogger.warn(commandPlayer, message, (String) null);   
            return true;
        }

        ProjectManager projectManager = ProjectManager.getInstance();
        
        Player targetPlayer = commandPlayer;
        if (args.length == 2) {
            PlayerRegistry playerRegistry = PlayerRegistry.getInstance();
            targetPlayer = playerRegistry.findByName(args[1]);
            if (targetPlayer == null) {
                String message = lang.getString("player-not-found").replace("%player%", args[1]);
                PlayerLogger.warn(commandPlayer, message, (String) null);
                return true;
            }
        } else {
            Set<Player> miembros = projectManager.getMembers(targetProyecto);
            PlayerListMenu playerListMenu = new PlayerListMenu(commandPlayer, lang.getString("gui-titles.select-leader").replace("%proyectoId%", targetProyecto.getId()), miembros, false, MenuUtils.PlayerContext.MIEMBRO, (player, event) -> {
                if (permissionManager.isLider(player, targetProyecto)) {
                    String message = lang.getString("project-already-leader").replace("%player%", player.getNombre()).replace("%proyectoId%", targetProyecto.getId());
                    PlayerLogger.error(commandPlayer, message, (String) null);
                    event.getWhoClicked().closeInventory();
                    return;
                }
                if (!permissionManager.isMiembro(player, targetProyecto)) {
                    String message = lang.getString("project-not-member").replace("%player%", player.getNombre()).replace("%proyectoId%", targetProyecto.getId());
                    PlayerLogger.error(commandPlayer, message, (String) null);
                    event.getWhoClicked().closeInventory(); 
                    return;
                }
                ProjectManager.getInstance().switchLeader(proyectoId, player.getUuid(), commandPlayer.getUuid());
                String successMessage = lang.getString("project-leader-switched-success").replace("%player%", player.getNombre()).replace("%proyectoId%", targetProyecto.getId());   
                PlayerLogger.info(commandPlayer, successMessage, (String) null);
                event.getWhoClicked().closeInventory();
            });
            playerListMenu.open();
            return true;
        }   
        if (permissionManager.isLider(targetPlayer, targetProyecto)) {
            String message = lang.getString("project-already-leader").replace("%player%", targetPlayer.getNombre()).replace("%proyectoId%", targetProyecto.getId());
            PlayerLogger.error(commandPlayer, message, (String) null);
            return true;
        }
        if (!permissionManager.isMiembro(targetPlayer, targetProyecto)) {
            String message = lang.getString("project-not-member").replace("%player%", targetPlayer.getNombre()).replace("%proyectoId%", targetProyecto.getId());
            PlayerLogger.error(commandPlayer, message, (String) null);   
            return true;
        }

        projectManager.switchLeader(proyectoId, targetPlayer.getUuid(), commandPlayer.getUuid());
        String successMessage = lang.getString("project-leader-switched-success").replace("%player%", targetPlayer.getNombre()).replace("%proyectoId%", targetProyecto.getId());   
        PlayerLogger.info(commandPlayer, successMessage, (String) null);

        return true;
    }

}
