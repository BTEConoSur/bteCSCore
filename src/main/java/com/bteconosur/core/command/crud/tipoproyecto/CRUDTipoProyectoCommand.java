package com.bteconosur.core.command.crud.tipoproyecto;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.command.crud.tipoproyecto.update.UTipoProyectoCommand;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.util.PlayerLogger;

public class CRUDTipoProyectoCommand extends BaseCommand {

    private final YamlConfiguration lang;

    public CRUDTipoProyectoCommand() {
        super("tipoproyecto", "Realizar operaciones CRUD sobre tipos de proyecto.", null, CommandMode.BOTH);
        this.addSubcommand(new CTipoProyectoCommand());
        this.addSubcommand(new RTipoProyectoCommand());
        this.addSubcommand(new UTipoProyectoCommand());
        this.addSubcommand(new DTipoProyectoCommand());
        this.addSubcommand(new GetListTipoProyectoCommand());
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
