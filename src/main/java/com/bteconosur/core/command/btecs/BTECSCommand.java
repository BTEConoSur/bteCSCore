package com.bteconosur.core.command.btecs;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.command.btecs.test.BTECSTestCommand;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.model.Player;

import org.bukkit.command.CommandSender;

public class BTECSCommand extends BaseCommand {

    public BTECSCommand() {
        super("btecs", "Comando principal de BTE Cono Sur", null);
        this.addSubcommand(new BTECSReloadCommand());
        this.addSubcommand(new BTECSTestCommand());
        this.addSubcommand(new BTECSCheckSyncProyectos());
        this.addSubcommand(new GenericHelpCommand(this));
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player player = Player.getBTECSPlayer((org.bukkit.entity.Player) sender);
        Language language = player.getLanguage();
        String message = LanguageHandler.getText(language, "help-command-usage").replace("%comando%", getFullCommand());
        PlayerLogger.info(sender, message, (String) null);
        return true;
    }
}
