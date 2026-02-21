package com.bteconosur.core.command.manager;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import com.bteconosur.core.chat.ChatUtil;
import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.DiscordLogger;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.PermissionManager;
import com.bteconosur.db.model.Pais;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PaisRegistry;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.db.util.PlaceholderUtils;

public class ManagerAddReviewerCommand extends BaseCommand {

    private final Set<String> paises = PaisRegistry.getInstance().getMap().values().stream().map(Pais::getNombre).collect(Collectors.toSet());

    public ManagerAddReviewerCommand() {
        super("addreviewer", "Añadir Reviewer a un país.", "<nombre_pais> <uuid/nombre_reviewer>", CommandMode.PLAYER_ONLY);
        this.addSubcommand(new GenericHelpCommand(this));
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        PermissionManager permissionManager = PermissionManager.getInstance();
        PlayerRegistry playerRegistry = PlayerRegistry.getInstance();
        Player commandPlayer = PlayerRegistry.getInstance().get(sender);
        Language language = commandPlayer.getLanguage();
        if (args.length != 2) {
            String message = LanguageHandler.getText(language, "help-command-usage").replace("%comando%", getFullCommand());
            PlayerLogger.info(sender, message, (String) null);
            return true;
        }

        UUID uuid;
        Player targetPlayer;
        
        Pais pais = PaisRegistry.getInstance().get(args[0].toLowerCase());

        if (pais == null) {
            String message = LanguageHandler.getText(language, "pais-not-found").replace("%search%", args[0]);
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        if (!permissionManager.isManager(commandPlayer, pais)) {
            String message = LanguageHandler.replaceMC("manager.not-manager-country", language, pais);
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
            String message = LanguageHandler.getText(language, "player-not-registered").replace("%player%", args[1]);
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        if (permissionManager.isReviewer(targetPlayer, pais)) {
            String message = LanguageHandler.replaceMC("reviewer.already", language, targetPlayer, pais);
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }

        targetPlayer = permissionManager.addReviewer(targetPlayer, pais);
        PlayerLogger.info(targetPlayer, LanguageHandler.replaceMC("reviewer.add.for-target", targetPlayer.getLanguage(), targetPlayer, pais),
            ChatUtil.getDsReviewerAdded(pais, targetPlayer.getLanguage()));
        if (commandPlayer != targetPlayer) PlayerLogger.info(sender, LanguageHandler.replaceMC("reviewer.add.success", language, targetPlayer, pais), (String) null);
        String countryLog = LanguageHandler.replaceDS("reviewer.add.log", Language.getDefault(), commandPlayer, targetPlayer);
        countryLog = PlaceholderUtils.replaceDS(countryLog, Language.getDefault(), pais);
        DiscordLogger.countryLog(countryLog, pais);

        return true;
    }

    @Override
    protected List<String> tabCompleteArgs(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) return paises.stream().filter(p -> p.toLowerCase().startsWith(args[0].toLowerCase())).toList();
        return super.tabComplete(sender, alias, args); //TODO: no parece funcionar
    }

}
