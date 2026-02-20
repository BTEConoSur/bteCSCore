package com.bteconosur.core.command.chat;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.menu.chat.ChatSelectMenu;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PlayerRegistry;

public class ChatSetCommand extends BaseCommand {


    public ChatSetCommand() {
        super("set", "Cambia el chat de un Jugador Online", "<nombre_jugador>", "btecs.command.chat.set", CommandMode.PLAYER_ONLY);
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

        org.bukkit.entity.Player bukkitPlayer = Bukkit.getPlayer(args[0]);

        if(bukkitPlayer == null) {
            String message = LanguageHandler.getText(language, "player-not-found").replace("%player%", args[0]);
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        Player targetPlayer = PlayerRegistry.getInstance().get(bukkitPlayer.getUniqueId());

        String title = LanguageHandler.replaceMC("gui-titles.chat-set", language, targetPlayer);
        ChatSelectMenu menu = new ChatSelectMenu(player, title);
        menu.setBTECSPlayer(targetPlayer);
        menu.open();

        return true;
    }

}
