package com.bteconosur.core.command.project;

import java.util.Set;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.ProjectManager;
import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.menu.ConfirmationMenu;
import com.bteconosur.core.menu.project.ProjectListMenu;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.PermissionManager;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.Proyecto;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.db.registry.ProyectoRegistry;
import com.bteconosur.db.util.Estado;

public class ProjectFinishCommand extends BaseCommand {

    private ConfirmationMenu confirmationMenu;
    private ProjectListMenu projectListMenu;

    public ProjectFinishCommand() {
        super("finish", "Finalizar un proyecto.", "[id_proyecto]", "btecs.command.project.finish", CommandMode.PLAYER_ONLY);
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
        org.bukkit.entity.Player bukkitPlayer = (org.bukkit.entity.Player) sender;
        PermissionManager permissionManager = PermissionManager.getInstance();
        Proyecto proyectoFinal = null;
        if (args.length == 1) {
            String proyectoId = args[0];
            proyectoFinal = ProyectoRegistry.getInstance().get(proyectoId);
            if (proyectoFinal == null) {
                PlayerLogger.warn(commandPlayer, LanguageHandler.replaceMC("project.not-found-id", language, proyectoFinal), (String) null);   
                return true;
            }
            if (!permissionManager.isLider(commandPlayer, proyectoFinal)) {
                PlayerLogger.error(commandPlayer, LanguageHandler.replaceMC("project.leader.not-leader", language, proyectoFinal), (String) null);   
                return true;
            }
        } else {
            Set<Proyecto> proyectos = ProyectoRegistry.getInstance().getByLocation(bukkitPlayer.getLocation().getBlockX(), bukkitPlayer.getLocation().getBlockZ());
            if (proyectos.isEmpty()) {
                PlayerLogger.warn(commandPlayer, LanguageHandler.getText(language, "project.not-found-here"), (String) null);
                return true;
            }
            Set<Proyecto> liderProyectos = ProyectoRegistry.getInstance().getByLider(commandPlayer, proyectos);
            if (liderProyectos.isEmpty()) {
                    PlayerLogger.warn(commandPlayer, LanguageHandler.getText(language, "project.leader.not-leader-here"), (String) null);
                    return true;
            }
            Set<Proyecto> activeProyectos = ProyectoRegistry.getInstance().getActiveOrEditando(liderProyectos);
            if (activeProyectos.isEmpty()) {
                PlayerLogger.warn(commandPlayer, LanguageHandler.getText(language, "project.leader.not-leader-active-editing-here"), (String) null); //TODO: Revisar similares
                return true;
            }
            if (activeProyectos.size() > 1) {
                projectListMenu = new ProjectListMenu(commandPlayer, LanguageHandler.getText(language, "gui-titles.proyectos-activos-list"), activeProyectos, (proyecto, event) -> {
                    String proyectoIdFinal = proyecto.getId();
                    String title = LanguageHandler.replaceMC("gui-titles.finish-project-confirm", language, proyecto);
                    confirmationMenu = new ConfirmationMenu(title, bukkitPlayer, projectListMenu, confirmClick -> {
                            confirmClick.getWhoClicked().closeInventory();
                            if (proyecto.getEstado() == Estado.EN_FINALIZACION) {
                                PlayerLogger.warn(commandPlayer, LanguageHandler.replaceMC("project.finish.request.already", language, proyecto), (String) null);
                                return;
                            }
                            if (proyecto.getEstado() == Estado.EN_FINALIZACION_EDICION) {
                                PlayerLogger.warn(commandPlayer, LanguageHandler.replaceMC("project.edit.finish.request.already", language, proyecto), (String) null);
                                return;
                            }
                            
                            if (proyecto.getEstado() != Estado.ACTIVO && proyecto.getEstado() != Estado.EDITANDO) {
                                PlayerLogger.error(commandPlayer, LanguageHandler.replaceMC("project.not-active-editing", language, proyecto), (String) null);   
                                return;
                            }
                            if (proyecto.getEstado() == Estado.EDITANDO) {
                                ProjectManager.getInstance().createFinishEditRequest(proyectoIdFinal, commandPlayer.getUuid());
                                PlayerLogger.info(bukkitPlayer, LanguageHandler.replaceMC("project.edit.finish.request.success", language, proyecto), (String) null);
                            } else {
                                ProjectManager.getInstance().createFinishRequest(proyectoIdFinal, commandPlayer.getUuid());
                                PlayerLogger.info(commandPlayer, LanguageHandler.replaceMC("project.finish.request.success", language, proyecto), (String) null);
                            }
                        });
                    confirmationMenu.open();
                });
                projectListMenu.open();
                return true;
            }
            proyectoFinal = activeProyectos.iterator().next();
        }
        if (proyectoFinal.getEstado() == Estado.EN_FINALIZACION) {
            PlayerLogger.warn(commandPlayer, LanguageHandler.replaceMC("project.finish.request.already", language, proyectoFinal), (String) null);
            return true;
        }
        if (proyectoFinal.getEstado() == Estado.EN_FINALIZACION_EDICION) {
            PlayerLogger.warn(commandPlayer, LanguageHandler.replaceMC("project.edit.finish.request.already", language, proyectoFinal), (String) null);
            return true;
        }

        if (proyectoFinal.getEstado() != Estado.ACTIVO && proyectoFinal.getEstado() != Estado.EDITANDO) {
            PlayerLogger.error(commandPlayer, LanguageHandler.replaceMC("project.not-active-editing-here", language, proyectoFinal), (String) null);   
            return true;
        }

        final Proyecto proyecto = proyectoFinal;
        confirmationMenu = new ConfirmationMenu(LanguageHandler.replaceMC("gui-titles.finish-project-confirm", language, proyectoFinal), bukkitPlayer, confirmClick -> {
                confirmClick.getWhoClicked().closeInventory();
                if (proyecto.getEstado() == Estado.EDITANDO) {
                    ProjectManager.getInstance().createFinishEditRequest(proyecto.getId(), commandPlayer.getUuid());
                    PlayerLogger.info(bukkitPlayer, LanguageHandler.replaceMC("project.edit.finish.request.success", language, proyecto), (String) null);
                } else {
                    ProjectManager.getInstance().createFinishRequest(proyecto.getId(), commandPlayer.getUuid());
                    PlayerLogger.info(commandPlayer, LanguageHandler.replaceMC("project.finish.request.success", language, proyecto), (String) null);
                }
            }, (cancelClick -> {    
                cancelClick.getWhoClicked().closeInventory();
        }));
        confirmationMenu.open();
        return true;
    }

}
