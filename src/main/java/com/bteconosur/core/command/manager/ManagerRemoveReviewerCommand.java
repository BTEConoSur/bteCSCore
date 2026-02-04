package com.bteconosur.core.command.manager;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import com.bteconosur.core.chat.ChatUtil;
import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.util.DiscordLogger;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.PermissionManager;
import com.bteconosur.db.model.Pais;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PaisRegistry;
import com.bteconosur.db.registry.PlayerRegistry;

public class ManagerRemoveReviewerCommand extends BaseCommand {

    private final YamlConfiguration lang;
    private final Set<String> paises = PaisRegistry.getInstance().getMap().values().stream().map(Pais::getNombre).collect(Collectors.toSet());

    public ManagerRemoveReviewerCommand() {
        super("removereviewer", "Remover Reviewer de un país.", "<nombre_pais> <uuid/nombre_reviewer>", "", CommandMode.PLAYER_ONLY);

        ConfigHandler configHandler = ConfigHandler.getInstance();
        lang = configHandler.getLang();
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        PermissionManager permissionManager = PermissionManager.getInstance();
        PlayerRegistry playerRegistry = PlayerRegistry.getInstance();
        Player commandPlayer = playerRegistry.get(((org.bukkit.entity.Player) sender).getUniqueId());

        if (args.length != 2) {
            String message = lang.getString("help-command-usage").replace("%command%", getFullCommand().replace(" " + command, ""));
            PlayerLogger.info(sender, message, (String) null);
            return true;
        }

        UUID uuid;
        Player targetPlayer;
        
        Pais pais = PaisRegistry.getInstance().get(args[0].toLowerCase());

        if (pais == null) {
            String message = lang.getString("pais-not-found").replace("%pais%", args[0]);
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        if (!permissionManager.isManager(commandPlayer, pais)) {
            String message = lang.getString("not-a-manager-country").replace("%pais%", pais.getNombrePublico());
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        try {
            uuid = UUID.fromString(args[1]);
            targetPlayer = playerRegistry.get(uuid);
        } catch (IllegalArgumentException exception) {
            targetPlayer = playerRegistry.findByName(args[1]);
        }

        if (targetPlayer == null) {
            String message = lang.getString("player-not-registered").replace("%player%", args[1]);
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        if (!permissionManager.isReviewer(targetPlayer, pais)) {
            String message = lang.getString("reviewer-not-added").replace("%player%", targetPlayer.getNombre()).replace("%pais%", pais.getNombrePublico());
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        targetPlayer = permissionManager.removeReviewer(targetPlayer, pais);
        
        PlayerLogger.info(targetPlayer, lang.getString("reviewer-target-removed").replace("%player%", targetPlayer.getNombre()).replace("%pais%", pais.getNombrePublico()),
            ChatUtil.getDsReviewerRemoved(pais.getNombrePublico()));
        if (commandPlayer != targetPlayer) PlayerLogger.info(sender, lang.getString("reviewer-removed").replace("%player%", targetPlayer.getNombre()).replace("%pais%", pais.getNombrePublico()), (String) null);
        String countryLogMessage = lang.getString("reviewer-remove-log").replace("%staff%", commandPlayer.getNombre()).replace("%player%", targetPlayer.getNombre()).replace("%pais%", pais.getNombrePublico());
        DiscordLogger.countryLog(countryLogMessage, pais);

        return true;
    }

    @Override
    protected List<String> tabCompleteArgs(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) return paises.stream().filter(p -> p.toLowerCase().startsWith(args[0].toLowerCase())).toList();
        return super.tabComplete(sender, alias, args);
    }

    @Override
    protected boolean customPermissionCheck(CommandSender sender) {
        Player commandPlayer = PlayerRegistry.getInstance().get(((org.bukkit.entity.Player) sender).getUniqueId());
        return PermissionManager.getInstance().isManager(commandPlayer);
    }

}
