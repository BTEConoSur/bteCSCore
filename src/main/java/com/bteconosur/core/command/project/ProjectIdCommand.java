package com.bteconosur.core.command.project;

import java.util.Set;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.menu.project.ProjectListMenu;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.core.util.TagResolverUtils;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.Proyecto;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.db.registry.ProyectoRegistry;

import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

public class ProjectIdCommand extends BaseCommand {

    private ProjectListMenu projectListMenu;

    public ProjectIdCommand() {
        super("id", "", "btecs.command.project.id", CommandMode.PLAYER_ONLY);
        this.addSubcommand(new GenericHelpCommand(this));
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player commandPlayer = PlayerRegistry.getInstance().get(sender);
        Language language = commandPlayer.getLanguage();
        if (args.length >= 1) {
            String message = LanguageHandler.getText(language, "help-command-usage").replace("%comando%", getFullCommand());
            PlayerLogger.info(sender, message, (String) null);
            return true;
        }
        org.bukkit.entity.Player bukkitPlayer = (org.bukkit.entity.Player) sender;
        Proyecto proyectoFinal = null;
      
        Set<Proyecto> proyectos = ProyectoRegistry.getInstance().getByLocation(bukkitPlayer.getLocation().getBlockX(), bukkitPlayer.getLocation().getBlockZ());
        if (proyectos.isEmpty()) {
            PlayerLogger.error(commandPlayer, LanguageHandler.getText(language, "project.not-found-here"), (String) null);
            return true;
        }
        if (proyectos.size() > 1) {
            projectListMenu = new ProjectListMenu(commandPlayer, LanguageHandler.replaceMC("gui-titles.proyectos-here-list", language, proyectoFinal), proyectos, (proyecto, event) -> {
                TagResolver tagResolver1 = TagResolverUtils.getCopyableText("id", proyecto.getId(), proyecto.getId(), language);
                PlayerLogger.info(sender, LanguageHandler.getText(language, "project.id"), (String) null, tagResolver1);
                event.getWhoClicked().closeInventory();
            });
            projectListMenu.open();
            return true;
        }
        proyectoFinal = proyectos.iterator().next();
        

        TagResolver tagResolver1 = TagResolverUtils.getCopyableText("id", proyectoFinal.getId(), proyectoFinal.getId(), language);
        PlayerLogger.info(sender, LanguageHandler.getText(language, "project.id"), (String) null, tagResolver1);
        return true;
    }

}
