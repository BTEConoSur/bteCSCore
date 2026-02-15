package com.bteconosur.core.command.manager;

import java.util.Set;

import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.menu.project.ProjectListMenu;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.core.util.RegionUtils;
import com.bteconosur.db.PermissionManager;
import com.bteconosur.db.model.Pais;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.Proyecto;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.db.registry.ProyectoRegistry;

public class ManagerSelectCommand extends BaseCommand {

    private final YamlConfiguration lang;
    private final YamlConfiguration config;
    private ProjectListMenu projectListMenu;

    public ManagerSelectCommand() {
        super("select", "Seleccionar un proyecto del país con World Edit.", "", CommandMode.PLAYER_ONLY);
        this.addSubcommand(new GenericHelpCommand(this));
        lang = ConfigHandler.getInstance().getLang();
        config = ConfigHandler.getInstance().getConfig();
    }

    @Override
    protected boolean onCommand(org.bukkit.command.CommandSender sender, String[] args) {
        if (args.length > 0) {
            String message = lang.getString("help-command-usage").replace("%command%", getFullCommand());
            PlayerLogger.info(sender, message, (String) null);
            return true;
        }
        org.bukkit.entity.Player bukkitPlayer = (org.bukkit.entity.Player) sender;
        Player commandPlayer = PlayerRegistry.getInstance().get(sender);
        PermissionManager permissionManager = PermissionManager.getInstance();
        ProyectoRegistry pr = ProyectoRegistry.getInstance();
        Proyecto proyectoFinal = null;
        int playerY = (int) bukkitPlayer.getLocation().getY();

        Set<Proyecto> proyectos = pr.getByLocation(bukkitPlayer.getLocation().getBlockX(), bukkitPlayer.getLocation().getBlockZ());
        if (proyectos.isEmpty()) {
            PlayerLogger.warn(commandPlayer, lang.getString("no-project-found-here"), (String) null);
            return true;
        }
        Set<Proyecto> memberProyectos = pr.getMemberOrLider(commandPlayer, proyectos);
        if (memberProyectos.isEmpty()) {
            PlayerLogger.warn(commandPlayer, lang.getString("not-a-member-here"), (String) null);
            return true;
        }

        if (memberProyectos.size() > 1) {
            projectListMenu = new ProjectListMenu(commandPlayer, lang.getString("gui-titles.proyectos-here-list"), memberProyectos, (proyecto, event) -> {
                event.getWhoClicked().closeInventory();
                Pais pais = proyecto.getPais();
                if (!permissionManager.isManager(commandPlayer, pais)) {
                    PlayerLogger.warn(commandPlayer, lang.getString("not-a-manager-country").replace("%pais%", pais.getNombrePublico()), (String) null);   
                    return;
                }
                RegionUtils.selectPolygon(bukkitPlayer, proyecto.getPoligono(), playerY - config.getInt("world-edit.region-select-extension"), playerY + config.getInt("world-edit.region-select-extension"));
            });
            projectListMenu.open();
            return true;
        }
        proyectoFinal = memberProyectos.iterator().next();
        
        Pais pais = proyectoFinal.getPais();
        if (!permissionManager.isManager(commandPlayer, pais)) {
            PlayerLogger.warn(commandPlayer, lang.getString("not-a-manager-country").replace("%pais%", pais.getNombrePublico()), (String) null);   
            return true;
        }
        
        RegionUtils.selectPolygon(bukkitPlayer, proyectoFinal.getPoligono(), playerY - config.getInt("world-edit.region-select-extension"), playerY + config.getInt("world-edit.region-select-extension"));
        return true;
    }

}
