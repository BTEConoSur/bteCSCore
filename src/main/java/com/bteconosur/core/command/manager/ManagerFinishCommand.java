package com.bteconosur.core.command.manager;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.ProjectManager;
import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.menu.ConfirmationMenu;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.PermissionManager;
import com.bteconosur.db.model.Pais;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.Proyecto;
import com.bteconosur.db.registry.PaisRegistry;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.db.registry.ProyectoRegistry;
import com.bteconosur.db.util.Estado;

public class ManagerFinishCommand extends BaseCommand {

    private final YamlConfiguration lang;
    private ConfirmationMenu confirmationMenu;

    public ManagerFinishCommand() {
        super("finish", "Finalizar cualquier proyecto del país.", "<id_proyecto>", CommandMode.PLAYER_ONLY);
        lang = ConfigHandler.getInstance().getLang();
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        if (args.length != 1) {
            String message = lang.getString("help-command-usage").replace("%command%", getFullCommand().replace(" " + command, ""));
            PlayerLogger.info(sender, message, (String) null);
            return true;
        }
        org.bukkit.entity.Player bukkitPlayer = (org.bukkit.entity.Player) sender;
        PermissionManager permissionManager = PermissionManager.getInstance();
        ProyectoRegistry pr = ProyectoRegistry.getInstance();
        Player commandPlayer = PlayerRegistry.getInstance().get(sender);
        Location location = bukkitPlayer.getLocation();
        Pais pais = PaisRegistry.getInstance().findByLocation(location.getBlockX(), location.getBlockZ()); 
        if (!permissionManager.isManager(commandPlayer, pais)) {
            PlayerLogger.warn(commandPlayer, lang.getString("not-a-manager-country").replace("%pais%", pais.getNombrePublico()), (String) null);   
            return true;
        }

        String proyectoId = args[0];
        Proyecto proyectoFinal = pr.get(proyectoId);
        if (proyectoFinal == null) {
            PlayerLogger.warn(commandPlayer, lang.getString("no-project-found-with-id").replace("%proyectoId%", proyectoId), (String) null);   
            return true;
        }
        if (proyectoFinal.getEstado() != Estado.ACTIVO) {
            PlayerLogger.warn(commandPlayer, lang.getString("not-a-active-project").replace("%proyectoId%", proyectoFinal.getId()), (String) null);   
            return true;
        }

        final String proyectoIdFinal = proyectoFinal.getId();
        confirmationMenu = new ConfirmationMenu(lang.getString("gui-titles.finish-project-confirm").replace("%proyectoId%", proyectoIdFinal), bukkitPlayer, confirmClick -> {
                ProjectManager.getInstance().createFinishRequest(proyectoIdFinal, commandPlayer.getUuid());
                PlayerLogger.info(commandPlayer, lang.getString("project-finish-request-success").replace("%proyectoId%", proyectoIdFinal), (String) null);
                confirmationMenu.getGui().close(bukkitPlayer);
            }, (cancelClick -> {    
                confirmationMenu.getGui().close(bukkitPlayer);
        }));
        confirmationMenu.open();
        return true;
    }

}
