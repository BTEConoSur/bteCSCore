package com.bteconosur.core.command.reviewer;

import java.util.UUID;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.menu.project.ProjectPromoteMenu;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.PermissionManager;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PlayerRegistry;

public class ProjectPromoteCommand extends BaseCommand {

    public ProjectPromoteCommand() { // TODO: Ponerlo en comando reviewer.
        super("promote", "Cambiar el Tipo de Usuario de un jugador.", "<nombre>|<uuid>", "btecs.command.project.promote", CommandMode.PLAYER_ONLY);
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

        UUID uuid;
        Player targetPlayer;
        PlayerRegistry playerRegistry = PlayerRegistry.getInstance();

        if (!PermissionManager.getInstance().isReviewer(commandPlayer)) {
            PlayerLogger.warn(commandPlayer, LanguageHandler.getText(language, "reviewer.not-a-reviewer-country"), (String) null);
            return true;
        }

        try{
            uuid = UUID.fromString(args[0]);
            targetPlayer = playerRegistry.get(uuid);
        } catch (IllegalArgumentException exception){
            targetPlayer = playerRegistry.findByName(args[0]);
        }

        if (targetPlayer == null) {
            String message = LanguageHandler.getText(language, "player-not-registered").replace("%player%", args[0]);
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        String title = LanguageHandler.replaceMC("gui-titles.project-promote", language, targetPlayer);
        ProjectPromoteMenu menu = new ProjectPromoteMenu(commandPlayer, title);
        menu.setBTECSPlayer(targetPlayer);
        menu.open();

        return true;
    }
}
