package com.bteconosur.core.command.project;

import java.util.Set;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.ProjectManager;
import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.menu.player.PlayerListMenu;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.PermissionManager;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.Proyecto;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.db.registry.ProyectoRegistry;
import com.bteconosur.db.util.Estado;

public class ProjectAddMemberCommand extends BaseCommand {

    private final YamlConfiguration lang;
    private final YamlConfiguration config;

    public ProjectAddMemberCommand() {
        super("addmember", "Agregar a un miembro a un proyecto.", "<id_proyecto> [nombre_jugador]", CommandMode.PLAYER_ONLY);
        this.addSubcommand(new GenericHelpCommand(this));
        ConfigHandler configHandler = ConfigHandler.getInstance();
        lang = configHandler.getLang();
        config = configHandler.getConfig();
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
            String message = lang.getString("not-a-active-editing-projec").replace("%proyectoId%", targetProyecto.getId());
            PlayerLogger.warn(commandPlayer, message, (String) null);   
            return true;
        }

        if (!targetProyecto.checkMaxMiembros()) {
            String message = lang.getString("max-members-reached").replace("%proyectoId%", targetProyecto.getId())
                .replace("%max%", String.valueOf(targetProyecto.getTipoProyecto().getMaxMiembros()))
                .replace("%current%", String.valueOf(targetProyecto.getCantMiembros()));
            PlayerLogger.error(commandPlayer, message, (String) null);   
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
            Player lider = projectManager.getLider(targetProyecto);
            Set<Player> miembros = projectManager.getMembers(targetProyecto);
            miembros.add(lider);
            PlayerListMenu playerListMenu = new PlayerListMenu(commandPlayer, lang.getString("gui-titles.select-member").replace("%proyectoId%", targetProyecto.getId()), miembros, true, (player, event) -> {
                if (permissionManager.isMiembroOrLider(player, targetProyecto)) {
                    String message = lang.getString("project-already-member").replace("%player%", player.getNombre()).replace("%proyectoId%", targetProyecto.getId());   
                    PlayerLogger.error(commandPlayer, message, (String) null);
                    event.getWhoClicked().closeInventory();
                    return;
                }

                if (permissionManager.isPostulante(player)) {
                    PlayerLogger.error(commandPlayer, lang.getString("cant-add-postulante"), (String) null);
                    event.getWhoClicked().closeInventory();
                    return;
                }

                int maxMembers = config.getInt("max-members-for-postulantes");
                if (permissionManager.isPostulante(commandPlayer) && targetProyecto.getCantMiembros() >= maxMembers) {
                    String message = lang.getString("postulante-cant-add-member").replace("%max%", String.valueOf(maxMembers));
                    PlayerLogger.error(commandPlayer, message, (String) null);
                    event.getWhoClicked().closeInventory();
                    return;
                }
                ProjectManager.getInstance().joinProject(targetProyecto.getId(), player.getUuid(), commandPlayer.getUuid(), true);
                String successMessage = lang.getString("project-add-member-success").replace("%player%", player.getNombre()).replace("%proyectoId%", targetProyecto.getId());   
                PlayerLogger.info(commandPlayer, successMessage, (String) null);
                event.getWhoClicked().closeInventory();
            });
            playerListMenu.open();
            return true;
        }   
        if (permissionManager.isMiembroOrLider(targetPlayer, targetProyecto)) {
            String message = lang.getString("project-already-member").replace("%player%", targetPlayer.getNombre()).replace("%proyectoId%", targetProyecto.getId());   
            PlayerLogger.error(commandPlayer, message, (String) null);
            return true;
        }

        if (permissionManager.isPostulante(targetPlayer)) {
            PlayerLogger.error(commandPlayer, lang.getString("cant-add-postulante"), (String) null);   
            return true;
        }

        int maxMembers = config.getInt("max-members-for-postulantes");
        if (permissionManager.isPostulante(commandPlayer) && targetProyecto.getCantMiembros() >= maxMembers) {
            String message = lang.getString("postulante-cant-add-member").replace("%max%", String.valueOf(maxMembers));
            PlayerLogger.error(commandPlayer, message, (String) null);   
            return true;
        }

        projectManager.joinProject(targetProyecto.getId(), targetPlayer.getUuid(), commandPlayer.getUuid(), true);
        String successMessage = lang.getString("project-add-member-success").replace("%player%", targetPlayer.getNombre()).replace("%proyectoId%", targetProyecto.getId());   
        PlayerLogger.info(commandPlayer, successMessage, (String) null);

        return true;
    }

}
