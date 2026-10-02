package com.bteconosur.core.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;

import com.bteconosur.core.config.ConfigHandler;

public class BannerUtils {

    public record BannerStroke(boolean isBackground, PatternType type) {}

    private static final Map<Character, List<BannerStroke>> LETTERS = new HashMap<>();
    private static final Map<String, DyeColor> COLOR_ALIASES = new HashMap<>();

    static {
        loadColorAliases();

        LETTERS.put('a', List.of(
            new BannerStroke(false, PatternType.STRIPE_MIDDLE),
            new BannerStroke(false, PatternType.STRIPE_LEFT),
            new BannerStroke(false, PatternType.STRIPE_RIGHT),
            new BannerStroke(false, PatternType.STRIPE_TOP),
            new BannerStroke(true, PatternType.BORDER)
        ));
        
        LETTERS.put('b', List.of(
            new BannerStroke(false, PatternType.STRIPE_RIGHT),
            new BannerStroke(false, PatternType.STRIPE_BOTTOM),
            new BannerStroke(false, PatternType.STRIPE_TOP),
            new BannerStroke(true, PatternType.CURLY_BORDER),
            new BannerStroke(false, PatternType.STRIPE_LEFT),
            new BannerStroke(false, PatternType.STRIPE_MIDDLE),
            new BannerStroke(true, PatternType.BORDER)
        ));

        LETTERS.put('c', List.of(
            new BannerStroke(false, PatternType.STRIPE_LEFT),
            new BannerStroke(false, PatternType.STRIPE_TOP),
            new BannerStroke(false, PatternType.STRIPE_BOTTOM),
            new BannerStroke(true, PatternType.BORDER)
        ));

        LETTERS.put('q', List.of(
            new BannerStroke(true, PatternType.RHOMBUS),
            new BannerStroke(false, PatternType.STRIPE_LEFT),
            new BannerStroke(false, PatternType.STRIPE_RIGHT),
            new BannerStroke(false, PatternType.SQUARE_BOTTOM_RIGHT),
            new BannerStroke(true, PatternType.BORDER)
        ));
    
        LETTERS.put('d', List.of(
            new BannerStroke(false, PatternType.STRIPE_BOTTOM),
            new BannerStroke(false, PatternType.STRIPE_TOP),
            new BannerStroke(false, PatternType.STRIPE_RIGHT),
            new BannerStroke(true, PatternType.CURLY_BORDER),
            new BannerStroke(false, PatternType.HALF_VERTICAL),
            new BannerStroke(true, PatternType.BORDER)
        ));

        LETTERS.put('e', List.of(
            new BannerStroke(false, PatternType.STRIPE_TOP),
            new BannerStroke(false, PatternType.STRIPE_BOTTOM),
            new BannerStroke(false, PatternType.STRIPE_MIDDLE),
            new BannerStroke(false, PatternType.STRIPE_LEFT),
            new BannerStroke(true, PatternType.BORDER)
        ));

        LETTERS.put('f', List.of(
            new BannerStroke(false, PatternType.STRIPE_TOP),
            new BannerStroke(false, PatternType.STRIPE_LEFT),
            new BannerStroke(false, PatternType.STRIPE_MIDDLE),
            new BannerStroke(true, PatternType.BORDER)
        ));

        LETTERS.put('g', List.of(
            new BannerStroke(false, PatternType.STRIPE_RIGHT),
            new BannerStroke(true, PatternType.HALF_HORIZONTAL),
            new BannerStroke(false, PatternType.STRIPE_BOTTOM),
            new BannerStroke(false, PatternType.STRIPE_LEFT),
            new BannerStroke(false, PatternType.STRIPE_TOP),
            new BannerStroke(true, PatternType.BORDER)
        ));

        LETTERS.put('h', List.of(
            new BannerStroke(false, PatternType.STRIPE_LEFT),
            new BannerStroke(false, PatternType.STRIPE_RIGHT),
            new BannerStroke(false, PatternType.STRIPE_MIDDLE),
            new BannerStroke(true, PatternType.BORDER)
        ));

        LETTERS.put('i', List.of(
            new BannerStroke(false, PatternType.STRIPE_CENTER),
            new BannerStroke(false, PatternType.STRIPE_TOP),
            new BannerStroke(false, PatternType.STRIPE_BOTTOM),
            new BannerStroke(true, PatternType.BORDER)
        ));

        LETTERS.put('j', List.of(
            new BannerStroke(false, PatternType.STRIPE_LEFT),
            new BannerStroke(true, PatternType.HALF_HORIZONTAL),
            new BannerStroke(false, PatternType.STRIPE_BOTTOM),
            new BannerStroke(false, PatternType.STRIPE_RIGHT),
            new BannerStroke(true, PatternType.BORDER)
        ));

        LETTERS.put('k', List.of(
            new BannerStroke(false, PatternType.STRIPE_DOWNRIGHT),
            new BannerStroke(true, PatternType.HALF_HORIZONTAL),
            new BannerStroke(false, PatternType.STRIPE_DOWNLEFT),
            new BannerStroke(false, PatternType.STRIPE_LEFT),
            new BannerStroke(true, PatternType.BORDER)
        ));

        LETTERS.put('l', List.of(
            new BannerStroke(false, PatternType.STRIPE_LEFT),
            new BannerStroke(false, PatternType.STRIPE_BOTTOM),
            new BannerStroke(true, PatternType.BORDER)
        ));

        LETTERS.put('m', List.of(
            new BannerStroke(false, PatternType.TRIANGLE_TOP),
            new BannerStroke(true, PatternType.TRIANGLES_TOP),
            new BannerStroke(false, PatternType.STRIPE_LEFT),
            new BannerStroke(false, PatternType.STRIPE_RIGHT),
            new BannerStroke(true, PatternType.BORDER)
        ));

        LETTERS.put('n', List.of(
            new BannerStroke(false, PatternType.STRIPE_DOWNRIGHT),
            new BannerStroke(false, PatternType.STRIPE_LEFT),
            new BannerStroke(false, PatternType.STRIPE_RIGHT),
            new BannerStroke(true, PatternType.BORDER)
        ));

        LETTERS.put('o', List.of(
            new BannerStroke(false, PatternType.STRIPE_TOP),
            new BannerStroke(false, PatternType.STRIPE_LEFT),
            new BannerStroke(false, PatternType.STRIPE_RIGHT),
            new BannerStroke(false, PatternType.STRIPE_BOTTOM),
            new BannerStroke(true, PatternType.BORDER)
        ));

        LETTERS.put('p', List.of(
            new BannerStroke(false, PatternType.STRIPE_RIGHT),
            new BannerStroke(true, PatternType.HALF_HORIZONTAL_BOTTOM),
            new BannerStroke(false, PatternType.STRIPE_MIDDLE),
            new BannerStroke(false, PatternType.STRIPE_TOP),
            new BannerStroke(false, PatternType.STRIPE_LEFT),
            new BannerStroke(true, PatternType.BORDER)
        ));

        LETTERS.put('q', List.of(
            new BannerStroke(true, PatternType.RHOMBUS),
            new BannerStroke(false, PatternType.STRIPE_LEFT),
            new BannerStroke(false, PatternType.STRIPE_RIGHT),
            new BannerStroke(false, PatternType.SQUARE_BOTTOM_RIGHT),
            new BannerStroke(true, PatternType.BORDER)
        ));

        LETTERS.put('r', List.of(
            new BannerStroke(false, PatternType.HALF_HORIZONTAL),
            new BannerStroke(true, PatternType.STRIPE_CENTER),
            new BannerStroke(false, PatternType.STRIPE_TOP),
            new BannerStroke(false, PatternType.STRIPE_LEFT),
            new BannerStroke(false, PatternType.STRIPE_DOWNRIGHT),
            new BannerStroke(true, PatternType.BORDER)
        ));

        LETTERS.put('s', List.of(
            new BannerStroke(false, PatternType.STRIPE_DOWNRIGHT),
            new BannerStroke(false, PatternType.STRIPE_TOP),
            new BannerStroke(false, PatternType.STRIPE_BOTTOM),
            new BannerStroke(true, PatternType.CURLY_BORDER),
            new BannerStroke(true, PatternType.BORDER)
        ));

        LETTERS.put('t', List.of(
            new BannerStroke(false, PatternType.STRIPE_CENTER),
            new BannerStroke(false, PatternType.STRIPE_TOP),
            new BannerStroke(true, PatternType.BORDER)
        ));

        LETTERS.put('u', List.of(
            new BannerStroke(false, PatternType.STRIPE_LEFT),
            new BannerStroke(false, PatternType.STRIPE_RIGHT),
            new BannerStroke(false, PatternType.STRIPE_BOTTOM),
            new BannerStroke(true, PatternType.BORDER)
        ));

        LETTERS.put('v', List.of(
            new BannerStroke(false, PatternType.STRIPE_LEFT),
            new BannerStroke(true, PatternType.TRIANGLES_BOTTOM),
            new BannerStroke(false, PatternType.STRIPE_DOWNLEFT),
            new BannerStroke(true, PatternType.BORDER)
        ));

        LETTERS.put('w', List.of(
            new BannerStroke(false, PatternType.TRIANGLE_BOTTOM),
            new BannerStroke(true, PatternType.TRIANGLES_BOTTOM),
            new BannerStroke(false, PatternType.STRIPE_LEFT),
            new BannerStroke(false, PatternType.STRIPE_RIGHT),
            new BannerStroke(true, PatternType.BORDER)
        ));

        LETTERS.put('x', List.of(
            new BannerStroke(false, PatternType.STRIPE_DOWNLEFT),
            new BannerStroke(false, PatternType.STRIPE_DOWNRIGHT),
            new BannerStroke(true, PatternType.BORDER)
        ));

        LETTERS.put('y', List.of(
            new BannerStroke(false, PatternType.STRIPE_DOWNRIGHT),
            new BannerStroke(true, PatternType.HALF_HORIZONTAL_BOTTOM),
            new BannerStroke(false, PatternType.STRIPE_DOWNLEFT),
            new BannerStroke(true, PatternType.BORDER)
        ));

        LETTERS.put('z', List.of(
            new BannerStroke(false, PatternType.STRIPE_BOTTOM),
            new BannerStroke(false, PatternType.STRIPE_TOP),
            new BannerStroke(false, PatternType.STRIPE_DOWNLEFT),
            new BannerStroke(true, PatternType.BORDER)
        ));

        LETTERS.put('0', List.of(
            new BannerStroke(false, PatternType.STRIPE_LEFT),
            new BannerStroke(false, PatternType.STRIPE_RIGHT),
            new BannerStroke(false, PatternType.STRIPE_DOWNLEFT),
            new BannerStroke(false, PatternType.STRIPE_BOTTOM),
            new BannerStroke(false, PatternType.STRIPE_TOP),
            new BannerStroke(true, PatternType.BORDER)
        ));

        LETTERS.put('1', List.of(
            new BannerStroke(false, PatternType.STRIPE_BOTTOM),
            new BannerStroke(false, PatternType.STRIPE_CENTER),
            new BannerStroke(false, PatternType.SQUARE_TOP_LEFT),
            new BannerStroke(true, PatternType.BORDER)
        ));

        LETTERS.put('2', List.of(
            new BannerStroke(false, PatternType.STRIPE_TOP),
            new BannerStroke(true, PatternType.RHOMBUS),
            new BannerStroke(false, PatternType.STRIPE_BOTTOM),
            new BannerStroke(false, PatternType.STRIPE_DOWNLEFT),
            new BannerStroke(true, PatternType.BORDER)
        ));

        LETTERS.put('3', List.of(
            new BannerStroke(false, PatternType.STRIPE_BOTTOM),
            new BannerStroke(false, PatternType.STRIPE_MIDDLE),
            new BannerStroke(false, PatternType.STRIPE_TOP),
            new BannerStroke(true, PatternType.CURLY_BORDER),
            new BannerStroke(false, PatternType.STRIPE_RIGHT),
            new BannerStroke(true, PatternType.BORDER)
        ));

        LETTERS.put('4', List.of(
            new BannerStroke(false, PatternType.STRIPE_LEFT),
            new BannerStroke(true, PatternType.HALF_HORIZONTAL_BOTTOM),
            new BannerStroke(false, PatternType.STRIPE_MIDDLE),
            new BannerStroke(false, PatternType.STRIPE_RIGHT),
            new BannerStroke(true, PatternType.BORDER)
        ));

        LETTERS.put('5', List.of(
            new BannerStroke(false, PatternType.STRIPE_BOTTOM),
            new BannerStroke(true, PatternType.RHOMBUS),
            new BannerStroke(false, PatternType.STRIPE_DOWNRIGHT),
            new BannerStroke(false, PatternType.STRIPE_TOP),
            new BannerStroke(true, PatternType.BORDER)
        ));

        LETTERS.put('6', List.of(
            new BannerStroke(false, PatternType.STRIPE_BOTTOM),
            new BannerStroke(false, PatternType.STRIPE_RIGHT),
            new BannerStroke(true, PatternType.HALF_HORIZONTAL),
            new BannerStroke(false, PatternType.STRIPE_LEFT),
            new BannerStroke(false, PatternType.STRIPE_MIDDLE),
            new BannerStroke(true, PatternType.BORDER)
        ));

        LETTERS.put('7', List.of(
            new BannerStroke(false, PatternType.STRIPE_TOP),
            new BannerStroke(false, PatternType.STRIPE_DOWNLEFT),
            new BannerStroke(true, PatternType.BORDER)
        ));

        LETTERS.put('8', List.of(
            new BannerStroke(false, PatternType.STRIPE_LEFT),
            new BannerStroke(false, PatternType.STRIPE_RIGHT),
            new BannerStroke(false, PatternType.STRIPE_TOP),
            new BannerStroke(false, PatternType.STRIPE_BOTTOM),
            new BannerStroke(false, PatternType.STRIPE_MIDDLE),
            new BannerStroke(true, PatternType.BORDER)
        ));

        LETTERS.put('9', List.of(
            new BannerStroke(false, PatternType.STRIPE_LEFT),
            new BannerStroke(true, PatternType.HALF_HORIZONTAL_BOTTOM),
            new BannerStroke(false, PatternType.STRIPE_TOP),
            new BannerStroke(false, PatternType.STRIPE_RIGHT),
            new BannerStroke(false, PatternType.STRIPE_MIDDLE),
            new BannerStroke(true, PatternType.BORDER)
        ));
        
    }
    
