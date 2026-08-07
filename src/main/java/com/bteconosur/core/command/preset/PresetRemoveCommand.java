package com.bteconosur.core.command.preset;

import java.util.Collections;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PlayerRegistry;

public class PresetRemoveCommand extends BaseCommand {

    public PresetRemoveCommand() {
        super("remove", "<nombre_preset>", "btecs.command.preset", CommandMode.PLAYER_ONLY);
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        PlayerRegistry registry = PlayerRegistry.getInstance();
        Player player = registry.get(sender);
        Language language = player.getLanguage();
        if (args.length != 1) {
            String message = LanguageHandler.getText(language, "help-command-usage").replace("%comando%", getFullCommand().replace(" " + command, ""));
            PlayerLogger.info(sender, message, (String) null);
            return true;
        }
        String presetName = args[0];
        if (!player.hasPreset(presetName)) {
            PlayerLogger.error(player, LanguageHandler.getText(language, "preset.not-found").replace("%nombre%", presetName), (String) null);
            return true;
        }
        registry.removePreset(player.getUuid(), presetName);
        PlayerLogger.info(player, LanguageHandler.getText(language, "preset.removed").replace("%nombre%", presetName), (String) null);
        return true;
    }

    @Override
    protected List<String> tabCompleteArgs(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) {
        Player player = PlayerRegistry.getInstance().get(sender);
        if (player == null) return Collections.emptyList();
        if (args.length == 1) return player.getPresets().stream().map(p -> p.getId().getNombre()).filter(n -> n.toLowerCase().startsWith(args[0].toLowerCase())).toList();
        return Collections.emptyList();
    }

}
