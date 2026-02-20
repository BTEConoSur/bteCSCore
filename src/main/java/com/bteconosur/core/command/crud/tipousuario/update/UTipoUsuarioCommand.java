package com.bteconosur.core.command.crud.tipousuario.update;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.model.Player;

public class UTipoUsuarioCommand extends BaseCommand {

    public UTipoUsuarioCommand() {
        super("update", "Actualizar propiedad de un TipoUsuario.", "<propiedad> <id> <valor>", CommandMode.BOTH);
        this.addSubcommand(new UTipoUsuarioNombreCommand());
        this.addSubcommand(new UTipoUsuarioDescripcionCommand());
        this.addSubcommand(new UTipoUsuarioCantProyecSimCommand());
        this.addSubcommand(new UTipoUsuarioAddPermisoCommand());
        this.addSubcommand(new UTipoUsuarioRemovePermisoCommand());
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
