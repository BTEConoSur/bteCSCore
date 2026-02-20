package com.bteconosur.core.command.crud.player;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.command.crud.player.update.UPlayerCommand;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.model.Player;

public class CRUDPlayerCommand extends BaseCommand {

    public CRUDPlayerCommand() {
        super("player", "Realizar operaciones CRUD sobre jugadores.", null, CommandMode.BOTH);
        this.addSubcommand(new CPlayerCommand());
        this.addSubcommand(new RPlayerCommand());
        this.addSubcommand(new UPlayerCommand());
        this.addSubcommand(new DPlayerCommand());
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