    public static void loadColorAliases() {
        COLOR_ALIASES.clear();
        ConfigurationSection section = ConfigHandler.getInstance().getConfig().getConfigurationSection("banners-dye");
        
        if (section != null) {
            for (String colorKey : section.getKeys(false)) {
                try {
                    DyeColor dyeColor = DyeColor.valueOf(colorKey.toUpperCase());
                    for (String alias : section.getStringList(colorKey)) {
                        COLOR_ALIASES.put(alias.toLowerCase(), dyeColor);
                    }
                } catch (IllegalArgumentException e) {
                    ConsoleLogger.error("Color inválido en config.yml (banners-dye): " + colorKey);
                }
            }
        }
    }

    public static DyeColor getColorByAlias(String alias) {
        if (alias == null) return null;
        return COLOR_ALIASES.get(alias.toLowerCase());
    }

    public static Set<String> getAllColorAliases() {
        return COLOR_ALIASES.keySet();
    }

    public static boolean hasLetter(char character) {
        return LETTERS.containsKey(character);
    }

    public static ItemStack generateLetterBanner(char character, DyeColor letterColor, DyeColor backColor) {
        List<BannerStroke> strokes = LETTERS.get(character);
        if (strokes == null) return null; 

        DyeColor baseColor = (character == 'q') ? letterColor : backColor;
        Material bannerMat = Material.valueOf(baseColor.name() + "_BANNER");
        ItemStack banner = new ItemStack(bannerMat);
        BannerMeta meta = (BannerMeta) banner.getItemMeta();

        for (BannerStroke stroke : strokes) {
            DyeColor patternColor = stroke.isBackground() ? backColor : letterColor;
            meta.addPattern(new Pattern(patternColor, stroke.type()));
        }

        banner.setItemMeta(meta);
        return banner;
    }

}
