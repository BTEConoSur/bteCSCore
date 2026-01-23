package com.bteconosur.core.command.config;

import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PlayerRegistry;

public class NicknameSetCommand extends BaseCommand {

    private final YamlConfiguration lang;

    public NicknameSetCommand() {
        super("set", "Cambiar el nombre público de un jugador.", "<uuid/nombre> <nombre_público>", "btecs.command.nickname.set");
        lang = ConfigHandler.getInstance().getLang();
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        if (args.length != 2) {
            String message = lang.getString("help-command-usage").replace("%command%", getFullCommand().replace(" " + command, ""));
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
            String message = lang.getString("player-not-registered").replace("%player%", args[0]);
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }
 
        String nuevoNombre = args[1];
        if (nuevoNombre.length() > 16) {
            PlayerLogger.error(sender, lang.getString("invalid-player-name"), (String) null);
            return true;
        }

        Player commandPlayer = playerRegistry.get(((org.bukkit.entity.Player) sender).getUniqueId());
        targetPlayer.setNombrePublico(nuevoNombre);
        playerRegistry.merge(targetPlayer.getUuid());
        PlayerLogger.info(targetPlayer, lang.getString("nickname-change").replace("%nickname%", nuevoNombre), (String) null);
        if (!targetPlayer.equals(commandPlayer)) {
            PlayerLogger.info(commandPlayer, lang.getString("nickname-set").replace("%player%", targetPlayer.getNombre()).replace("%nickname%", nuevoNombre), (String) null);
        }
        return true;
    }

}
