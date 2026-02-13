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

public class ProjectDescriptionCommand extends BaseCommand {

    private final YamlConfiguration lang;

    public ProjectDescriptionCommand() {
        super("description", "Cambiar la descripción de un proyecto.", "<id_proyecto> <nueva_descripcion>", CommandMode.PLAYER_ONLY);
        this.addSubcommand(new GenericHelpCommand(this));
        ConfigHandler configHandler = ConfigHandler.getInstance();
        lang = configHandler.getLang();
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player commandPlayer = PlayerRegistry.getInstance().get(sender);
        PermissionManager permissionManager = PermissionManager.getInstance();
        if (args.length <= 1) {
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

        StringBuilder descripcionBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            if (i > 1) descripcionBuilder.append(" ");
            descripcionBuilder.append(args[i]);
        }
        String descripcion = descripcionBuilder.toString();

        if (descripcion.length() > 100) {
            PlayerLogger.error(commandPlayer, lang.getString("invalid-project-description"), (String) null);
            return true;
        }

        ProjectManager projectManager = ProjectManager.getInstance();
        targetProyecto.setDescripcion(descripcion);
        proyectoRegistry.merge(targetProyecto.getId());
        PlayerLogger.info(commandPlayer, lang.getString("project-description-updated").replace("%proyectoId%", targetProyecto.getId()).replace("%descripcion%", descripcion), (String) null);
        Set<Player> miembros = projectManager.getMembers(targetProyecto);
        String notification = lang.getString("project-description-updated-member").replace("%proyectoId%", targetProyecto.getId()).replace("%descripcion%", descripcion);
        MessageEmbed embed = ChatUtil.getDsProjectDescriptionUpdated(targetProyecto.getId(), descripcion);
        for (Player miembro : miembros) PlayerLogger.info(miembro, notification, embed);
        String countryLog = lang.getString("project-description-update-log").replace("%proyectoId%", targetProyecto.getId()).replace("%proyectoDescripcion%", descripcion).replace("%lider%", commandPlayer.getNombre());
        DiscordLogger.countryLog(countryLog, targetProyecto.getPais());
        return true;
    }

}
