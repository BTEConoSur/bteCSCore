package com.bteconosur.core.command.btecs.crud.rangousuario.update;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.util.PlayerLogger;

public class URangoUsuarioCommand extends BaseCommand {

    private final YamlConfiguration lang;

    public URangoUsuarioCommand() {
        super("update", "Actualizar propiedad de un RangoUsuario.", "<propiedad> <id> <valor>", CommandMode.BOTH);
        this.addSubcommand(new URangoUsuarioNombreCommand());
        this.addSubcommand(new URangoUsuarioDescripcionCommand());
        this.addSubcommand(new URangoUsuarioAddPermisoCommand());
        this.addSubcommand(new URangoUsuarioRemovePermisoCommand());
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
