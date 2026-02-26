package com.bteconosur.core.command.pwarp;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.Pwarp;
import com.bteconosur.db.registry.PlayerRegistry;

public class PwarpCommand extends BaseCommand {

    public PwarpCommand() {
        super("pwarp", "<nombre_warp>|<subcomando>", "btecs.command.pwarp", CommandMode.PLAYER_ONLY);
        this.addSubcommand(new PwarpCreateCommand());
        this.addSubcommand(new PwarpRemoveCommand());
        this.addSubcommand(new PwarpListCommand());
        this.addSubcommand(new GenericHelpCommand(this));
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player player = PlayerRegistry.getInstance().get(sender);
        Language language = player.getLanguage();
        if (args.length != 1) {
            String message = LanguageHandler.getText(language, "help-command-usage").replace("%comando%", getFullCommand());
            PlayerLogger.info(sender, message, (String) null);
            return true;
        }
        String nombreWarp = args[0];
        if (!player.hasPwarp(nombreWarp)) {
            PlayerLogger.error(player, LanguageHandler.getText(language, "pwarp.not-found").replace("%nombre%", nombreWarp), (String) null);
            return true;
        }
        Pwarp pwarp = player.getPwarp(nombreWarp);
        org.bukkit.entity.Player bukkitPlayer = (org.bukkit.entity.Player) sender;
        bukkitPlayer.teleport(pwarp.toLocation());
        return true;
    }

    @Override
    protected List<String> tabCompleteArgs(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) {
        Player player = PlayerRegistry.getInstance().get(sender);
        if (args.length == 1) return player.getPwarpNames().stream().filter(p -> p.toLowerCase().startsWith(args[0].toLowerCase())).toList();
        return super.tabComplete(sender, alias, args);
    }

}
