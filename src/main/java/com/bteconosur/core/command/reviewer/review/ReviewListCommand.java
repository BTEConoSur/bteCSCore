package com.bteconosur.core.command.reviewer.review;

import java.util.Set;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.menu.project.ProjectInfoMenu;
import com.bteconosur.core.menu.project.ProjectListMenu;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.PermissionManager;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.Proyecto;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.db.registry.ProyectoRegistry;

public class ReviewListCommand extends BaseCommand {

	private ProjectListMenu projectListMenu;

	public ReviewListCommand() {
		super("list", null, "btecs.command.reviewer", CommandMode.PLAYER_ONLY);
	}

	@Override
	protected boolean onCommand(CommandSender sender, String[] args) {
		Player commandPlayer = PlayerRegistry.getInstance().get(sender);
		Language language = commandPlayer.getLanguage();
		if (args.length > 0) {
			String message = LanguageHandler.getText(language, "help-command-usage").replace("%comando%", getFullCommand());
			PlayerLogger.info(sender, message, (String) null);
			return true;
		}

		ProyectoRegistry proyectoRegistry = ProyectoRegistry.getInstance();
		PermissionManager permissionManager = PermissionManager.getInstance();
		Set<Proyecto> finishingProyectos = proyectoRegistry.getFinishing();
		finishingProyectos.removeIf(proyecto -> !permissionManager.isReviewer(commandPlayer, proyecto.getPais()));

		if (finishingProyectos.isEmpty()) {
			PlayerLogger.error(commandPlayer, LanguageHandler.getText(language, "project.not-finishing"), (String) null);
			return true;
		}

		projectListMenu = new ProjectListMenu(commandPlayer, LanguageHandler.getText(language, "gui-titles.proyectos-finishing-list"), finishingProyectos, (proyecto, event) -> {
			new ProjectInfoMenu(commandPlayer, proyecto, projectListMenu, LanguageHandler.replaceMC("gui-titles.project-info", language, proyecto)).open();
		});
		projectListMenu.open();
		return true;
	}

	@Override
	protected boolean customPermissionCheck(CommandSender sender) {
		Player commandPlayer = PlayerRegistry.getInstance().get(((org.bukkit.entity.Player) sender).getUniqueId());
		return PermissionManager.getInstance().isReviewer(commandPlayer);
	}

}
