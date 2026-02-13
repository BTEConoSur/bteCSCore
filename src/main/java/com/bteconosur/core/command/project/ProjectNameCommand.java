package com.bteconosur.core.command.project;

import java.util.Set;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.ProjectManager;
import com.bteconosur.core.chat.ChatUtil;
import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.util.DiscordLogger;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.PermissionManager;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.Proyecto;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.db.registry.ProyectoRegistry;

import net.dv8tion.jda.api.entities.MessageEmbed;

public class ProjectNameCommand extends BaseCommand {

    private final YamlConfiguration lang;

    public ProjectNameCommand() {
        super("name", "Cambiar el nombre de un proyecto.", "<id_proyecto> <nuevo_nombre>", CommandMode.PLAYER_ONLY);
        this.addSubcommand(new GenericHelpCommand(this));
        ConfigHandler configHandler = ConfigHandler.getInstance();
        lang = configHandler.getLang();
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player commandPlayer = PlayerRegistry.getInstance().get(sender);
        PermissionManager permissionManager = PermissionManager.getInstance();
        if (args.length != 2) {
            String message = lang.getString("help-command-usage").replace("%command%", getFullCommand());
            PlayerLogger.info(commandPlayer, message, (String) null);
            return true;
        }

        ProyectoRegistry proyectoRegistry = ProyectoRegistry.getInstance();
        String proyectoId = args[0];
        Proyecto targetProyecto = proyectoRegistry.get(proyectoId);
        if (targetProyecto == null) {
            PlayerLogger.warn(commandPlayer, lang.getString("no-project-found-with-id").replace("%proyectoId%", proyectoId), (String) null);   
            return true;
        }
        if (!permissionManager.isLider(commandPlayer, targetProyecto)) {
            PlayerLogger.error(commandPlayer, lang.getString("not-a-leader-project").replace("%proyectoId%", targetProyecto.getId()), (String) null);   
            return true;
        }

        String nombre = args[1];
        if (nombre.length() > 50) {
            PlayerLogger.error(commandPlayer, lang.getString("invalid-project-name"), (String) null);
            return true;
        }

        ProjectManager projectManager = ProjectManager.getInstance();
        targetProyecto.setNombre(nombre);
        proyectoRegistry.merge(targetProyecto.getId());
        PlayerLogger.info(commandPlayer, lang.getString("project-name-updated").replace("%proyectoId%", targetProyecto.getId()).replace("%nombre%", nombre), (String) null);
        Set<Player> miembros = projectManager.getMembers(targetProyecto);
        String notification = lang.getString("project-name-updated-member").replace("%proyectoId%", targetProyecto.getId()).replace("%nombre%", nombre);
        MessageEmbed embed = ChatUtil.getDsProjectNameUpdated(targetProyecto.getId(), nombre);
        for (Player miembro : miembros) PlayerLogger.info(miembro, notification, embed);
        String countryLog = lang.getString("project-name-update-log").replace("%proyectoId%", targetProyecto.getId()).replace("%proyectoNombre%", nombre).replace("%lider%", commandPlayer.getNombre());
        DiscordLogger.countryLog(countryLog, targetProyecto.getPais());
        return true;
    }
    
}
