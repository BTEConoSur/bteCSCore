package com.bteconosur.core.command.crud.player.update;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.model.Player;

public class UPlayerCommand extends BaseCommand {

    public UPlayerCommand() {
        super("update", "Actualizar propiedad de un Player.", "<propiedad> <uuid> <valor>", CommandMode.BOTH);
        this.addSubcommand(new UPlayerIdCommand());
        this.addSubcommand(new UPlayerNombreCommand());
        this.addSubcommand(new UPlayerNombrePublicoCommand());
        this.addSubcommand(new UPlayerTipoUsuarioCommand());    
        this.addSubcommand(new URangoUsuarioCommand());
        this.addSubcommand(new UPlayerFechaIngresoCommand());
        this.addSubcommand(new UPlayerFechaUltimaConexionCommand());
        this.addSubcommand(new GenericHelpCommand(this));
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player commandPlayer = Player.getBTECSPlayer((org.bukkit.entity.Player) sender);
        Language language = commandPlayer.getLanguage();
        String message = LanguageHandler.getText(language, "help-command-usage").replace("%comando%", getFullCommand());
        PlayerLogger.info(sender, message, (String) null);
        return true;
    }
}