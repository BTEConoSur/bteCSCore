package com.bteconosur.core.command.crud.rangousuario.update;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.model.Player;

public class URangoUsuarioCommand extends BaseCommand {

    public URangoUsuarioCommand() {
        super("update", "Actualizar propiedad de un RangoUsuario.", "<propiedad> <id> <valor>", CommandMode.BOTH);
        this.addSubcommand(new URangoUsuarioNombreCommand());
        this.addSubcommand(new URangoUsuarioDescripcionCommand());
        this.addSubcommand(new URangoUsuarioAddPermisoCommand());
        this.addSubcommand(new URangoUsuarioRemovePermisoCommand());
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
