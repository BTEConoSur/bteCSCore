package com.bteconosur.core.command.crud.division.update;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.model.Player;

public class UDivisionCommand extends BaseCommand {

    public UDivisionCommand() {
        super("update", "Actualizar atributos de una Division.", null, CommandMode.BOTH);
        this.addSubcommand(new UDivisionNombreCommand());
        this.addSubcommand(new UDivisionNamCommand());
        this.addSubcommand(new UDivisionGnaCommand());
        this.addSubcommand(new UDivisionFnaCommand());
        this.addSubcommand(new UDivisionPaisCommand());
        this.addSubcommand(new UDivisionFnaCommand());
        this.addSubcommand(new AddDivisionRegionGeojsonCommand());
        this.addSubcommand(new RemoveDivisionRegionCommand());
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
