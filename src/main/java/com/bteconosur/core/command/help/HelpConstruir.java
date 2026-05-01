package com.bteconosur.core.command.help;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.model.Player;

public class HelpConstruir extends BaseCommand {

    public HelpConstruir() {
        super("construir", "", "btecs.command.help", CommandMode.PLAYER_ONLY);
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player player = Player.getBTECSPlayer((org.bukkit.entity.Player) sender);
        Language language = player.getLanguage();
        String pluginPrefix = LanguageHandler.getText(language, "plugin-prefix");
        List<String> lore = LanguageHandler.getTextList(language, "help-construir");
        List<String> processedLore = new ArrayList<>();
        for (String line : lore) {
            processedLore.add(line.replace("%plugin-prefix%", pluginPrefix));
        }
        String message = String.join("\n", processedLore);
        PlayerLogger.send(player, message, (String) null);
        return true;
    }


}
