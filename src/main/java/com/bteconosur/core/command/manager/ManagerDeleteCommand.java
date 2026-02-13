package com.bteconosur.core.command.manager;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.ProjectManager;
import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.menu.ConfirmationMenu;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.PermissionManager;
import com.bteconosur.db.model.Pais;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.Proyecto;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.db.registry.ProyectoRegistry;

public class ManagerDeleteCommand extends BaseCommand {

    private final YamlConfiguration lang;
    private ConfirmationMenu confirmationMenu;

    public ManagerDeleteCommand() {
        super("delete", "Eliminar cualquier proyecto del país.", "<id_proyecto>", CommandMode.PLAYER_ONLY);
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
        org.bukkit.entity.Player bukkitPlayer = (org.bukkit.entity.Player) sender;
        PermissionManager permissionManager = PermissionManager.getInstance();
        ProyectoRegistry pr = ProyectoRegistry.getInstance();
        Player commandPlayer = PlayerRegistry.getInstance().get(sender);

        String proyectoId = args[0];
        Proyecto proyectoFinal = pr.get(proyectoId);
        if (proyectoFinal == null) {
            PlayerLogger.warn(commandPlayer, lang.getString("no-project-found-with-id").replace("%proyectoId%", proyectoId), (String) null);   
            return true;
        }

        Pais pais = proyectoFinal.getPais();
        if (!permissionManager.isManager(commandPlayer, pais)) {
            PlayerLogger.warn(commandPlayer, lang.getString("not-a-manager-country").replace("%pais%", pais.getNombrePublico()), (String) null);   
            return true;
        }

        confirmationMenu = new ConfirmationMenu(lang.getString("gui-titles.delete-project-confirm").replace("%proyectoId%", proyectoFinal.getId()), bukkitPlayer, confirmClick -> {
                ProjectManager.getInstance().deleteProject(proyectoFinal, commandPlayer.getUuid());
                PlayerLogger.info(commandPlayer, lang.getString("project-delete-request-success").replace("%proyectoId%", proyectoFinal.getId()), (String) null);
                confirmationMenu.getGui().close(bukkitPlayer);
            }, (cancelClick -> {    
                confirmationMenu.getGui().close(bukkitPlayer);
        }));
        confirmationMenu.open();
        return true;
    }
    
}
