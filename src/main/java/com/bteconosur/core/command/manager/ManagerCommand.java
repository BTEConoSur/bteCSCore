package com.bteconosur.core.command.manager;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;

import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.PermissionManager;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PlayerRegistry;

public class ManagerCommand extends BaseCommand {

    private final YamlConfiguration lang;

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
        this.addSubcommand(new GenericHelpCommand(this));
        // TODO: Comando para eliminar lider,
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
        return PermissionManager.getInstance().isManager(commandPlayer);
    }

}
