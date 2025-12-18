package com.bteconosur.core.command.btecs.crud.player.update;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.config.ConfigHandler;

public class UPlayerCommand extends BaseCommand {

    private final YamlConfiguration lang;

    public UPlayerCommand() {
        super("update", "Actualizar propiedad de un Player.", "<propiedad> <uuid> <valor>", CommandMode.BOTH);
        this.addSubcommand(new UPlayerIdCommand());
        this.addSubcommand(new UPlayerNombreCommand());
        this.addSubcommand(new UPlayerNombrePublicoCommand());
        this.addSubcommand(new UPlayerTipoUsuarioCommand());    
        this.addSubcommand(new URangoUsuarioCommand());
        this.addSubcommand(new UPlayerFechaIngresoCommand());
        this.addSubcommand(new UPlayerFechaUltimaConexionCommand());
        this.addSubcommand(new UPlayerDsIdUsuarioCommand());
        this.addSubcommand(new GenericHelpCommand(this));

        ConfigHandler configHandler = ConfigHandler.getInstance();
        lang = configHandler.getLang();
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        // TODO: Enviar por sistema de notificaciones que use help
        String message = lang.getString("help-command-usage").replace("%command%", getFullCommand());
        sender.sendMessage(message);
        return true;
    }
}