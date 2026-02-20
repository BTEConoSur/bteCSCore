package com.bteconosur.core.command.crud.rangousuario;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.command.crud.rangousuario.update.URangoUsuarioCommand;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.model.Player;

public class CRUDRangoUsuarioCommand extends BaseCommand {

    public CRUDRangoUsuarioCommand() {
        super("rangousuario", "Realizar operaciones CRUD sobre rangos de usuario.", null, CommandMode.BOTH);
        this.addSubcommand(new CRangoUsuarioCommand());
        this.addSubcommand(new RRangoUsuarioCommand());
        this.addSubcommand(new URangoUsuarioCommand());
        this.addSubcommand(new DRangoUsuarioCommand());
        this.addSubcommand(new GetListRangoUsuarioCommand());
        this.addSubcommand(new GetRangoUsuarioListPermisosCommand());
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
