package com.bteconosur.core.command.project.admin;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.util.PlayerLogger;

public class ProjectAdminCommand extends BaseCommand {

    private final YamlConfiguration lang;

    public ProjectAdminCommand() {
        super("admin", "Comando para Admin de los proyectos.", null, "btecs.command.project.admin", CommandMode.PLAYER_ONLY);
        this.addSubcommand(new ProjectAdminAddManager());
        this.addSubcommand(new ProjectAdminRemoveManager());
        this.addSubcommand(new ProjectAdminAddReviewer());
        this.addSubcommand(new ProjectAdminRemoveReviewer());
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
