package com.bteconosur.core.command.config;

import java.util.UUID;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PlayerRegistry;

public class NicknameSetCommand extends BaseCommand {

    public NicknameSetCommand() {
        super("set", "<uuid/nombre> <nombre_público>", "btecs.command.nickname.set", CommandMode.BOTH);
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player commandPlayer = null;
        if (sender instanceof org.bukkit.entity.Player) commandPlayer = Player.getBTECSPlayer((org.bukkit.entity.Player) sender);
        Language language = commandPlayer != null ? commandPlayer.getLanguage() : Language.getDefault();
        if (args.length < 1) {
            String message = LanguageHandler.getText(language, "help-command-usage").replace("%comando%", getFullCommand().replace(" " + command, ""));
            PlayerLogger.info(sender, message, (String) null);
            return true;
        }

        UUID uuid;
        Player targetPlayer;
        PlayerRegistry playerRegistry = PlayerRegistry.getInstance();

        try{
            uuid = UUID.fromString(args[0]);
            targetPlayer = playerRegistry.get(uuid);
        } catch (IllegalArgumentException exception){
            targetPlayer = playerRegistry.findByName(args[0]);
        }

        if (targetPlayer == null) {
            String message = LanguageHandler.getText(language, "player-not-registered").replace("%player%", args[0]);
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }
 
        String nuevoNombre = "";
        if (args.length > 1) {
            nuevoNombre = args[1];
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
        }

        String msg1;
        String msg2;
        if (nuevoNombre.length() == 0) {
            nuevoNombre = targetPlayer.getNombre();
            msg1 = LanguageHandler.getText(targetPlayer.getLanguage(), "nickname.reset");
            msg2 = LanguageHandler.replaceMC("nickname.set-reset", language, targetPlayer);
        } else {
            msg1 = LanguageHandler.getText(targetPlayer.getLanguage(), "nickname.change").replace("%nickname%", nuevoNombre);
            msg2 = LanguageHandler.replaceMC("nickname.set", language, targetPlayer).replace("%nickname%", nuevoNombre);
        }

        targetPlayer.setNombrePublico(nuevoNombre);
        playerRegistry.merge(targetPlayer.getUuid());
        PlayerLogger.info(targetPlayer, msg1, (String) null);
        if (!targetPlayer.equals(commandPlayer)) {
            PlayerLogger.info(sender, msg2, (String) null);
        }
        return true;
    }

}
