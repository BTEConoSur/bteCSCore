package com.bteconosur.core.command.crud.pais.update;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.model.Player;

public class UPaisCommand extends BaseCommand {

    public UPaisCommand() {
        super("update", "<propiedad> <id> <valor>", "btecs.command.crud", CommandMode.BOTH);
        this.addSubcommand(new UPaisNombreCommand());
        this.addSubcommand(new UPaisDsIdGuildCommand());
        this.addSubcommand(new UPaisDsIdGlobalChatCommand());
        this.addSubcommand(new UPaisDsIdCountryChatCommand());
        this.addSubcommand(new UPaisDsIdLogCommand());
        this.addSubcommand(new UPaisDsIdRequestCommand());
        this.addSubcommand(new UPaisNombrePublicoCommand());
        this.addSubcommand(new AddPaisRegionGeojsonCommand());
        this.addSubcommand(new AddDivisionesGeojsonCommand());
        this.addSubcommand(new RemovePaisRegionCommand());
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
