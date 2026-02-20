package com.bteconosur.core.command.pais;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.menu.config.PaisPrefixSelectMenu;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PlayerRegistry;

public class PaisPrefixSetCommand extends BaseCommand {

    public PaisPrefixSetCommand() {
        super("set", "Cambia el prefix de un Jugador Online", "<nombre_jugador>", "btecs.command.prefix.set", CommandMode.PLAYER_ONLY);
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player commandPlayer = PlayerRegistry.getInstance().get(sender);
        Language language = commandPlayer.getLanguage();
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

        Player player = PlayerRegistry.getInstance().get(((org.bukkit.entity.Player) sender).getUniqueId());
        String title = LanguageHandler.replaceMC("gui-titles.pais-prefix-set", language, targetPlayer);
        PaisPrefixSelectMenu menu = new PaisPrefixSelectMenu(player, title);
        menu.setBTECSPlayer(targetPlayer);
        menu.open();

        return true;
    }

}
