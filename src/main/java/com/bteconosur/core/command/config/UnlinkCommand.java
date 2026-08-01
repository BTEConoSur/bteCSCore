package com.bteconosur.core.command.config;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.menu.ConfirmationMenu;
import com.bteconosur.core.util.DiscordLogger;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.model.Player;
import com.bteconosur.discord.util.LinkService;

public class UnlinkCommand extends BaseCommand {

    private ConfirmationMenu confirmationMenu;

    public UnlinkCommand() {
        super("unlink", "", "btecs.command.unlink", CommandMode.PLAYER_ONLY);
        this.addSubcommand(new UnlinkSetCommand());
        this.addSubcommand(new GenericHelpCommand(this));
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player commandPlayer = Player.getBTECSPlayer((org.bukkit.entity.Player) sender);
        Language language = commandPlayer.getLanguage();
        if (args.length != 0) {
            String message = LanguageHandler.getText(language, "help-command-usage").replace("%comando%", getFullCommand());
            PlayerLogger.info(sender, message, (String) null);
            return true;
        }

        if (!LinkService.isPlayerLinked(commandPlayer)) {
            PlayerLogger.error(sender, LanguageHandler.getText(language, "link.mc-not-linked"), (String) null);
            return true;
        }

        org.bukkit.entity.Player bukkitPlayer = commandPlayer.getBukkitPlayer();
        long discordId = commandPlayer.getDsIdUsuario();
        confirmationMenu = new ConfirmationMenu(LanguageHandler.getText(language, "gui-titles.unlink-confirm"), bukkitPlayer, confirmClick -> {
                LinkService.unlink(commandPlayer);
                PlayerLogger.info(sender, LanguageHandler.getText(language, "link.unlink-success"), (String) null);
                DiscordLogger.staffLog(LanguageHandler.replaceDS("link.unlink-log", Language.getDefault(), commandPlayer).replace("%discordId%", String.valueOf(discordId)));
                confirmationMenu.getGui().close(bukkitPlayer);
            }, (cancelClick -> {
                confirmationMenu.getGui().close(bukkitPlayer);
        }));

        confirmationMenu.open();
        return true;
    }
}
