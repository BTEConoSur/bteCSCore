package com.bteconosur.core.command.btecs.crud.tipousuario;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.command.btecs.crud.tipousuario.update.UTipoUsuarioCommand;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.util.PlayerLogger;

public class CRUDTipoUsuarioCommand extends BaseCommand {

    private final YamlConfiguration lang;

    public CRUDTipoUsuarioCommand() {
        super("tipousuario", "Realizar operaciones CRUD sobre tipos de usuario.", null, CommandMode.BOTH);
        this.addSubcommand(new CTipoUsuarioCommand());
        this.addSubcommand(new RTipoUsuarioCommand());
        this.addSubcommand(new UTipoUsuarioCommand());
        this.addSubcommand(new DTipoUsuarioCommand());
        this.addSubcommand(new GetListTipoUsuarioCommand());
        this.addSubcommand(new GetTipoUsuarioListPermisosCommand());
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
