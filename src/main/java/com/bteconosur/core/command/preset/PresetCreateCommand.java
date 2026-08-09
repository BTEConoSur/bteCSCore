package com.bteconosur.core.command.preset;

import java.util.List;
import java.util.Map;

import org.bukkit.block.data.BlockData;
import org.bukkit.command.CommandSender;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.menu.preset.PresetCreateMenu;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.Preset;
import com.bteconosur.db.registry.PlayerRegistry;

public class PresetCreateCommand extends BaseCommand {

    public PresetCreateCommand() {
        super("create", "<nombre_preset> [blocks]", "btecs.command.preset", CommandMode.PLAYER_ONLY);
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

        int maxPresets = ConfigHandler.getInstance().getConfig().getInt("preset.max-per-player");
        if (player.getPresets().size() >= maxPresets) {
            PlayerLogger.error(player, LanguageHandler.getText(language, "preset.max-presets").replace("%quantity%", String.valueOf(maxPresets)), (String) null);
            return true;
        }

        if (player.hasPreset(presetName)) {
            PlayerLogger.error(player, LanguageHandler.getText(language, "preset.already").replace("%nombre%", presetName), (String) null);
            return true;
        }

        if (presetName.length() > 30) {
            PlayerLogger.error(player, LanguageHandler.getText(language, "preset.invalid-name-length"), (String) null);
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

            registry.createPreset(player.getUuid(), blocksMap, presetName);
            PlayerLogger.info(player, LanguageHandler.getText(language, "preset.created").replace("%nombre%", presetName), (String) null);
            return true;
        }

        PresetCreateMenu menu = new PresetCreateMenu(player, presetName);
        menu.open();
        return true;
    }

}
