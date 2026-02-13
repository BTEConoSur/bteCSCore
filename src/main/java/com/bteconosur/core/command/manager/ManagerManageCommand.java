package com.bteconosur.core.command.manager;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.menu.project.ProjectManageMenu;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.PermissionManager;
import com.bteconosur.db.model.Pais;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.Proyecto;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.db.registry.ProyectoRegistry;

public class ManagerManageCommand extends BaseCommand {

    private final YamlConfiguration lang;
    private ProjectManageMenu projectManageMenu;

    public ManagerManageCommand() {
        super("manage", "Gestionar un proyecto del país.", "<id_proyecto>", CommandMode.PLAYER_ONLY);
        this.addSubcommand(new GenericHelpCommand(this));
        lang = ConfigHandler.getInstance().getLang();
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        if (args.length != 1) {
            String message = lang.getString("help-command-usage").replace("%command%", getFullCommand());
            PlayerLogger.info(sender, message, (String) null);
            return true;
        }
        PermissionManager permissionManager = PermissionManager.getInstance();
        Player commandPlayer = PlayerRegistry.getInstance().get(sender);
        Proyecto proyectoFinal = null;
        String proyectoId = args[0];
        proyectoFinal = ProyectoRegistry.getInstance().get(proyectoId);
        if (proyectoFinal == null) {
            PlayerLogger.warn(commandPlayer, lang.getString("no-project-found-with-id").replace("%proyectoId%", proyectoId), (String) null);   
            return true;
        }
        Pais pais = proyectoFinal.getPais();
        if (!permissionManager.isManager(commandPlayer, pais)) {
            PlayerLogger.warn(commandPlayer, lang.getString("not-a-manager-country").replace("%pais%", pais.getNombrePublico()), (String) null);   
            return true;
        }

        projectManageMenu = new ProjectManageMenu(commandPlayer, proyectoFinal, lang.getString("gui-titles.project-manage").replace("%proyectoId%", proyectoId));
        projectManageMenu.open();
        return true;
    }

}
