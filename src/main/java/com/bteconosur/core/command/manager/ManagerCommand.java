package com.bteconosur.core.command.manager;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.PermissionManager;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PlayerRegistry;

public class ManagerCommand extends BaseCommand {

    public ManagerCommand() {
        super("manager", "Comando para Manager de los proyectos.", null, "btecs.command.manager", CommandMode.PLAYER_ONLY);
        this.addSubcommand(new ManagerAddReviewerCommand());
        this.addSubcommand(new ManagerRemoveReviewerCommand());
        this.addSubcommand(new ManagerConfigCommand());
        this.addSubcommand(new ManagerFinishCommand());
        this.addSubcommand(new ManagerRedefineCommand());
        this.addSubcommand(new ManagerAddMemberCommand());
        this.addSubcommand(new ManagerTransferCommand());
        this.addSubcommand(new ManagerEditCommand());
        this.addSubcommand(new ManagerRemoveMemberCommand());
        this.addSubcommand(new ManagerRemoveLeaderCommand());
        this.addSubcommand(new ManagerDeleteCommand());
        this.addSubcommand(new ManagerAcceptCommand());
        this.addSubcommand(new ManagerNameCommand());
        this.addSubcommand(new ManagerDescriptionCommand());
        this.addSubcommand(new ManagerManageCommand());
        this.addSubcommand(new ManagerSelectCommand());
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
        return PermissionManager.getInstance().isManager(commandPlayer);
    }

}
