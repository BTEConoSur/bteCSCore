package com.bteconosur.core.command.btecs.crud.rangousuario;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.command.btecs.crud.rangousuario.update.URangoUsuarioCommand;
import com.bteconosur.core.config.ConfigHandler;

public class CRUDRangoUsuarioCommand extends BaseCommand {

    private final YamlConfiguration lang;

    public CRUDRangoUsuarioCommand() {
        super("rangousuario", "Realizar operaciones CRUD sobre rangos de usuario.", null, CommandMode.BOTH);
        this.addSubcommand(new CRangoUsuarioCommand());
        this.addSubcommand(new RRangoUsuarioCommand());
        this.addSubcommand(new URangoUsuarioCommand());
        this.addSubcommand(new DRangoUsuarioCommand());
        this.addSubcommand(new GetListRangoUsuarioCommand());
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
