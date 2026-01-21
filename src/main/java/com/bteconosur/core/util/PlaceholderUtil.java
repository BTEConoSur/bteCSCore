package com.bteconosur.core.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

public class PlaceholderUtil {

    public static TagResolver getClickableText(String key, String textToCopy, String displayText) {
        Component clickable = Component.text(displayText)
            .clickEvent(ClickEvent.copyToClipboard(textToCopy))
            .hoverEvent(HoverEvent.showText(Component.text("Click para copiar").color(NamedTextColor.GRAY)));
        return Placeholder.component(key, clickable);
    }

}
