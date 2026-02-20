package com.bteconosur.core.command.crud.tipousuario;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.command.crud.tipousuario.update.UTipoUsuarioCommand;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.model.Player;

public class CRUDTipoUsuarioCommand extends BaseCommand {

    public CRUDTipoUsuarioCommand() {
        super("tipousuario", "Realizar operaciones CRUD sobre tipos de usuario.", null, CommandMode.BOTH);
        this.addSubcommand(new CTipoUsuarioCommand());
        this.addSubcommand(new RTipoUsuarioCommand());
        this.addSubcommand(new UTipoUsuarioCommand());
        this.addSubcommand(new DTipoUsuarioCommand());
        this.addSubcommand(new GetListTipoUsuarioCommand());
        this.addSubcommand(new GetTipoUsuarioListPermisosCommand());
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
