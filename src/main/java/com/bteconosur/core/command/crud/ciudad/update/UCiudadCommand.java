package com.bteconosur.core.command.crud.ciudad.update;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.util.PlayerLogger;

public class UCiudadCommand extends BaseCommand {

    private final YamlConfiguration lang;

    public UCiudadCommand() {
        super("update", "Actualizar atributos de una Ciudad.", null, CommandMode.BOTH);
        this.addSubcommand(new UCiudadNombreCommand());
        this.addSubcommand(new UpdateCiudadPoligonoCommand());
        this.addSubcommand(new UCiudadNombrePublicoCommand());
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
