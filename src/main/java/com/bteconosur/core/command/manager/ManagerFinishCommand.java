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

public class ManagerFinishCommand extends BaseCommand {

    private ConfirmationMenu confirmationMenu;

    public ManagerFinishCommand() {
        super("finish", "<id_proyecto>", CommandMode.PLAYER_ONLY);
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
            PlayerLogger.error(commandPlayer, LanguageHandler.getText(language, "project.not-found-id").replace("%search%", args[0]), (String) null);   
            return true;
        }

        Pais pais = proyectoFinal.getPais();
        if (!permissionManager.isManager(commandPlayer, pais)) {
            PlayerLogger.error(commandPlayer, LanguageHandler.replaceMC("manager.not-manager-country", language, pais), (String) null);   
            return true;
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
            PlayerLogger.error(commandPlayer, LanguageHandler.replaceMC("project.not-active-editing", language, proyectoFinal), (String) null);
            return true;
        }

        final String proyectoIdFinal = proyectoFinal.getId();
        final Proyecto proyecto = proyectoFinal;
        confirmationMenu = new ConfirmationMenu(LanguageHandler.replaceMC("gui-titles.finish-project-confirm", language, proyectoFinal), bukkitPlayer, confirmClick -> {
                confirmClick.getWhoClicked().closeInventory();
                if (proyecto.getEstado() == Estado.EDITANDO) {
                    ProjectManager.getInstance().createFinishEditRequest(proyectoIdFinal, commandPlayer.getUuid());
                    PlayerLogger.info(commandPlayer, LanguageHandler.replaceMC("project.edit.finish.request.success", language, proyecto), (String) null);
                } else {
                    ProjectManager.getInstance().createFinishRequest(proyectoIdFinal, commandPlayer.getUuid());
                    PlayerLogger.info(commandPlayer, LanguageHandler.replaceMC("project.finish.request.success", language, proyecto), (String) null);
                }
                confirmationMenu.getGui().close(bukkitPlayer);
            }, (cancelClick -> {    
                cancelClick.getWhoClicked().closeInventory();
                confirmationMenu.getGui().close(bukkitPlayer);
        }));
        confirmationMenu.open();
        return true;
    }

    @Override
    protected boolean customPermissionCheck(CommandSender sender) {
        Player commandPlayer = PlayerRegistry.getInstance().get(((org.bukkit.entity.Player) sender).getUniqueId());
        return PermissionManager.getInstance().isManager(commandPlayer);
    }
}
