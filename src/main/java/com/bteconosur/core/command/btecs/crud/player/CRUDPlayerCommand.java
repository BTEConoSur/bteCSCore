package com.bteconosur.core.command.btecs.crud.player;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.command.btecs.crud.player.update.UPlayerCommand;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.util.PlayerLogger;

public class CRUDPlayerCommand extends BaseCommand {

    private final YamlConfiguration lang;

    public CRUDPlayerCommand() {
        super("player", "Realizar operaciones CRUD sobre jugadores.", null, CommandMode.BOTH);
        this.addSubcommand(new CPlayerCommand());
        this.addSubcommand(new RPlayerCommand());
        this.addSubcommand(new UPlayerCommand());
        this.addSubcommand(new DPlayerCommand());
            this.addSubcommand(new GenericHelpCommand(this));

        ConfigHandler configHandler = ConfigHandler.getInstance();
        lang = configHandler.getLang();
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        String message = lang.getString("help-command-usage").replace("%command%", getFullCommand());
        PlayerLogger.info(sender, message, (String) null);
        return true;
    }

}
