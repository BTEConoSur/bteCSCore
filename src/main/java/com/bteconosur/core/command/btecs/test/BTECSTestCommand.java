package com.bteconosur.core.command.btecs.test;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.config.ConfigHandler;

public class BTECSTestCommand extends BaseCommand {

    private final YamlConfiguration lang;

    public BTECSTestCommand() {
        super("test", "Para testear cosas.", null, CommandMode.PLAYER_ONLY);
        this.addSubcommand(new TestGenericCommand());
        this.addSubcommand(new TestConsoleLoggerCommand());
        this.addSubcommand(new GenericHelpCommand(this));
        ConfigHandler configHandler = ConfigHandler.getInstance();
        lang = configHandler.getLang();
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        // TODO: Enviar por sistema de notificaciones que use help
        String message = lang.getString("help-command-usage");
        sender.sendMessage(message);
        return true;
    }
}
