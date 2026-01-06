package com.bteconosur.core.command.pais;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.menu.chat.ChatSelectMenu;
import com.bteconosur.core.menu.config.PaisPrefixSelectMenu;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PlayerRegistry;

public class PaisPrefixSetCommand extends BaseCommand {

    private final YamlConfiguration lang;

    public PaisPrefixSetCommand() {
        super("set", "Cambia el prefix de un Jugador Online", "<nombre_jugador>", "btecs.command.prefix.set", CommandMode.PLAYER_ONLY);
        lang = ConfigHandler.getInstance().getLang();
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        if (args.length != 1) {
            String message = lang.getString("help-command-usage").replace("%command%", getFullCommand().replace(" " + command, ""));
            sender.sendMessage(message);
            return true;
        }

        org.bukkit.entity.Player bukkitPlayer = Bukkit.getPlayer(args[0]);

        if(bukkitPlayer == null) {
            String message = lang.getString("player-not-found").replace("%player%", args[0]);
            sender.sendMessage(message);
            return true;
        }

        Player targetPlayer = PlayerRegistry.getInstance().get(bukkitPlayer.getUniqueId());

        Player player = PlayerRegistry.getInstance().get(((org.bukkit.entity.Player) sender).getUniqueId());
        String title = lang.getString("gui-titles.pais-prefix-set").replace("%player%", targetPlayer.getNombre());
        PaisPrefixSelectMenu menu = new PaisPrefixSelectMenu(player, title);
        menu.setBTECSPlayer(targetPlayer);
        menu.open();

        return true;
    }

}
