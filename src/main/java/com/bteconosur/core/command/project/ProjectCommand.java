package com.bteconosur.core.command.project;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.command.project.admin.ProjectAdminCommand;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PlayerRegistry;

public class ProjectCommand extends BaseCommand {
    
    public ProjectCommand() {
        super("project", "Comando principal de los Proyectos.", null, "btecs.command.project", CommandMode.BOTH);
        this.addSubcommand(new ProjectCreateCommand());
        this.addSubcommand(new ProjectRedefineCommand());
        this.addSubcommand(new ProjectFinishCommand());
        this.addSubcommand(new ProjectJoinCommand());
        this.addSubcommand(new ProjectLeaveCommand());
        this.addSubcommand(new ProjectAcceptCommand());
        this.addSubcommand(new ProjectAddMemberCommand());
        this.addSubcommand(new ProjectTransferCommand());
        this.addSubcommand(new ProjectRemoveMemberCommand());
        this.addSubcommand(new ProjectEditCommand());
        this.addSubcommand(new ProjectNameCommand());
        this.addSubcommand(new ProjectDescriptionCommand());
        this.addSubcommand(new ProjectAdminCommand());
        this.addSubcommand(new ProjectManageCommand());
        this.addSubcommand(new ProjectInfoCommand());
        this.addSubcommand(new ProjectClaimCommand());
        this.addSubcommand(new ProjectSelectCommand());
        this.addSubcommand(new ProjectBorderCommand());
        this.addSubcommand(new GenericHelpCommand(this));
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player commandPlayer = PlayerRegistry.getInstance().get(sender);
        String message = LanguageHandler.getText(commandPlayer.getLanguage(), "help-command-usage").replace("%comando%", getFullCommand().replace(" " + command, ""));
        PlayerLogger.info(sender, message, (String) null);
        return true;
    }
}
