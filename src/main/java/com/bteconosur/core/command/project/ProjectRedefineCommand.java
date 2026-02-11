package com.bteconosur.core.command.project;

import java.util.Set;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.locationtech.jts.geom.Polygon;

import com.bteconosur.core.ProjectManager;
import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.config.ConfigHandler;
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

    private final YamlConfiguration lang;
    private ConfirmationMenu confirmationMenu;
    private ProjectListMenu projectListMenu;

    public ProjectRedefineCommand() {
        super("redefine", "Redefinir un proyecto existente.", "[id_proyecto]", CommandMode.PLAYER_ONLY);
        this.addSubcommand(new GenericHelpCommand(this));
        lang = ConfigHandler.getInstance().getLang();
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        if (args.length > 1) {
            String message = lang.getString("help-command-usage").replace("%command%", getFullCommand());
            PlayerLogger.info(sender, message, (String) null);
            return true;
        }
        org.bukkit.entity.Player bukkitPlayer = (org.bukkit.entity.Player) sender;
        PermissionManager permissionManager = PermissionManager.getInstance();
        Player commandPlayer = PlayerRegistry.getInstance().get(sender);
        if (!LinkService.isPlayerLinked(commandPlayer)) {
            PlayerLogger.warn(commandPlayer, lang.getString("minecraft-link-recomendation"), (String) null);
        }

        Polygon regionPolygon = RegionUtils.getPolygon(sender);
        if (regionPolygon == null) return true;

        Double tamaño = regionPolygon.getArea();
        TipoProyecto tipoProyecto = TipoProyectoRegistry.getInstance().get(tamaño);
        if (tipoProyecto == null) {
            PlayerLogger.error(bukkitPlayer, lang.getString("invalid-project-size"), (String) null);
            return true;
        }

        PaisRegistry paisr = PaisRegistry.getInstance();
        Division division = paisr.findDivisionByPolygon(regionPolygon, paisr.findByPolygon(regionPolygon));
        if (division == null) {
            PlayerLogger.error(bukkitPlayer, lang.getString("invalid-project-location"), (String) null);
            return true;
        }

        ProyectoRegistry pr = ProyectoRegistry.getInstance();
        Proyecto targetProyecto = null;
        if (args.length == 1) {
            String proyectoId = args[0];
            targetProyecto = pr.get(proyectoId);
            if (targetProyecto == null) {
                PlayerLogger.warn(commandPlayer, lang.getString("no-project-found-with-id").replace("%proyectoId%", proyectoId), (String) null);   
                return true;
            }
            if (!permissionManager.isLider(commandPlayer, targetProyecto)) {
                PlayerLogger.error(commandPlayer, lang.getString("not-a-leader-project").replace("%proyectoId%", proyectoId), (String) null);   
                return true;
            }
        } else {
            Location location = bukkitPlayer.getLocation();
            Set<Proyecto> proyectos = pr.getByLocation(location.getBlockX(), location.getBlockZ());
            if (proyectos.isEmpty()) {
                PlayerLogger.warn(commandPlayer, lang.getString("no-project-found-here"), (String) null);
                return true;
            }
            Set<Proyecto> liderProyectos = pr.getByLider(commandPlayer, proyectos);
            if (liderProyectos.isEmpty()) {
                    PlayerLogger.warn(commandPlayer, lang.getString("not-a-leader-here"), (String) null);
                    return true;
            }
            Set<Proyecto> activeProyectos = pr.getActiveOrEditando(liderProyectos);
            if (activeProyectos.isEmpty()) {
                PlayerLogger.warn(commandPlayer, lang.getString("no-project-active-editing-here"), (String) null);
                return true;
            }
            if (activeProyectos.size() > 1) {
                projectListMenu = new ProjectListMenu(commandPlayer, lang.getString("gui-titles.proyectos-activos-list"), activeProyectos, (proyecto, event) -> {
                    String proyectoIdFinal = proyecto.getId();
                    if (!permissionManager.isLider(commandPlayer, proyecto)) {
                        PlayerLogger.error(commandPlayer, lang.getString("not-a-leader-project").replace("%proyectoId%", proyectoIdFinal), (String) null);   
                        event.getWhoClicked().closeInventory();
                        return;
                    }
                    if (InteractionRegistry.getInstance().findRedefineRequest(proyectoIdFinal) != null) {
                        PlayerLogger.warn(bukkitPlayer, lang.getString("project-redefine-request-already").replace("%proyectoId%", proyectoIdFinal), (String) null);
                        event.getWhoClicked().closeInventory();
                        return;
                    }
                    if (proyecto.getEstado() != Estado.ACTIVO && proyecto.getEstado() != Estado.EDITANDO) {
                        PlayerLogger.error(commandPlayer, lang.getString("not-a-active-editing-project").replace("%proyectoId%", proyecto.getId()), (String) null);   
                        event.getWhoClicked().closeInventory();
                        return;
                    }
                    confirmationMenu = new ConfirmationMenu(lang.getString("gui-titles.redefine-project-confirm").replace("%proyectoId%", proyectoIdFinal), bukkitPlayer, projectListMenu, confirmClick -> {
                            confirmClick.getWhoClicked().closeInventory();
                            if (!ProjectManager.getInstance().createRedefineRequest(proyecto.getId(), regionPolygon, commandPlayer.getUuid(), tipoProyecto, division)) {
                                PlayerLogger.error(commandPlayer, lang.getString("internal-error"), (String) null);
                                return;
                            }
                            PlayerLogger.info(bukkitPlayer, lang.getString("project-redefine-request-success").replace("%proyectoId%", proyecto.getId()), (String) null);
                        });
                    confirmationMenu.open();
                });
                projectListMenu.open();
                return true;
            }
            targetProyecto = activeProyectos.iterator().next();
        }
        if (InteractionRegistry.getInstance().findRedefineRequest(targetProyecto.getId()) != null) {
            PlayerLogger.warn(bukkitPlayer, lang.getString("project-redefine-request-already").replace("%proyectoId%", targetProyecto.getId()), (String) null);
            return true;
        }

        if (targetProyecto.getEstado() != Estado.ACTIVO && targetProyecto.getEstado() != Estado.EDITANDO) {
            PlayerLogger.error(commandPlayer, lang.getString("not-a-active-editing-project").replace("%proyectoId%", targetProyecto.getId()), (String) null);   
            return true;
        }

        final String proyectoIdFinal = targetProyecto.getId();
        confirmationMenu = new ConfirmationMenu(lang.getString("gui-titles.redefine-project-confirm").replace("%proyectoId%", proyectoIdFinal), bukkitPlayer, confirmClick -> {
            confirmClick.getWhoClicked().closeInventory();
            if (!ProjectManager.getInstance().createRedefineRequest(proyectoIdFinal, regionPolygon, commandPlayer.getUuid(), tipoProyecto, division)) {
                PlayerLogger.error(commandPlayer, lang.getString("internal-error"), (String) null);
                return;
            }
            PlayerLogger.info(bukkitPlayer, lang.getString("project-redefine-request-success").replace("%proyectoId%", proyectoIdFinal), (String) null);
        }, (cancelClick -> {    
            cancelClick.getWhoClicked().closeInventory();
        }));
        confirmationMenu.open();
        return true;
    }

}
