package com.bteconosur.core.command.project;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.locationtech.jts.geom.Polygon;

import com.bteconosur.core.ProjectManager;
import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.core.util.RegionUtils;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.discord.util.LinkService;


public class ProjectCreateCommand extends BaseCommand {

    private final YamlConfiguration lang;

    public ProjectCreateCommand() {
        super("create", "Crear un nuevo proyecto.", "[nombre] [descripción]", "btecs.command.project.create", CommandMode.PLAYER_ONLY);
        lang = ConfigHandler.getInstance().getLang();
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        String nombre = null;
        String descripcion = null;

        Player commandPlayer = PlayerRegistry.getInstance().get(sender);
        if (!LinkService.isPlayerLinked(commandPlayer)) {
            PlayerLogger.warn(commandPlayer, lang.getString("minecraft-link-needed"), (String) null);
            return true;
        }

        if (args.length >= 1) {
            nombre = args[0];
            if (nombre.length() > 50) {
                PlayerLogger.error(commandPlayer, lang.getString("invalid-project-name"), (String) null);
                return true;
            }
        }

        if (args.length >= 2) {
            StringBuilder descripcionBuilder = new StringBuilder();
            for (int i = 1; i < args.length; i++) {
                if (i > 1) descripcionBuilder.append(" ");
                descripcionBuilder.append(args[i]);
            }
            descripcion = descripcionBuilder.toString();

            if (descripcion.length() > 100) {
                PlayerLogger.error(commandPlayer, lang.getString("invalid-project-description"), (String) null);
                return true;
            }
        }

        Polygon regionPolygon = RegionUtils.getPolygon(sender);
        if (regionPolygon == null) return true;
        ProjectManager.getInstance().createProject(nombre, descripcion, regionPolygon, commandPlayer);
        return true;
    }

}
