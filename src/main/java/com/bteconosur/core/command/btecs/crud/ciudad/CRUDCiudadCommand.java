package com.bteconosur.core.command.btecs.crud.ciudad;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.command.btecs.crud.ciudad.update.UCiudadCommand;
import com.bteconosur.core.config.ConfigHandler;

public class CRUDCiudadCommand extends BaseCommand {

    private final YamlConfiguration lang;

    public CRUDCiudadCommand() {
        super("ciudad", "Realizar operaciones CRUD sobre ciudades.", null, CommandMode.BOTH);
        this.addSubcommand(new CCiudadCommand());
        this.addSubcommand(new RCiudadCommand());
        this.addSubcommand(new UCiudadCommand());
        this.addSubcommand(new DCiudadCommand());
        this.addSubcommand(new GenericHelpCommand(this));

        ConfigHandler configHandler = ConfigHandler.getInstance();
        lang = configHandler.getLang();
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        String message = lang.getString("help-command-usage").replace("%command%", getFullCommand());
        sender.sendMessage(message);
        return true;
    }

}
