package com.bteconosur.core.command.crud.division.update;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.util.PlayerLogger;

public class UDivisionCommand extends BaseCommand {

    private final YamlConfiguration lang;

    public UDivisionCommand() {
        super("update", "Actualizar atributos de una Division.", null, CommandMode.BOTH);
        this.addSubcommand(new UDivisionNombreCommand());
        this.addSubcommand(new UDivisionNamCommand());
        this.addSubcommand(new UDivisionGnaCommand());
        this.addSubcommand(new UDivisionFnaCommand());
        this.addSubcommand(new UDivisionPaisCommand());
        this.addSubcommand(new UDivisionFnaCommand());
        this.addSubcommand(new AddDivisionRegionGeojsonCommand());
        this.addSubcommand(new RemoveDivisionRegionCommand());
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
