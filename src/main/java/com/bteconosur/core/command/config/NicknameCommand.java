package com.bteconosur.core.command.config;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.chat.ChatUtil;
import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PlayerRegistry;

public class NicknameCommand extends BaseCommand {


    public NicknameCommand() {
        super("nickname", "<nombre_público>", "btecs.command.nickname", CommandMode.PLAYER_ONLY);
        this.addSubcommand(new NicknameSetCommand());
        this.addSubcommand(new GenericHelpCommand(this));
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player player = Player.getBTECSPlayer((org.bukkit.entity.Player) sender);
        Language language = player.getLanguage();
 
        String nuevoNombre = "";
        if (args.length != 0) {
            nuevoNombre = args[0];
            if (nuevoNombre.length() > 16) {
                PlayerLogger.error(sender, LanguageHandler.getText(language, "invalid-player-name"), (String) null);
                return true;
            }

            if (nuevoNombre.length() < 2 && nuevoNombre.length() != 0) {
                PlayerLogger.error(sender, LanguageHandler.getText(language, "nickname.min-length"), (String) null);
                return true;
            }

            if (nuevoNombre.matches(".*<[^>]+>.*")) {
                PlayerLogger.error(sender, LanguageHandler.getText(language, "nickname.invalid-regex"), (String) null);
                return true;
            }

            if (ChatUtil.hasBannedChars(nuevoNombre)) {
                PlayerLogger.error(sender, LanguageHandler.getText(language, "invalid-chars"), (String) null);
                return true;
            }
        }
        PlayerRegistry playerRegistry = PlayerRegistry.getInstance();
        Player commandPlayer = playerRegistry.get(((org.bukkit.entity.Player) sender).getUniqueId());
        String msg;
        if (nuevoNombre.length() == 0) {
            nuevoNombre = commandPlayer.getNombre();
            msg = LanguageHandler.getText(language, "nickname.reset");
        } else msg = LanguageHandler.getText(language, "nickname.change").replace("%nickname%", nuevoNombre);
        commandPlayer.setNombrePublico(nuevoNombre);
        playerRegistry.merge(commandPlayer.getUuid());
        PlayerLogger.info(commandPlayer, msg, (String) null);
        return true;
    }

}
