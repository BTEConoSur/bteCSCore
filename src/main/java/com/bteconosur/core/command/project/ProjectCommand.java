package com.bteconosur.core.command.project;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.command.project.admin.ProjectAdminCommand;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.util.PlayerLogger;

public class ProjectCommand extends BaseCommand {
    
    private final YamlConfiguration lang;

    public ProjectCommand() {
        super("project", "Comando principal de los Proyectos.", null, "btecs.command.project", CommandMode.BOTH);
        this.addSubcommand(new ProjectPromoteCommand());
        this.addSubcommand(new ProjectCreateCommand());
        this.addSubcommand(new ProjectFinishCommand());
        this.addSubcommand(new ProjectAddMemberCommand());
        this.addSubcommand(new ProjectRemoveMemberCommand());
        this.addSubcommand(new ProjectAdminCommand());
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
