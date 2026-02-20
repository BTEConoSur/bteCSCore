package com.bteconosur.core.command.btecs.test;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.model.Player;

public class BTECSTestCommand extends BaseCommand {

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
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player player = Player.getBTECSPlayer((org.bukkit.entity.Player) sender);
        Language language = player.getLanguage();
        String message = LanguageHandler.getText(language, "help-command-usage").replace("%comando%", getFullCommand());
        PlayerLogger.info(sender, message, (String) null);
        return true;
    }
}
