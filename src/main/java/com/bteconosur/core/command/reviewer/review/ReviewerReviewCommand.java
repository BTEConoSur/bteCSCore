package com.bteconosur.core.command.reviewer.review;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.util.PlayerLogger;

public class ReviewerReviewCommand extends BaseCommand {

    private final YamlConfiguration lang;

    public ReviewerReviewCommand() {
        super("review", "Revisar proyectos.", null, CommandMode.PLAYER_ONLY);
        this.addSubcommand(new ReviewAcceptCommand());
        this.addSubcommand(new ReviewRejectCommand());
        this.addSubcommand(new GenericHelpCommand(this));
        
        ConfigHandler configHandler = ConfigHandler.getInstance();
        lang = configHandler.getLang();
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        String message = lang.getString("help-command-usage").replace("%command%", getFullCommand());
        PlayerLogger.info(sender, message, (String) null);
        return true;
    }

}
