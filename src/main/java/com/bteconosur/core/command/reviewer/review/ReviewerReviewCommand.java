package com.bteconosur.core.command.reviewer.review;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PlayerRegistry;

public class ReviewerReviewCommand extends BaseCommand {


    public ReviewerReviewCommand() {
        super("review", "Revisar proyectos.", null, CommandMode.PLAYER_ONLY);
        this.addSubcommand(new ReviewAcceptCommand());
        this.addSubcommand(new ReviewRejectCommand());
        this.addSubcommand(new GenericHelpCommand(this));
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player commandPlayer = PlayerRegistry.getInstance().get(sender);
        String message = LanguageHandler.getText(commandPlayer.getLanguage(), "help-command-usage").replace("%comando%", getFullCommand());
        PlayerLogger.info(sender, message, (String) null);
        return true;
    }

}
