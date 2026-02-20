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
import com.bteconosur.core.menu.project.ProjectListMenu;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.PermissionManager;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.Proyecto;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.db.registry.ProyectoRegistry;
import com.bteconosur.db.util.Estado;

public class ProjectJoinCommand extends BaseCommand {

    private final YamlConfiguration config;
    private ProjectListMenu projectListMenu;

    public ProjectJoinCommand() { // Postulantes no pueden usar este comando.
        super("join", "Unirse a un proyecto Activo o en Edición.", "[id_proyecto]", CommandMode.PLAYER_ONLY);
        this.addSubcommand(new GenericHelpCommand(this));
        ConfigHandler configHandler = ConfigHandler.getInstance();
        config = configHandler.getConfig();
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
        org.bukkit.entity.Player bukkitPlayer = (org.bukkit.entity.Player) sender;
        PermissionManager permissionManager = PermissionManager.getInstance();
        ProjectManager projectManager = ProjectManager.getInstance();
        ProyectoRegistry pr = ProyectoRegistry.getInstance();
        Proyecto proyectoFinal = null;

        if (args.length == 1) {
            String proyectoId = args[0];
            proyectoFinal = ProyectoRegistry.getInstance().get(proyectoId);
            if (proyectoFinal == null) {
                PlayerLogger.warn(commandPlayer, LanguageHandler.replaceMC("project.not-found-id", language, proyectoFinal), (String) null);   
                return true;
            }
        } else {
            Set<Proyecto> proyectos = pr.getByLocation(bukkitPlayer.getLocation().getBlockX(), bukkitPlayer.getLocation().getBlockZ());
            if (proyectos.isEmpty()) {
                PlayerLogger.warn(commandPlayer, LanguageHandler.getText(language, "project.not-found-here"), (String) null);
                return true;
            }
            Set<Proyecto> roomProyectos = pr.getWithRoom(proyectos);
            if (roomProyectos.isEmpty()) {
                PlayerLogger.warn(commandPlayer, LanguageHandler.getText(language, "project.not-with-room-here"), (String) null);
                return true;
            }
            Set<Proyecto> activeProyectos = pr.getActiveOrEditando(roomProyectos);
            if (activeProyectos.isEmpty()) {
                PlayerLogger.warn(commandPlayer, LanguageHandler.getText(language, "project.not-active-editing-here"), (String) null); //TODO: verificar casos en editando
                return true;
            }
            Set<Proyecto> notMemberProyectos = pr.getNotMemberOrLider(commandPlayer, activeProyectos);
            if (notMemberProyectos.isEmpty()) {
                PlayerLogger.warn(commandPlayer, LanguageHandler.getText(language, "project.member.already-here"), (String) null);
                return true;
            }

            if (notMemberProyectos.size() > 1) {
                projectListMenu = new ProjectListMenu(commandPlayer, LanguageHandler.getText(language, "gui-titles.proyectos-activos-list"), notMemberProyectos, (proyecto, event) -> {
                    if (permissionManager.isMiembroOrLider(commandPlayer, proyecto)) {
                        String message = LanguageHandler.replaceMC("project.member.already", language, proyecto);   
                        PlayerLogger.error(commandPlayer, message, (String) null);
                        event.getWhoClicked().closeInventory();
                        return;
                    }
                    if (proyecto.getEstado() != Estado.ACTIVO && proyecto.getEstado() != Estado.EDITANDO) {
                        String message = LanguageHandler.replaceMC("project.not-active-editing", language, proyecto);
                        PlayerLogger.error(commandPlayer, message, (String) null);
                        event.getWhoClicked().closeInventory();
                        return;
                    }
                    if (!proyecto.checkMaxMiembros()) {
                        String message = LanguageHandler.replaceMC("project.member.add.max-reached", language, proyecto)
                            .replace("%current%", String.valueOf(proyecto.getCantMiembros()));
                        PlayerLogger.error(commandPlayer, message, (String) null);
                        event.getWhoClicked().closeInventory();
                        return;
                    }

                    Player lider = projectManager.getLider(proyecto);
                    int maxMembers = config.getInt("max-members-for-postulantes");
                    if (permissionManager.isPostulante(lider) && proyecto.getCantMiembros() >= maxMembers) {
                        String message = LanguageHandler.getText(language, "project.member.add.postulante-max-reached").replace("%max%", String.valueOf(maxMembers));
                        PlayerLogger.error(commandPlayer, message, (String) null);
                        event.getWhoClicked().closeInventory();  
                        return;
                    }
                    projectManager.createJoinRequest(proyecto.getId(), commandPlayer.getUuid());
                    event.getWhoClicked().closeInventory();
                });
                projectListMenu.open();
                return true;
            }
            proyectoFinal = notMemberProyectos.iterator().next();
        }

        if (permissionManager.isMiembroOrLider(commandPlayer, proyectoFinal)) {
            String message = LanguageHandler.replaceMC("project.member.already", language, proyectoFinal);   
            PlayerLogger.error(commandPlayer, message, (String) null);
            return true;
        }

        if (proyectoFinal.getEstado() != Estado.ACTIVO && proyectoFinal.getEstado() != Estado.EDITANDO) {
            String message = LanguageHandler.replaceMC("project.not-active-editing", language, proyectoFinal);
            PlayerLogger.error(commandPlayer, message, (String) null);   
            return true;
        }

        if (!proyectoFinal.checkMaxMiembros()) {
            String message = LanguageHandler.replaceMC("project.member.add.max-reached", language, proyectoFinal)
                .replace("%current%", String.valueOf(proyectoFinal.getCantMiembros()));
            PlayerLogger.error(commandPlayer, message, (String) null);   
            return true;
        }

        Player lider = projectManager.getLider(proyectoFinal);
        int maxMembers = config.getInt("max-members-for-postulantes");
        if (permissionManager.isPostulante(lider) && proyectoFinal.getCantMiembros() >= maxMembers) {
            String message = LanguageHandler.getText(language, "project.member.add.postulante-max-reached").replace("%max%", String.valueOf(maxMembers));
            PlayerLogger.error(commandPlayer, message, (String) null);   
            return true;
        }

        projectManager.createJoinRequest(proyectoFinal.getId(), commandPlayer.getUuid());
        return true;
    }

}
