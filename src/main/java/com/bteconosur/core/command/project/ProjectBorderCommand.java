package com.bteconosur.core.command.project;

import java.util.Set;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.menu.project.ProjectListMenu;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.Proyecto;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.db.registry.ProyectoRegistry;

public class ProjectBorderCommand extends BaseCommand {

    private ProjectListMenu projectListMenu;

    public ProjectBorderCommand() {
        super("border", "Activar o desactivar el borde de un proyecto.", "[id_proyecto]", CommandMode.PLAYER_ONLY);
        this.addSubcommand(new GenericHelpCommand(this));
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player commandPlayer = PlayerRegistry.getInstance().get(sender);
        Language language = commandPlayer.getLanguage();
        if (args.length > 1) {
            String message = LanguageHandler.getText(language, "help-command-usage").replace("%comando%", getFullCommand().replace(" " + command, ""));
            PlayerLogger.info(sender, message, (String) null);
            return true;
        }
        org.bukkit.entity.Player bukkitPlayer = (org.bukkit.entity.Player) sender;
        ProyectoRegistry pr = ProyectoRegistry.getInstance();
        Proyecto proyectoFinal = null;
        if (args.length == 1) {
            String proyectoId = args[0];
            proyectoFinal = ProyectoRegistry.getInstance().get(proyectoId);
            if (proyectoFinal == null) {
                PlayerLogger.warn(commandPlayer, LanguageHandler.replaceMC("project.not-found-id", language, proyectoFinal), (String) null);   
                return true;
            }
        } else {
            Set<Proyecto> proyectos = ProyectoRegistry.getInstance().getByLocation(bukkitPlayer.getLocation().getBlockX(), bukkitPlayer.getLocation().getBlockZ());
            if (proyectos.isEmpty()) {
                PlayerLogger.warn(commandPlayer, LanguageHandler.getText(language, "project.not-found-here"), (String) null);
                return true;
            }
            if (proyectos.size() > 1) {
                projectListMenu = new ProjectListMenu(commandPlayer, LanguageHandler.getText(language, "gui-titles.proyectos-here-list"), proyectos, (proyecto, event) -> {
                    event.getWhoClicked().closeInventory();
                    String lastProyectoId = pr.getPlayerBorderParticle(commandPlayer.getUuid());
                    if (lastProyectoId != null && lastProyectoId.equals(proyecto.getId())) {
                        pr.removePlayerBorderParticle(commandPlayer.getUuid());
                        String message = LanguageHandler.replaceMC("project.border.deactivated", language, proyecto);
                        PlayerLogger.info(commandPlayer, message, (String) null);
                    } else {
                        pr.addPlayerBorderParticle(commandPlayer.getUuid(), proyecto.getId());
                        String message = LanguageHandler.replaceMC("project.border.activated", language, proyecto);
                        PlayerLogger.info(commandPlayer, message, (String) null);
                    }
                });
                projectListMenu.open();
                return true;
            }
            proyectoFinal = proyectos.iterator().next();
        }

        String lastProyectoId = pr.getPlayerBorderParticle(commandPlayer.getUuid());
        if (lastProyectoId != null && lastProyectoId.equals(proyectoFinal.getId())) {
            pr.removePlayerBorderParticle(commandPlayer.getUuid());
            String message = LanguageHandler.replaceMC("project.border.deactivated", language, proyectoFinal);
            PlayerLogger.info(commandPlayer, message, (String) null);
        } else {
            pr.addPlayerBorderParticle(commandPlayer.getUuid(), proyectoFinal.getId());
            String message = LanguageHandler.replaceMC("project.border.activated", language, proyectoFinal);
            PlayerLogger.info(commandPlayer, message, (String) null);
        }
        return true;
    }

}
