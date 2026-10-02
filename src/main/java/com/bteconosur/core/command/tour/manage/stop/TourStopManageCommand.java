package com.bteconosur.core.command.tour.manage.stop;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.PermissionManager;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PlayerRegistry;

public class TourStopManageCommand extends BaseCommand {

    public TourStopManageCommand() {
        super("stopmanage", null, "btecs.command.tour", CommandMode.PLAYER_ONLY);
        this.addSubcommand(new TourStopRemoveCommand());
        this.addSubcommand(new TourStopEditCommand());
        this.addSubcommand(new TourStopGetCommand());
        this.addSubcommand(new TourAddStopCommand());
        this.addSubcommand(new TourStopTpCommand());
        this.addSubcommand(new TourStopSelectCommand());
        this.addSubcommand(new GenericHelpCommand(this));
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player player = PlayerRegistry.getInstance().get(sender);
        Language language = player != null ? player.getLanguage() : Language.getDefault();
        String message = LanguageHandler.getText(language, "help-command-usage").replace("%comando%", getFullCommand());
        PlayerLogger.info(sender, message, (String) null);
        return true;
    }

    @Override
    protected boolean customPermissionCheck(CommandSender sender) {
        Player commandPlayer = PlayerRegistry.getInstance().get(((org.bukkit.entity.Player) sender).getUniqueId());
        PermissionManager pm = PermissionManager.getInstance();
        return pm.isManager(commandPlayer) || pm.isAdmin(commandPlayer);
    }

}
