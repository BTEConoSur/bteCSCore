package com.bteconosur.core.command.reviewer;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.command.reviewer.review.ReviewerReviewCommand;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.PermissionManager;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PlayerRegistry;

public class ReviewerCommand extends BaseCommand {

    public ReviewerCommand() {
        super("reviewer", "Comando para Reviewer de los proyectos.", null, "btecs.command.reviewer", CommandMode.PLAYER_ONLY);
        this.addSubcommand(new ReviewerConfigCommand());
        this.addSubcommand(new ReviewerReviewCommand());
        this.addSubcommand(new ProjectPromoteCommand());
        this.addSubcommand(new GenericHelpCommand(this));
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player commandPlayer = PlayerRegistry.getInstance().get(sender);
        String message = LanguageHandler.getText(commandPlayer.getLanguage(), "help-command-usage").replace("%comando%", getFullCommand());
        PlayerLogger.info(sender, message, (String) null);
        return true;
    }

    @Override
    protected boolean customPermissionCheck(CommandSender sender) {
        Player commandPlayer = PlayerRegistry.getInstance().get(((org.bukkit.entity.Player) sender).getUniqueId());
        return PermissionManager.getInstance().isReviewer(commandPlayer);
    }

}
