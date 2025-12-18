package com.bteconosur.core.command.btecs;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.command.btecs.crud.BTECSCrudCommand;
import com.bteconosur.core.command.btecs.test.BTECSTestCommand;
import com.bteconosur.core.config.ConfigHandler;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

public class BTECSCommand extends BaseCommand {

    private final YamlConfiguration lang;

    public BTECSCommand() {
        super("btecs", "Comando principal de BTE Cono Sur", null);
        this.addSubcommand(new BTECSReloadCommand());
        this.addSubcommand(new BTECSTestCommand());
        this.addSubcommand(new GenericHelpCommand(this));
        this.addSubcommand(new BTECSCrudCommand());

        ConfigHandler configHandler = ConfigHandler.getInstance();
        lang = configHandler.getLang();
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        // TODO: Enviar por sistema de notificaciones que use help
        String message = lang.getString("help-command-usage").replace("%command%", getFullCommand());
        sender.sendMessage(message);
        return true;
    }
}
