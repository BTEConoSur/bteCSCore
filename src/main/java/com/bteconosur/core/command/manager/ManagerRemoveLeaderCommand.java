package com.bteconosur.core.command.manager;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.ProjectManager;
import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.PermissionManager;
import com.bteconosur.db.model.Pais;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.Proyecto;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.db.registry.ProyectoRegistry;

public class ManagerRemoveLeaderCommand extends BaseCommand {

    private final YamlConfiguration lang;

    public ManagerRemoveLeaderCommand() {
        super("removeleader", "Quitar a un líder de un proyecto del país.", "<id_proyecto>", CommandMode.PLAYER_ONLY);
        this.addSubcommand(new GenericHelpCommand(this));
        lang = ConfigHandler.getInstance().getLang();
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player commandPlayer = PlayerRegistry.getInstance().get(sender);
        if (args.length != 1) {
            String message = lang.getString("help-command-usage").replace("%command%", getFullCommand());
            PlayerLogger.info(commandPlayer, message, (String) null);
            return true;
        }

        PermissionManager permissionManager = PermissionManager.getInstance();

        ProyectoRegistry proyectoRegistry = ProyectoRegistry.getInstance();
        String proyectoId = args[0];
        Proyecto targetProyecto = proyectoRegistry.get(proyectoId);
        if (targetProyecto == null) {
            PlayerLogger.warn(commandPlayer, lang.getString("no-project-found-with-id").replace("%proyectoId%", proyectoId), (String) null);   
            return true;
        }

        Pais pais = targetProyecto.getPais();
        if (!permissionManager.isManager(commandPlayer, pais)) {
            PlayerLogger.error(commandPlayer, lang.getString("not-a-manager-country").replace("%pais%", pais.getNombrePublico()), (String) null);   
            return true;
        }

        ProjectManager projectManager = ProjectManager.getInstance();
        Player lider = projectManager.getLider(targetProyecto);
        if (permissionManager.hasMembers(targetProyecto)) {
            String message = lang.getString("leader-cant-leave-project-staff").replace("%proyectoId%", targetProyecto.getId());   
            PlayerLogger.error(commandPlayer, message, (String) null);
            return true;
        }
        
        projectManager.removeFromProject(targetProyecto.getId(), lider.getUuid(), commandPlayer.getUuid());
        String successMessage = lang.getString("project-leader-removed-staff").replace("%proyectoId%", targetProyecto.getId()).replace("%player%", lider.getNombre());
        PlayerLogger.info(commandPlayer, successMessage, (String) null);

        return true;
    }

}
