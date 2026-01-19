package com.bteconosur.core.command.project.admin;

import java.util.List;
import java.util.Set;
import java.util.UUID;

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

public class ProjectAdminAddReviewer extends BaseCommand {

    private final YamlConfiguration lang;
    private final Set<String> paises = PaisRegistry.getInstance().getMap().keySet();

    public ProjectAdminAddReviewer() {
        super("addreviewer", "Añadir Reviewer a un país.", "<nombre_pais> <uuid/nombre_reviewer>", "btecs.command.project.admin.addreviewer", CommandMode.BOTH);

        ConfigHandler configHandler = ConfigHandler.getInstance();
        lang = configHandler.getLang();
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

        String paisNombre = args[0];
        Pais pais = PaisRegistry.getInstance().get(paisNombre.toLowerCase());

        if (pais == null) {
            String message = lang.getString("pais-not-found").replace("%pais%", paisNombre);
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        PermissionManager permissionManager = PermissionManager.getInstance();

        if (permissionManager.isReviewer(targetPlayer, pais)) {
            String message = lang.getString("reviewer-already-added").replace("%player%", targetPlayer.getNombre()).replace("%pais%", pais.getNombrePublico());
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        permissionManager.addReviewer(targetPlayer, pais);
        Player commandPlayer = playerRegistry.get(((org.bukkit.entity.Player) sender).getUniqueId());
        PlayerLogger.info(targetPlayer, lang.getString("reviewer-target-added").replace("%player%", targetPlayer.getNombre()).replace("%pais%", pais.getNombrePublico()),
            ChatUtil.getDsReviewerAdded(pais.getNombrePublico()));
        if (commandPlayer != targetPlayer) PlayerLogger.info(sender, lang.getString("reviewer-added").replace("%player%", targetPlayer.getNombre()).replace("%pais%", pais.getNombrePublico()), (String) null);
        String countryLogMessage = lang.getString("reviewer-add-log").replace("%staff%", commandPlayer.getNombre()).replace("%player%", targetPlayer.getNombre()).replace("%pais%", pais.getNombrePublico());
        DiscordLogger.countryLog(countryLogMessage, pais);

        return true;
    }

    @Override
    protected List<String> tabCompleteArgs(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) return paises.stream().filter(p -> p.toLowerCase().startsWith(args[0].toLowerCase())).toList();
        return super.tabComplete(sender, alias, args);
    }
}
