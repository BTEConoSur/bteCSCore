package com.bteconosur.core.command.manager;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.ProjectManager;
import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.menu.ConfirmationMenu;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.PermissionManager;
import com.bteconosur.db.model.Pais;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.Proyecto;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.db.registry.ProyectoRegistry;
import com.bteconosur.db.util.Estado;

public class ManagerEditCommand extends BaseCommand {

    private ConfirmationMenu confirmationMenu;

    public ManagerEditCommand() {
        super("edit", "Activar edición de cualquier proyecto del país.", "<id_proyecto>", CommandMode.PLAYER_ONLY);
        this.addSubcommand(new GenericHelpCommand(this));
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player commandPlayer = PlayerRegistry.getInstance().get(sender);
        Language language = commandPlayer.getLanguage();
        if (args.length != 1) {
            String message = LanguageHandler.getText(language, "help-command-usage").replace("%comando%", getFullCommand());
            PlayerLogger.info(sender, message, (String) null);
            return true;
        }
        org.bukkit.entity.Player bukkitPlayer = (org.bukkit.entity.Player) sender;
        PermissionManager permissionManager = PermissionManager.getInstance();
        ProyectoRegistry pr = ProyectoRegistry.getInstance();

        String proyectoId = args[0];
        Proyecto proyectoFinal = pr.get(proyectoId);
        if (proyectoFinal == null) {
            PlayerLogger.warn(commandPlayer, LanguageHandler.replaceMC("project.not-found-id", language, proyectoFinal), (String) null);   
            return true;
        }

        Pais pais = proyectoFinal.getPais();
        if (!permissionManager.isManager(commandPlayer, pais)) {
            PlayerLogger.warn(commandPlayer, LanguageHandler.replaceMC("manager.not-manager-country", language, pais), (String) null);   
            return true;
        }

        if (proyectoFinal.getEstado() != Estado.COMPLETADO) {
            PlayerLogger.warn(commandPlayer, LanguageHandler.replaceMC("project.not-completed", language, proyectoFinal), (String) null);   
            return true;
        }

        String title = LanguageHandler.replaceMC("gui-titles.edit-project-confirm", language, proyectoFinal);
        confirmationMenu = new ConfirmationMenu(title, bukkitPlayer, confirmClick -> {
                ProjectManager.getInstance().activateEdit(proyectoFinal.getId(), commandPlayer.getUuid());
                PlayerLogger.info(bukkitPlayer, LanguageHandler.replaceMC("project.edit.activate.success", language, commandPlayer, proyectoFinal), (String) null);
                confirmationMenu.getGui().close(bukkitPlayer);
            }, (cancelClick -> {    
                confirmationMenu.getGui().close(bukkitPlayer);
        }));
        confirmationMenu.open();
        return true;
    }

}
