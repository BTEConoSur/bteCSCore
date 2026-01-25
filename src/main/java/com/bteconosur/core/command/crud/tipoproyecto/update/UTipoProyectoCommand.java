package com.bteconosur.core.command.crud.tipoproyecto.update;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.util.PlayerLogger;

public class UTipoProyectoCommand extends BaseCommand {

    private final YamlConfiguration lang;

    public UTipoProyectoCommand() {
        super("update", "Actualizar propiedad de un TipoProyecto.", "<propiedad> <id> <valor>", CommandMode.BOTH);
        this.addSubcommand(new UTipoProyectoNombreCommand());
        this.addSubcommand(new UTipoProyectoMaxMiembrosCommand());
        this.addSubcommand(new UTipoProyectoTamañoMinCommand());
        this.addSubcommand(new UTipoProyectoTamañoMaxCommand());
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
