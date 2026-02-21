package com.bteconosur.core.command.reviewer;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.PermissionManager;
import com.bteconosur.db.model.Pais;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.Proyecto;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.db.registry.ProyectoRegistry;
import com.bteconosur.world.ReviewerToggleBuildService;

public class ReviewerToggleBypassCommand extends BaseCommand {

    public ReviewerToggleBypassCommand() {
        super("togglebypass", "Activar o desactivar el bypass de construcción de un proyecto.", "<id_proyecto>", CommandMode.PLAYER_ONLY);
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
        PermissionManager permissionManager = PermissionManager.getInstance();
        Proyecto proyectoFinal = null;

        String proyectoId = args[0];
        proyectoFinal = ProyectoRegistry.getInstance().get(proyectoId);
        if (proyectoFinal == null) {
            String message = LanguageHandler.replaceMC("project.not-found-id", language, proyectoFinal).replace("%proyecto.id%", proyectoId);
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        Pais pais = proyectoFinal.getPais();
        if (!permissionManager.isReviewer(commandPlayer, pais)) {
            PlayerLogger.warn(commandPlayer, LanguageHandler.replaceMC("reviewer.not-reviewer-country", language, pais), (String) null);   
            return true;
        }
        
        ReviewerToggleBuildService.toggle(commandPlayer, proyectoFinal);
        return true;
    }

}
