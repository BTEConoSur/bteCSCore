package com.bteconosur.core.command.preset;

import java.util.Collections;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.core.util.TagResolverUtils;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.Preset;
import com.bteconosur.db.registry.PlayerRegistry;

import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

public class PresetSeeCommand extends BaseCommand {

    public PresetSeeCommand() {
        super("see", "<nombre_preset>", "btecs.command.preset", CommandMode.PLAYER_ONLY);
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player player = PlayerRegistry.getInstance().get(sender);
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
        Preset preset = player.getPreset(presetName);
        if (preset.getBlocks() == null || preset.getBlocks().isEmpty() || preset.getBlocks().isBlank()) {
            PlayerLogger.info(sender, LanguageHandler.getText(language, "preset.see-empty").replace("%nombre%", preset.getId().getNombre()), (String) null);
            return true;
        }
        TagResolver tagResolver1 = TagResolverUtils.getCopyableText("contenido", preset.getBlocks(), preset.getBlocks(), language);
        PlayerLogger.info(sender, LanguageHandler.getText(language, "preset.see").replace("%nombre%", preset.getId().getNombre()), (String) null, tagResolver1);
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
