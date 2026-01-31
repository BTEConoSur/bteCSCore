package com.bteconosur.core.command.btecs.test;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.util.PlayerLogger;

public class BTECSTestCommand extends BaseCommand {

    private final YamlConfiguration lang;

    public BTECSTestCommand() {
        super("test", "Para testear cosas.", null, CommandMode.BOTH);
        this.addSubcommand(new TestGenericCommand());
        this.addSubcommand(new TestConsoleLoggerCommand());
        this.addSubcommand(new TestSimpleMenuCommand());
        this.addSubcommand(new TestPaginatedMenuCommand());
        this.addSubcommand(new TestDiscordLogger());
        this.addSubcommand(new TestPlayerLoggerCommand());
        this.addSubcommand(new TestRegionPaisGeojsonCommand());
        this.addSubcommand(new TestRegionDivisionGeojsonCommand());
        this.addSubcommand(new TestAllRegionPaisGeojsonCommand());
        this.addSubcommand(new TestAllRegionDivisionGeojsonCommand());
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
