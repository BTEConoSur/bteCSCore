package com.bteconosur.core.command.crud.pais.update;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.util.PlayerLogger;

public class UPaisCommand extends BaseCommand {

    private final YamlConfiguration lang;

    public UPaisCommand() {
        super("update", "Actualizar propiedad de un País.", "<propiedad> <id> <valor>", CommandMode.BOTH);
        this.addSubcommand(new UPaisNombreCommand());
        this.addSubcommand(new UPaisDsIdGuildCommand());
        this.addSubcommand(new UPaisDsIdGlobalChatCommand());
        this.addSubcommand(new UPaisDsIdCountryChatCommand());
        this.addSubcommand(new UPaisDsIdLogCommand());
        this.addSubcommand(new UPaisDsIdRequestCommand());
        this.addSubcommand(new UPaisNombrePublicoCommand());
        this.addSubcommand(new AddPaisRegionCommand());
        this.addSubcommand(new UPaisGeoJsonCommand());
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
