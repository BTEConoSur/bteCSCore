package com.bteconosur.core.command.preset;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.bukkit.block.data.BlockData;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.menu.preset.PresetCreateMenu;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.model.Preset;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PlayerRegistry;

public class PresetEditCommand extends BaseCommand {

    public PresetEditCommand() {
        super("edit", "<nombre_preset> [blocks]", "btecs.command.preset", CommandMode.PLAYER_ONLY);
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        PlayerRegistry registry = PlayerRegistry.getInstance();
        Player player = registry.get(sender);
        Language language = player.getLanguage();
        if (args.length < 1) {
            String message = LanguageHandler.getText(language, "help-command-usage").replace("%comando%", getFullCommand().replace(" " + command, ""));
            PlayerLogger.info(sender, message, (String) null);
            return true;
        }

        String presetName = args[0];
        if (!player.hasPreset(presetName)) {
            PlayerLogger.error(player, LanguageHandler.getText(language, "preset.not-found").replace("%nombre%", presetName), (String) null);
            return true;
        }

        if (args.length > 1) {
            if (args.length != 2) {
                PlayerLogger.error(player, LanguageHandler.getText(language, "help-command-usage").replace("%comando%", getFullCommand().replace(" " + command, "")), (String) null);
                return true;
            }

            String blocksArg = args[1];
            Map<BlockData, Integer> blocksMap;
            try {
                blocksMap = Preset.parseBlocks(blocksArg, true);
            } catch (IllegalArgumentException e) {
                PlayerLogger.error(player, LanguageHandler.getText(language, "preset.invalid-format").replace("%error%", e.getMessage()), (String) null);
                return true;
            }

            List<String> bannedMaterials = ConfigHandler.getInstance().getConfig().getStringList("preset.banned-blocks");
            for (BlockData blockData : blocksMap.keySet()) {
                String materialName = blockData.getMaterial().name();

                boolean isBanned = bannedMaterials.stream().anyMatch(banned -> banned.equalsIgnoreCase(materialName));

                if (isBanned) {
                    PlayerLogger.error(player, LanguageHandler.getText(language, "preset.banned-block").replace("%block%", materialName), (String) null);
                    return true;
                }
            }

            registry.editPreset(player.getUuid(), blocksMap, presetName);
            PlayerLogger.info(player, LanguageHandler.getText(language, "preset.edited").replace("%nombre%", presetName), (String) null);
            return true;
        }

        Preset preset = player.getPreset(presetName);
        PresetCreateMenu menu = new PresetCreateMenu(player, presetName, preset.getBlocksMap());
        menu.open();
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
