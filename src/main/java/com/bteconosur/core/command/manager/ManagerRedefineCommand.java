package com.bteconosur.core.command.manager;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.locationtech.jts.geom.Polygon;

import com.bteconosur.core.ProjectManager;
import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.menu.ConfirmationMenu;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.core.util.RegionUtils;
import com.bteconosur.db.PermissionManager;
import com.bteconosur.db.model.Division;
import com.bteconosur.db.model.Pais;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.Proyecto;
import com.bteconosur.db.model.TipoProyecto;
import com.bteconosur.db.registry.InteractionRegistry;
import com.bteconosur.db.registry.PaisRegistry;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.db.registry.ProyectoRegistry;
import com.bteconosur.db.registry.TipoProyectoRegistry;
import com.bteconosur.db.util.Estado;
import com.bteconosur.discord.util.LinkService;

public class ManagerRedefineCommand extends BaseCommand {

    private final YamlConfiguration lang;
    private ConfirmationMenu confirmationMenu;

    public ManagerRedefineCommand() {
        super("redefine", "Redefinir un proyecto de un país.", "<id_proyecto>", CommandMode.PLAYER_ONLY);
        this.addSubcommand(new GenericHelpCommand(this));
        lang = ConfigHandler.getInstance().getLang();
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        if (args.length != 1) {
            String message = lang.getString("help-command-usage").replace("%command%", getFullCommand());
            PlayerLogger.info(sender, message, (String) null);
            return true;
        }
        org.bukkit.entity.Player bukkitPlayer = (org.bukkit.entity.Player) sender;
        PermissionManager permissionManager = PermissionManager.getInstance();
        Player commandPlayer = PlayerRegistry.getInstance().get(sender);
        ProyectoRegistry pr = ProyectoRegistry.getInstance();
        if (!LinkService.isPlayerLinked(commandPlayer)) {
            PlayerLogger.warn(commandPlayer, lang.getString("minecraft-link-recomendation"), (String) null);
        }

        String proyectoId = args[0];
        Proyecto proyectoFinal = pr.get(proyectoId);
        if (proyectoFinal == null) {
            PlayerLogger.warn(commandPlayer, lang.getString("no-project-found-with-id").replace("%proyectoId%", proyectoId), (String) null);   
            return true;
        }

        Pais pais = proyectoFinal.getPais();
        if (!permissionManager.isManager(commandPlayer, pais)) {
            PlayerLogger.warn(commandPlayer, lang.getString("not-a-manager-country").replace("%pais%", pais.getNombrePublico()), (String) null);   
            return true;
        }

        if (InteractionRegistry.getInstance().findRedefineRequest(proyectoId) != null) {
            PlayerLogger.warn(bukkitPlayer, lang.getString("project-redefine-request-already").replace("%proyectoId%", proyectoId), (String) null);
            return true;
        }

        if (proyectoFinal.getEstado() != Estado.ACTIVO && proyectoFinal.getEstado() != Estado.EDITANDO) {
            PlayerLogger.warn(commandPlayer, lang.getString("not-a-active-editing-project").replace("%proyectoId%", proyectoId), (String) null);   
            return true;
        }

        Polygon regionPolygon = RegionUtils.getPolygon(sender);
        if (regionPolygon == null) return true;

        Double tamaño = regionPolygon.getArea();
        TipoProyecto tipoProyecto = TipoProyectoRegistry.getInstance().get(tamaño);
        if (tipoProyecto == null) {
            PlayerLogger.error(bukkitPlayer, lang.getString("invalid-project-size"), (String) null);
            return true;
        }

        PaisRegistry paisr = PaisRegistry.getInstance();
        Division division = paisr.findDivisionByPolygon(regionPolygon, paisr.findByPolygon(regionPolygon));
        if (division == null) {
            PlayerLogger.error(bukkitPlayer, lang.getString("invalid-project-location"), (String) null);
            return true;
        }

        final String proyectoIdFinal = proyectoFinal.getId();
        confirmationMenu = new ConfirmationMenu(lang.getString("gui-titles.redefine-project-confirm").replace("%proyectoId%", proyectoIdFinal), bukkitPlayer, confirmClick -> {
            confirmClick.getWhoClicked().closeInventory();
            if (!ProjectManager.getInstance().createRedefineRequest(proyectoIdFinal, regionPolygon, commandPlayer.getUuid(), tipoProyecto, division)) {
                PlayerLogger.error(commandPlayer, lang.getString("internal-error"), (String) null);
                return;
            }
            PlayerLogger.info(bukkitPlayer, lang.getString("project-redefine-request-success").replace("%proyectoId%", proyectoIdFinal), (String) null);
        }, (cancelClick -> {    
            cancelClick.getWhoClicked().closeInventory();
        }));
        confirmationMenu.open();
        return true;
    }

}
