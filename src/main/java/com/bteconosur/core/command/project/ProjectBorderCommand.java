package com.bteconosur.core.command.project;

import java.util.Set;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.menu.project.ProjectListMenu;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.Proyecto;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.db.registry.ProyectoRegistry;

public class ProjectBorderCommand extends BaseCommand {

    private final YamlConfiguration lang;
    private ProjectListMenu projectListMenu;

    public ProjectBorderCommand() {
        super("border", "Activar o desactivar el borde de un proyecto.", "[id_proyecto]", CommandMode.PLAYER_ONLY);
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
        Player commandPlayer = PlayerRegistry.getInstance().get(sender);
        ProyectoRegistry pr = ProyectoRegistry.getInstance();
        Proyecto proyectoFinal = null;
        if (args.length == 1) {
            String proyectoId = args[0];
            proyectoFinal = ProyectoRegistry.getInstance().get(proyectoId);
            if (proyectoFinal == null) {
                PlayerLogger.warn(commandPlayer, lang.getString("no-project-found-with-id").replace("%proyectoId%", proyectoId), (String) null);   
                return true;
            }
        } else {
            Set<Proyecto> proyectos = ProyectoRegistry.getInstance().getByLocation(bukkitPlayer.getLocation().getBlockX(), bukkitPlayer.getLocation().getBlockZ());
            if (proyectos.isEmpty()) {
                PlayerLogger.warn(commandPlayer, lang.getString("no-project-found-here"), (String) null);
                return true;
            }
            if (proyectos.size() > 1) {
                projectListMenu = new ProjectListMenu(commandPlayer, lang.getString("gui-titles.proyectos-here-list"), proyectos, (proyecto, event) -> {
                    event.getWhoClicked().closeInventory();
                    String lastProyectoId = pr.getPlayerBorderParticle(commandPlayer.getUuid());
                    if (lastProyectoId != null && lastProyectoId.equals(proyecto.getId())) {
                        pr.removePlayerBorderParticle(commandPlayer.getUuid());
                        String message = lang.getString("project-border-deactivated").replace("%proyectoId%", proyecto.getId());
                        PlayerLogger.info(commandPlayer, message, (String) null);
                    } else {
                        pr.addPlayerBorderParticle(commandPlayer.getUuid(), proyecto.getId());
                        String message = lang.getString("project-border-activated").replace("%proyectoId%", proyecto.getId());
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
            String message = lang.getString("project-border-deactivated").replace("%proyectoId%", proyectoFinal.getId());
            PlayerLogger.info(commandPlayer, message, (String) null);
        } else {
            pr.addPlayerBorderParticle(commandPlayer.getUuid(), proyectoFinal.getId());
            String message = lang.getString("project-border-activated").replace("%proyectoId%", proyectoFinal.getId());
            PlayerLogger.info(commandPlayer, message, (String) null);
        }
        return true;
    }

}
