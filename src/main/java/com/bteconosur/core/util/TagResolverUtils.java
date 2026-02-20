package com.bteconosur.core.util;

import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

public class TagResolverUtils {

    public static TagResolver getCopyableText(String key, String textToCopy, String displayText, Language language) {
        Component clickable = Component.text(displayText)
            .clickEvent(ClickEvent.copyToClipboard(textToCopy))
            .hoverEvent(HoverEvent.showText(Component.text(LanguageHandler.getText(language, "placeholder.tag-resolver.click-to-copy")).color(NamedTextColor.GRAY)));
        return Placeholder.component(key, clickable);
    }

    public static TagResolver getLinkText(String key, String url, String displayText, Language language) {
        Component link = Component.text(displayText)
            .clickEvent(ClickEvent.openUrl(url))
            .hoverEvent(HoverEvent.showText(Component.text(LanguageHandler.getText(language, "placeholder.tag-resolver.click-to-open")).color(NamedTextColor.GRAY)));
        return Placeholder.component(key, link);
    }

}
