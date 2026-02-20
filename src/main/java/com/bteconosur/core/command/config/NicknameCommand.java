package com.bteconosur.core.command.config;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PlayerRegistry;

public class NicknameCommand extends BaseCommand {


    public NicknameCommand() {
        super("nickname", "Cambiar el nombre público.", "<nombre_público>", "btecs.command.nickname");
        this.addSubcommand(new NicknameSetCommand());
        this.addSubcommand(new GenericHelpCommand(this));
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player player = Player.getBTECSPlayer((org.bukkit.entity.Player) sender);
        Language language = player.getLanguage();
        if (args.length != 1) {
            String message = LanguageHandler.getText(language, "help-command-usage").replace("%comando%", getFullCommand().replace(" " + command, ""));
            PlayerLogger.info(sender, message, (String) null);
            return true;
        }
 
        String nuevoNombre = args[0];
        if (nuevoNombre.length() > 16) {
            PlayerLogger.error(sender, LanguageHandler.getText(language, "invalid-player-name"), (String) null);
            return true;
        }

        PlayerRegistry playerRegistry = PlayerRegistry.getInstance();
        Player commandPlayer = playerRegistry.get(((org.bukkit.entity.Player) sender).getUniqueId());
        commandPlayer.setNombrePublico(nuevoNombre);
        playerRegistry.merge(commandPlayer.getUuid());
        PlayerLogger.info(commandPlayer, LanguageHandler.getText(language, "nickname.change").replace("%nickname%", nuevoNombre), (String) null);
        return true;
    }

}
