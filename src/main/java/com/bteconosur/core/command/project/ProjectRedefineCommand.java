package com.bteconosur.core.command.project;

import java.util.Set;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.locationtech.jts.geom.Polygon;

import com.bteconosur.core.ProjectManager;
import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.menu.ConfirmationMenu;
import com.bteconosur.core.menu.project.ProjectListMenu;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.core.util.RegionUtils;
import com.bteconosur.db.PermissionManager;
import com.bteconosur.db.model.Division;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.Proyecto;
import com.bteconosur.db.model.TipoProyecto;
import com.bteconosur.db.registry.InteractionRegistry;
import com.bteconosur.db.registry.PaisRegistry;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.db.registry.ProyectoRegistry;
import com.bteconosur.db.registry.TipoProyectoRegistry;
import com.bteconosur.db.util.Estado;
import com.bteconosur.discord.util.LinkService;

public class ProjectRedefineCommand extends BaseCommand {

    private ConfirmationMenu confirmationMenu;
    private ProjectListMenu projectListMenu;

    public ProjectRedefineCommand() {
        super("redefine", "Redefinir un proyecto existente.", "[id_proyecto]", CommandMode.PLAYER_ONLY);
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
        if (!LinkService.isPlayerLinked(commandPlayer)) {
            PlayerLogger.warn(commandPlayer, LanguageHandler.getText(language, "link.mc-link-recomendation"), (String) null);
        }

        Polygon regionPolygon = RegionUtils.getPolygon(sender);
        if (regionPolygon == null) return true;

        Double tamaño = regionPolygon.getArea();
        TipoProyecto tipoProyecto = TipoProyectoRegistry.getInstance().get(tamaño);
        if (tipoProyecto == null) {
            PlayerLogger.error(bukkitPlayer, LanguageHandler.getText(language, "invalid-project-size"), (String) null);
            return true;
        }

        PaisRegistry paisr = PaisRegistry.getInstance();
        Division division = paisr.findDivisionByPolygon(regionPolygon, paisr.findByPolygon(regionPolygon));
        if (division == null) {
            PlayerLogger.error(bukkitPlayer, LanguageHandler.getText(language, "invalid-project-location"), (String) null);
            return true;
        }

        ProyectoRegistry pr = ProyectoRegistry.getInstance();
        Proyecto targetProyecto = null;
        if (args.length == 1) {
            String proyectoId = args[0];
            targetProyecto = pr.get(proyectoId);
            if (targetProyecto == null) {
                PlayerLogger.warn(commandPlayer, LanguageHandler.getText(language, "project.not-found-id").replace("%search%", args[0]), (String) null);   
                return true;
            }
            if (!permissionManager.isLider(commandPlayer, targetProyecto)) {
                PlayerLogger.error(commandPlayer, LanguageHandler.replaceMC("project.leader.not-leader", language, targetProyecto), (String) null);   
                return true;
            }
        } else {
            Location location = bukkitPlayer.getLocation();
            Set<Proyecto> proyectos = pr.getByLocation(location.getBlockX(), location.getBlockZ());
            if (proyectos.isEmpty()) {
                PlayerLogger.warn(commandPlayer, LanguageHandler.getText(language, "project.not-found-here"), (String) null);
                return true;
            }
            Set<Proyecto> liderProyectos = pr.getByLider(commandPlayer, proyectos);
            if (liderProyectos.isEmpty()) {
                    PlayerLogger.warn(commandPlayer, LanguageHandler.getText(language, "project.leader.not-leader-here"), (String) null);
                    return true;
            }
            Set<Proyecto> activeProyectos = pr.getActiveOrEditando(liderProyectos);
            if (activeProyectos.isEmpty()) {
                PlayerLogger.warn(commandPlayer, LanguageHandler.getText(language, "project.leader.not-leader-active-editing-here"), (String) null);
                return true;
            }
            if (activeProyectos.size() > 1) {
                projectListMenu = new ProjectListMenu(commandPlayer, LanguageHandler.getText(language, "gui-titles.proyectos-activos-list"), activeProyectos, (proyecto, event) -> {
                    String proyectoIdFinal = proyecto.getId();
                    if (!permissionManager.isLider(commandPlayer, proyecto)) {
                        PlayerLogger.error(commandPlayer, LanguageHandler.replaceMC("project.leader.not-leader", language, proyecto), (String) null);   
                        event.getWhoClicked().closeInventory();
                        return;
                    }
                    if (InteractionRegistry.getInstance().findRedefineRequest(proyectoIdFinal) != null) {
                        PlayerLogger.warn(bukkitPlayer,  LanguageHandler.replaceMC("project.redefine.request.already", language, proyecto), (String) null);
                        event.getWhoClicked().closeInventory();
                        return;
                    }
                    if (proyecto.getEstado() != Estado.ACTIVO && proyecto.getEstado() != Estado.EDITANDO) {
                        PlayerLogger.error(commandPlayer, LanguageHandler.replaceMC("project.not-active-editing", language, proyecto), (String) null);   
                        event.getWhoClicked().closeInventory();
                        return;
                    }
                    String title = LanguageHandler.replaceMC("gui-titles.redefine-project-confirm", language, proyecto);
                    confirmationMenu = new ConfirmationMenu(title, bukkitPlayer, projectListMenu, confirmClick -> {
                            confirmClick.getWhoClicked().closeInventory();
                            if (!ProjectManager.getInstance().createRedefineRequest(proyecto.getId(), regionPolygon, commandPlayer.getUuid(), tipoProyecto, division)) {
                                PlayerLogger.error(commandPlayer, LanguageHandler.getText(language, "internal-error"), (String) null);
                                return;
                            }
                            PlayerLogger.info(bukkitPlayer, LanguageHandler.replaceMC("project.redefine.request.success", language, proyecto), (String) null);
                        });
                    confirmationMenu.open();
                });
                projectListMenu.open();
                return true;
            }
            targetProyecto = activeProyectos.iterator().next();
        }
        if (InteractionRegistry.getInstance().findRedefineRequest(targetProyecto.getId()) != null) {
            PlayerLogger.warn(bukkitPlayer, LanguageHandler.replaceMC("project.redefine.request.already", language, targetProyecto), (String) null);
            return true;
        }

        if (targetProyecto.getEstado() != Estado.ACTIVO && targetProyecto.getEstado() != Estado.EDITANDO) {
            PlayerLogger.error(commandPlayer, LanguageHandler.replaceMC("project.not-active-editing", language, targetProyecto), (String) null);   
            return true;
        }

        final Proyecto proyectoFinal = targetProyecto;
        String title = LanguageHandler.replaceMC("gui-titles.redefine-project-confirm", language, targetProyecto);
        confirmationMenu = new ConfirmationMenu(title, bukkitPlayer, confirmClick -> {
            confirmClick.getWhoClicked().closeInventory();
            if (!ProjectManager.getInstance().createRedefineRequest(proyectoFinal.getId(), regionPolygon, commandPlayer.getUuid(), tipoProyecto, division)) {
                PlayerLogger.error(commandPlayer, LanguageHandler.getText(language, "internal-error"), (String) null);
                return;
            }
            PlayerLogger.info(bukkitPlayer, LanguageHandler.replaceMC("project.redefine.request.success", language, proyectoFinal), (String) null);
        }, (cancelClick -> {    
            cancelClick.getWhoClicked().closeInventory();
        }));
        confirmationMenu.open();
        return true;
    }

}
