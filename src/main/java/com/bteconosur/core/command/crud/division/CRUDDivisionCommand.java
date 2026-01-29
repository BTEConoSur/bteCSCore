package com.bteconosur.core.command.crud.division;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.command.crud.division.update.UDivisionCommand;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.util.PlayerLogger;

public class CRUDDivisionCommand extends BaseCommand {

    private final YamlConfiguration lang;

    public CRUDDivisionCommand() {
        super("division", "Realizar operaciones CRUD sobre divisiones.", null, CommandMode.BOTH);
        this.addSubcommand(new CDivisionCommand());
        this.addSubcommand(new RDivisionCommand());
        this.addSubcommand(new UDivisionCommand());
        this.addSubcommand(new DDivisionCommand());
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
