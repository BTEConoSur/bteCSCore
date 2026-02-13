package com.bteconosur.core.command.project;

import java.util.Set;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.menu.project.ProjectListMenu;
import com.bteconosur.core.menu.project.ProjectManageMenu;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.PermissionManager;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.Proyecto;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.db.registry.ProyectoRegistry;

public class ProjectManageCommand extends BaseCommand {

    private final YamlConfiguration lang;
    private ProjectListMenu projectListMenu;
    private ProjectManageMenu projectManageMenu;

    public ProjectManageCommand() {
        super("manage", "Gestionar un proyecto.", "[id_proyecto]", CommandMode.PLAYER_ONLY);
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
        Proyecto proyectoFinal = null;
        if (args.length == 1) {
            String proyectoId = args[0];
            proyectoFinal = ProyectoRegistry.getInstance().get(proyectoId);
            if (proyectoFinal == null) {
                PlayerLogger.warn(commandPlayer, lang.getString("no-project-found-with-id").replace("%proyectoId%", proyectoId), (String) null);   
                return true;
            }
            if (!permissionManager.isMiembroOrLider(commandPlayer, proyectoFinal)) {
                String message = lang.getString("not-a-member").replace("%proyectoId%", proyectoFinal.getId());   
                PlayerLogger.error(commandPlayer, message, (String) null);
                return true;
            }
        } else {
            Set<Proyecto> proyectos = ProyectoRegistry.getInstance().getByLocation(bukkitPlayer.getLocation().getBlockX(), bukkitPlayer.getLocation().getBlockZ());
            if (proyectos.isEmpty()) {
                PlayerLogger.warn(commandPlayer, lang.getString("no-project-found-here"), (String) null);
                return true;
            }
            Set<Proyecto> liderProyectos = ProyectoRegistry.getInstance().getMemberOrLider(commandPlayer, proyectos);
            if (liderProyectos.isEmpty()) {
                PlayerLogger.warn(commandPlayer, lang.getString("not-a-member-here"), (String) null);
                return true;
            }
            
            if (liderProyectos.size() > 1) {
                projectListMenu = new ProjectListMenu(commandPlayer, lang.getString("gui-titles.proyectos-here-list"), liderProyectos, (proyecto, event) -> {
                    String proyectoIdFinal = proyecto.getId();
                    projectManageMenu = new ProjectManageMenu(commandPlayer, proyecto, lang.getString("gui-titles.project-manage").replace("%proyectoId%", proyectoIdFinal));
                });
                projectListMenu.open();
                return true;
            }
            proyectoFinal = liderProyectos.iterator().next();
        }


        final String proyectoIdFinal = proyectoFinal.getId();
        projectManageMenu = new ProjectManageMenu(commandPlayer, proyectoFinal, lang.getString("gui-titles.project-manage").replace("%proyectoId%", proyectoIdFinal));
        projectManageMenu.open();
        return true;
    }

}
