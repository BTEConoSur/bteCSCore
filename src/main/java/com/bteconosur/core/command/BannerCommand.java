package com.bteconosur.core.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.DyeColor;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.core.util.BannerUtils;

public class BannerCommand extends BaseCommand {

    public BannerCommand() {
        super("banner", "<color_letra> <color_fondo> <texto>", "btecs.command.banner", CommandMode.PLAYER_ONLY);
        this.addSubcommand(new GenericHelpCommand(this));
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        org.bukkit.entity.Player bukkitPlayer = (org.bukkit.entity.Player) sender;
        Player player = PlayerRegistry.getInstance().get(bukkitPlayer.getUniqueId());
        Language language = player != null ? player.getLanguage() : Language.getDefault();

        if (args.length < 3) {
            String usage = LanguageHandler.getText(language, "help-command-usage").replace("%comando%", getFullCommand());
            PlayerLogger.error(sender, usage, (String) null);
            return true;
        }

        DyeColor letterColor = BannerUtils.getColorByAlias(args[0]);
        DyeColor backColor = BannerUtils.getColorByAlias(args[1]);

        if (letterColor == null) {
            PlayerLogger.error(sender, LanguageHandler.getText(language, "banner.invalid-color").replace("%color%", args[0]), (String) null);
            return true;
        }

        if (backColor == null) {
            PlayerLogger.error(sender, LanguageHandler.getText(language, "banner.invalid-color").replace("%color%", args[1]), (String) null);
            return true;
        }

        StringBuilder textBuilder = new StringBuilder();
        for (int i = 2; i < args.length; i++) {
            textBuilder.append(args[i]);
        }
        String text = textBuilder.toString().toLowerCase();

        Set<Character> uniqueValidChars = new HashSet<>();
        for (char c : text.toCharArray()) {
            if (BannerUtils.hasLetter(c)) uniqueValidChars.add(c);
        }

        int bannersNeeded = uniqueValidChars.size();

        if (bannersNeeded == 0) {
            PlayerLogger.error(sender, LanguageHandler.getText(language, "banner.invalid-text").replace("%text%", text), (String) null);
            return true;
        }

        int emptySlots = 0;
        for (ItemStack item : bukkitPlayer.getInventory().getStorageContents()) {
            if (item == null || item.getType().isAir()) emptySlots++;
        }

        if (bannersNeeded > emptySlots) {
            PlayerLogger.error(sender, LanguageHandler.getText(language, "banner.no-space").replace("%espacio%", String.valueOf(bannersNeeded)), (String) null);
            return true;
        }

        for (char c : uniqueValidChars) {
            ItemStack bannerItem = BannerUtils.generateLetterBanner(c, letterColor, backColor);
            if (bannerItem != null) bukkitPlayer.getInventory().addItem(bannerItem);
        }

        PlayerLogger.info(sender, LanguageHandler.getText(language, "banner.success").replace("%cantidad%", String.valueOf(bannersNeeded)), (String) null);
        
        return true;
    }

    @Override
    protected List<String> tabCompleteArgs(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1 || args.length == 2) {
            String currentArg = args[args.length - 1].toLowerCase();
            List<String> completions = new ArrayList<>();
            
            for (String colorAlias : BannerUtils.getAllColorAliases()) {
                if (colorAlias.startsWith(currentArg)) completions.add(colorAlias);
            }
            return completions;
        }
        return Collections.emptyList();
    }
}
