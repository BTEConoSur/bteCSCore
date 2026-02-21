package com.bteconosur.core.command.project;

import java.util.Set;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.ProjectManager;
import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.menu.player.PlayerListMenu;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.PermissionManager;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.Proyecto;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.db.registry.ProyectoRegistry;
import com.bteconosur.db.util.Estado;

public class ProjectAddMemberCommand extends BaseCommand {

    private final YamlConfiguration config;

    public ProjectAddMemberCommand() {
        super("addmember", "Agregar a un miembro a un proyecto.", "<id_proyecto> [nombre_jugador]", CommandMode.PLAYER_ONLY);
        this.addSubcommand(new GenericHelpCommand(this));
        ConfigHandler configHandler = ConfigHandler.getInstance();
        config = configHandler.getConfig();
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player commandPlayer = PlayerRegistry.getInstance().get(sender);
        Language language = commandPlayer.getLanguage();
        PermissionManager permissionManager = PermissionManager.getInstance();
        if (args.length > 2 || args.length < 1) {
            String message = LanguageHandler.getText(language, "help-command-usage").replace("%comando%", getFullCommand().replace(" " + command, ""));
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

        if (!targetProyecto.checkMaxMiembros()) {
            String message = LanguageHandler.replaceMC("project.member.add.max-reached", language, targetProyecto)
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
                String message = LanguageHandler.getText(language, "player-not-registered").replace("%player%", args[1]);
                PlayerLogger.warn(commandPlayer, message, (String) null);
                return true;
            }
        } else {
            Player lider = projectManager.getLider(targetProyecto);
            Set<Player> miembros = projectManager.getMembers(targetProyecto);
            miembros.add(lider);
            String title = LanguageHandler.replaceMC("gui-titles.select-member", language, targetProyecto);
            PlayerListMenu playerListMenu = new PlayerListMenu(commandPlayer, title, miembros, true, (player, event) -> {
                if (permissionManager.isMiembroOrLider(player, targetProyecto)) {
                    String message = LanguageHandler.replaceMC("project.member.already", language, player, targetProyecto);   
                    PlayerLogger.error(commandPlayer, message, (String) null);
                    event.getWhoClicked().closeInventory();
                    return;
                }

                if (permissionManager.isPostulante(player)) {
                    PlayerLogger.error(commandPlayer, LanguageHandler.getText(language, "project.member.add.cant-add-postulante"), (String) null);
                    event.getWhoClicked().closeInventory();
                    return;
                }

                int maxMembers = config.getInt("max-members-for-postulantes");
                if (permissionManager.isPostulante(commandPlayer) && targetProyecto.getCantMiembros() >= maxMembers) {
                    String message = LanguageHandler.getText(language, "project.member.add.postulante-max-reached").replace("%max%", String.valueOf(maxMembers));
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

        if (permissionManager.isPostulante(targetPlayer)) {
            PlayerLogger.error(commandPlayer, LanguageHandler.getText(language, "project.member.add.cant-add-postulante"), (String) null);   
            return true;
        }

        int maxMembers = config.getInt("max-members-for-postulantes");
        if (permissionManager.isPostulante(commandPlayer) && targetProyecto.getCantMiembros() >= maxMembers) {
            String message = LanguageHandler.getText(language, "project.member.add.postulante-max-reached").replace("%max%", String.valueOf(maxMembers));
            PlayerLogger.error(commandPlayer, message, (String) null);   
            return true;
        }

        projectManager.joinProject(targetProyecto.getId(), targetPlayer.getUuid(), commandPlayer.getUuid(), true);
        String successMessage = LanguageHandler.replaceMC("project.member.add.success", language, targetPlayer, targetProyecto);   
        PlayerLogger.info(commandPlayer, successMessage, (String) null);

        return true;
    }

}
