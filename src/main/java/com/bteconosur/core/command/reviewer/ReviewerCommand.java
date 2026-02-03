package com.bteconosur.core.command.reviewer;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.command.reviewer.review.ReviewerReviewCommand;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.PermissionManager;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PlayerRegistry;

public class ReviewerCommand extends BaseCommand {

    private final YamlConfiguration lang;

    public ReviewerCommand() {
        super("reviewer", "Comando para Reviewer de los proyectos.", null, "btecs.command.reviewer", CommandMode.PLAYER_ONLY);
        this.addSubcommand(new ReviewerConfigCommand());
        this.addSubcommand(new ReviewerReviewCommand());
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

    @Override
    protected boolean customPermissionCheck(CommandSender sender) {
        Player commandPlayer = PlayerRegistry.getInstance().get(((org.bukkit.entity.Player) sender).getUniqueId());
        return PermissionManager.getInstance().isReviewer(commandPlayer);
    }

}
