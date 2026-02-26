package com.bteconosur.core.command.project;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.locationtech.jts.geom.Point;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.Proyecto;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.db.registry.ProyectoRegistry;
import com.bteconosur.world.WorldManager;

public class ProjectTpCommand extends BaseCommand {

    public ProjectTpCommand() {
        super("tp", "<id_proyecto>", "btecs.command.project.tp", CommandMode.PLAYER_ONLY);
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
        Proyecto proyectoFinal = null;
        String proyectoId = args[0];
        proyectoFinal = ProyectoRegistry.getInstance().get(proyectoId);
        if (proyectoFinal == null) {
            PlayerLogger.error(commandPlayer, LanguageHandler.getText(language, "project.not-found-id").replace("%search%", args[0]), (String) null);   
            return true;
        }
        Point centroid = proyectoFinal.getPoligono().getCentroid();
        double x = Math.floor(centroid.getX());
        double z = Math.floor(centroid.getY());
        
        World world = WorldManager.getInstance().getBTEWorld().getLabelWorld(x, z).getBukkitWorld();
        int highestY = world.getHighestBlockYAt((int) x, (int) z);
        
        Location tpLocation = new Location(world, x + 0.5, highestY + 1, z + 0.5, bukkitPlayer.getLocation().getYaw(), bukkitPlayer.getLocation().getPitch());
        bukkitPlayer.teleport(tpLocation);
        
        PlayerLogger.info(commandPlayer, LanguageHandler.replaceMC("project.tp-success", language, proyectoFinal), (String) null);  
        return true;
    }

}
