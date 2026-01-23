package com.bteconosur.core.command.config;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PlayerRegistry;

public class NicknameCommand extends BaseCommand {

    private final YamlConfiguration lang;

    public NicknameCommand() {
        super("nickname", "Cambiar el nombre público.", "<nombre_público>", "btecs.command.nickname");
        this.addSubcommand(new NicknameSetCommand());
        this.addSubcommand(new GenericHelpCommand(this));
        lang = ConfigHandler.getInstance().getLang();
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        if (args.length != 1) {
            String message = lang.getString("help-command-usage").replace("%command%", getFullCommand().replace(" " + command, ""));
            PlayerLogger.info(sender, message, (String) null);
            return true;
        }
 
        String nuevoNombre = args[0];
        if (nuevoNombre.length() > 16) {
            PlayerLogger.error(sender, lang.getString("invalid-player-name"), (String) null);
            return true;
        }

        PlayerRegistry playerRegistry = PlayerRegistry.getInstance();
        Player commandPlayer = playerRegistry.get(((org.bukkit.entity.Player) sender).getUniqueId());
        commandPlayer.setNombrePublico(nuevoNombre);
        playerRegistry.merge(commandPlayer.getUuid());
        PlayerLogger.info(commandPlayer, lang.getString("nickname-change").replace("%nickname%", nuevoNombre), (String) null);
        return true;
    }

}
