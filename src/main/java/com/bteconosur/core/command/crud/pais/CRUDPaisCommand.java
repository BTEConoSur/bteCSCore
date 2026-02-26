package com.bteconosur.core.command.crud.pais;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.command.crud.pais.update.UPaisCommand;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.model.Player;

public class CRUDPaisCommand extends BaseCommand {

    public CRUDPaisCommand() {
        super("pais", null, "btecs.command.crud", CommandMode.BOTH);
        this.addSubcommand(new CPaisCommand());
        this.addSubcommand(new RPaisCommand());
        this.addSubcommand(new UPaisCommand());
        this.addSubcommand(new DPaisCommand());
        this.addSubcommand(new GetListPaisCommand());
        this.addSubcommand(new GetListRegionPaisCommand());
        this.addSubcommand(new GenericHelpCommand(this));
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player commandPlayer = null;
        if (sender instanceof org.bukkit.entity.Player) commandPlayer = Player.getBTECSPlayer((org.bukkit.entity.Player) sender);
        Language language = commandPlayer != null ? commandPlayer.getLanguage() : Language.getDefault();
        String message = LanguageHandler.getText(language, "help-command-usage").replace("%comando%", getFullCommand());
        PlayerLogger.info(sender, message, (String) null);
        return true;
    }

}
