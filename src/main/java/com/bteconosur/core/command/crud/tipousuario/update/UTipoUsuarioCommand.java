package com.bteconosur.core.command.crud.tipousuario.update;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.util.PlayerLogger;

public class UTipoUsuarioCommand extends BaseCommand {

    private final YamlConfiguration lang;

    public UTipoUsuarioCommand() {
        super("update", "Actualizar propiedad de un TipoUsuario.", "<propiedad> <id> <valor>", CommandMode.BOTH);
        this.addSubcommand(new UTipoUsuarioNombreCommand());
        this.addSubcommand(new UTipoUsuarioDescripcionCommand());
        this.addSubcommand(new UTipoUsuarioCantProyecSimCommand());
        this.addSubcommand(new UTipoUsuarioAddPermisoCommand());
        this.addSubcommand(new UTipoUsuarioRemovePermisoCommand());
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
