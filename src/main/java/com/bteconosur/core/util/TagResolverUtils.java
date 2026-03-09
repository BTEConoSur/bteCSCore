package com.bteconosur.core.util;

import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

/**
 * Utilidad para crear resolvedores de tags personalizados en mensajes MiniMessage.
 * Proporciona factory methods para tags interactivos como copiar, enlaces, comandos
 * y hovers con soporte para localización.
 */
public class TagResolverUtils {

    /**
     * Crea un tag de texto copiable al portapapeles.
     *
     * @param key clave del tag para usar en MiniMessage.
     * @param textToCopy texto a copiar al hacer clic.
     * @param displayText texto a mostrar en el mensaje.
     * @param language idioma para menúsaje de ayuda.
     * @return resolvedor de tag con comportamiento de copia.
     */
    public static TagResolver getCopyableText(String key, String textToCopy, String displayText, Language language) {
        Component clickable = MiniMessage.miniMessage().deserialize(displayText)
            .clickEvent(ClickEvent.copyToClipboard(textToCopy))
            .hoverEvent(HoverEvent.showText(MiniMessage.miniMessage().deserialize(LanguageHandler.getText(language, "placeholder.tag-resolver.click-to-copy"))));
        return Placeholder.component(key, clickable);
    }

    /**
     * Crea un tag de enlace URL.
     *
     * @param key clave del tag para usar en MiniMessage.
     * @param url URL a abrir al hacer clic.
     * @param displayText texto a mostrar en el mensaje.
     * @param language idioma para menúsaje de ayuda.
     * @return resolvedor de tag con comportamiento de enlace.
     */
    public static TagResolver getLinkText(String key, String url, String displayText, Language language) {
        Component link = MiniMessage.miniMessage().deserialize(displayText)
            .clickEvent(ClickEvent.openUrl(url))
            .hoverEvent(HoverEvent.showText(MiniMessage.miniMessage().deserialize(LanguageHandler.getText(language, "placeholder.tag-resolver.click-to-open"))));
        return Placeholder.component(key, link);
    }

    /**
     * Crea un tag de comando que ejecuta un comando al hacer clic.
     *
     * @param key clave del tag para usar en MiniMessage.
     * @param command comando a ejecutar al hacer clic.
     * @param displayText texto a mostrar en el mensaje.
     * @param hoverText texto a mostrar al pasar el ratón encima.
     * @return resolvedor de tag con comportamiento de comando.
     */
    public static TagResolver getCommandText(String key, String command, String displayText, String hoverText) {
        Component commandComponent = MiniMessage.miniMessage().deserialize(displayText)
            .clickEvent(ClickEvent.runCommand(command))
            .hoverEvent(HoverEvent.showText(MiniMessage.miniMessage().deserialize(hoverText)));
        return Placeholder.component(key, commandComponent);
    }

    /**
     * Crea un tag de texto con efecto hover (al pasar el ratón).
     *
     * @param key clave del tag para usar en MiniMessage.
     * @param displayText texto a mostrar en el mensaje.
     * @param hoverText texto a mostrar al pasar el ratón encima.
     * @return resolvedor de tag con comportamiento hover.
     */
    public static TagResolver getHoverText(String key, String displayText, String hoverText) {
        Component hoverComponent = MiniMessage.miniMessage().deserialize(displayText)
            .hoverEvent(HoverEvent.showText(MiniMessage.miniMessage().deserialize(hoverText)));
        return Placeholder.component(key, hoverComponent);
    }

}
