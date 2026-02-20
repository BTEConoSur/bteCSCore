package com.bteconosur.core.command.manager;

import org.bukkit.command.CommandSender;
import org.locationtech.jts.geom.Polygon;

import com.bteconosur.core.ProjectManager;
import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
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

    private ConfirmationMenu confirmationMenu;

    public ManagerRedefineCommand() {
        super("redefine", "Redefinir un proyecto de un país.", "<id_proyecto>", CommandMode.PLAYER_ONLY);
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
        PermissionManager permissionManager = PermissionManager.getInstance();
        ProyectoRegistry pr = ProyectoRegistry.getInstance();
        if (!LinkService.isPlayerLinked(commandPlayer)) {
            PlayerLogger.warn(commandPlayer, LanguageHandler.getText(language, "link.mc-link-recomendation"), (String) null);
        }

        String proyectoId = args[0];
        Proyecto proyectoFinal = pr.get(proyectoId);
        if (proyectoFinal == null) {
            PlayerLogger.warn(commandPlayer, LanguageHandler.replaceMC("project.not-found-id", language, proyectoFinal), (String) null);   
            return true;
        }

        Pais pais = proyectoFinal.getPais();
        if (!permissionManager.isManager(commandPlayer, pais)) {
            PlayerLogger.warn(commandPlayer, LanguageHandler.replaceMC("manager.not-manager-country", language, pais), (String) null);   
            return true;
        }

        if (InteractionRegistry.getInstance().findRedefineRequest(proyectoId) != null) {
            PlayerLogger.warn(bukkitPlayer, LanguageHandler.replaceMC("project.redefine.request.already", language, proyectoFinal), (String) null);
            return true;
        }

        if (proyectoFinal.getEstado() != Estado.ACTIVO && proyectoFinal.getEstado() != Estado.EDITANDO) {
            PlayerLogger.warn(commandPlayer, LanguageHandler.replaceMC("project.not-active-editing", language, proyectoFinal), (String) null);   
            return true;
        }

        Polygon regionPolygon = RegionUtils.getPolygon(sender);
        if (regionPolygon == null) return true;

        Double tamaño = regionPolygon.getArea();
        TipoProyecto tipoProyecto = TipoProyectoRegistry.getInstance().get(tamaño);
        if (tipoProyecto == null) {
            PlayerLogger.error(bukkitPlayer, LanguageHandler.getText(language, "invalid-project-size"), (String) null);
            return true;
        }

        PaisRegistry paisr = PaisRegistry.getInstance();
        Division division = paisr.findDivisionByPolygon(regionPolygon, paisr.findByPolygon(regionPolygon));
        if (division == null) {
            PlayerLogger.error(bukkitPlayer, LanguageHandler.getText(language, "invalid-project-location"), (String) null);
            return true;
        }

        final String proyectoIdFinal = proyectoFinal.getId();
        String title = LanguageHandler.replaceMC("gui-titles.redefine-project-confirm", language, proyectoFinal);
        confirmationMenu = new ConfirmationMenu(title, bukkitPlayer, confirmClick -> {
            confirmClick.getWhoClicked().closeInventory();
            if (!ProjectManager.getInstance().createRedefineRequest(proyectoIdFinal, regionPolygon, commandPlayer.getUuid(), tipoProyecto, division)) {
                PlayerLogger.error(commandPlayer, LanguageHandler.getText(language, "internal-error"), (String) null);
                return;
            }
            PlayerLogger.info(bukkitPlayer, LanguageHandler.replaceMC("project.redefine.request.success", language, proyectoFinal), (String) null);
        }, (cancelClick -> {    
            cancelClick.getWhoClicked().closeInventory();
        }));
        confirmationMenu.open();
        return true;
    }

}
