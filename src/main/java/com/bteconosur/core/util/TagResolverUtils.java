package com.bteconosur.core.util;

import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

public class TagResolverUtils {

    public static TagResolver getCopyableText(String key, String textToCopy, String displayText, Language language) {
        Component clickable = MiniMessage.miniMessage().deserialize(displayText)
            .clickEvent(ClickEvent.copyToClipboard(textToCopy))
            .hoverEvent(HoverEvent.showText(MiniMessage.miniMessage().deserialize(LanguageHandler.getText(language, "placeholder.tag-resolver.click-to-copy"))));
        return Placeholder.component(key, clickable);
    }

    public static TagResolver getLinkText(String key, String url, String displayText, Language language) {
        Component link = MiniMessage.miniMessage().deserialize(displayText)
            .clickEvent(ClickEvent.openUrl(url))
            .hoverEvent(HoverEvent.showText(MiniMessage.miniMessage().deserialize(LanguageHandler.getText(language, "placeholder.tag-resolver.click-to-open"))));
        return Placeholder.component(key, link);
    }

    public static TagResolver getCommandText(String key, String command, String displayText, String hoverText) {
        Component commandComponent = MiniMessage.miniMessage().deserialize(displayText)
            .clickEvent(ClickEvent.runCommand(command))
            .hoverEvent(HoverEvent.showText(MiniMessage.miniMessage().deserialize(hoverText)));
        return Placeholder.component(key, commandComponent);
    }

}
