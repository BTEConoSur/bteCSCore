package com.bteconosur.core.command.config;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.menu.ConfirmationMenu;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.model.Player;
import com.bteconosur.discord.util.LinkService;

public class UnlinkCommand extends BaseCommand {

    private final YamlConfiguration lang;
    private ConfirmationMenu confirmationMenu;

    public UnlinkCommand() {
        super("unlink", "Desvincular la cuenta de Discord.", "[subcomando]", "btecs.command.unlink");
        this.addSubcommand(new UnlinkSetCommand());
        this.addSubcommand(new GenericHelpCommand(this));
        lang = ConfigHandler.getInstance().getLang();
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        if (args.length != 0) {
            String message = lang.getString("help-command-usage").replace("%command%", getFullCommand().replace(" " + command, ""));
            PlayerLogger.info(sender, message, (String) null);
            return true;
        }

        Player player = Player.getBTECSPlayer((org.bukkit.entity.Player) sender);
        if (!LinkService.isPlayerLinked(player)) {
            PlayerLogger.error(sender, lang.getString("minecraft-not-linked"), (String) null);
            return true;
        }

        org.bukkit.entity.Player bukkitPlayer = player.getBukkitPlayer();

        confirmationMenu = new ConfirmationMenu(lang.getString("gui-titles.unlink-confirm"), bukkitPlayer, confirmClick -> {
                LinkService.unlink(player);
                PlayerLogger.info(sender, lang.getString("minecraft-unlink-success"), (String) null);
                confirmationMenu.getGui().close(bukkitPlayer);
            }, (cancelClick -> {
                confirmationMenu.getGui().close(bukkitPlayer);
        }));

        confirmationMenu.open();
        return true;
    }
}
