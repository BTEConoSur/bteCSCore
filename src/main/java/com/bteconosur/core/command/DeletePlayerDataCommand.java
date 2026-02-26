package com.bteconosur.core.command;

import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.ConsoleLogger;
import com.bteconosur.core.util.PlayerLogger;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

public class DeletePlayerDataCommand extends BaseCommand {

    public DeletePlayerDataCommand() {
        super("deleteplayerdata", "<uuid>", "btecs.command.deleteplayerdata", CommandMode.CONSOLE_ONLY);
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        if (args.length != 1) {
            String message = LanguageHandler.getText("delete-playerdata-usage").replace("%comando%", getFullCommand());
            PlayerLogger.info(sender, message, (String) null);
            return true;
        }

        UUID uuid;
        try {
            uuid = UUID.fromString(args[0]);
        } catch (IllegalArgumentException ex) {
            String message = LanguageHandler.getText("delete-playerdata-invalid-uuid").replace("%uuid%", args[0]);
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        String worldName = config.getString("lobby.world");
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            String message = LanguageHandler.getText("delete-playerdata-world-not-found").replace("%world%", worldName);
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        Player onlinePlayer = Bukkit.getPlayer(uuid);
        if (onlinePlayer != null && onlinePlayer.isOnline()) {
            String message = LanguageHandler.getText("delete-playerdata-online").replace("%uuid%", uuid.toString());
            PlayerLogger.warn(sender, message, (String) null);
            return true;
        }

        File playerDataFolder = new File(world.getWorldFolder(), "playerdata");
        if (!playerDataFolder.exists()) {
            String message = LanguageHandler.getText("delete-playerdata-not-found")
                    .replace("%uuid%", uuid.toString())
                    .replace("%world%", world.getName());
            PlayerLogger.warn(sender, message, (String) null);
            return true;
        }

        File dataFile = new File(playerDataFolder, uuid + ".dat");
        File backupFile = new File(playerDataFolder, uuid + ".dat_old");
        boolean deletedAny = false;

        try {
            boolean deletedData = Files.deleteIfExists(dataFile.toPath());
            boolean deletedBackup = Files.deleteIfExists(backupFile.toPath());
            deletedAny = deletedData || deletedBackup;
        } catch (IOException ex) {
            String message = LanguageHandler.getText("delete-playerdata-error").replace("%uuid%", uuid.toString()).replace("%world%", world.getName());
            ConsoleLogger.error(message, ex);
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        if (!deletedAny) {
            String message = LanguageHandler.getText("delete-playerdata-not-found").replace("%uuid%", uuid.toString()).replace("%world%", world.getName());
            PlayerLogger.warn(sender, message, (String) null);
            return true;
        }

        String message = LanguageHandler.getText("delete-playerdata-success").replace("%uuid%", uuid.toString()).replace("%world%", world.getName());
        PlayerLogger.info(sender, message, (String) null);
        return true;
    }
}
