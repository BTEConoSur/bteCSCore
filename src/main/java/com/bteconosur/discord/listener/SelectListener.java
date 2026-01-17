package com.bteconosur.discord.listener;

import javax.annotation.Nonnull;

import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.util.ConsoleLogger;
import com.bteconosur.db.model.DiscordInteraction;
import com.bteconosur.db.registry.DiscordInteractionRegistry;
import com.bteconosur.discord.action.SelectAction;

import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class SelectListener extends ListenerAdapter {

    private static final YamlConfiguration lang = ConfigHandler.getInstance().getLang();
    private static final DiscordInteractionRegistry registry = DiscordInteractionRegistry.getInstance();

    @SuppressWarnings("null")
    @Override
    public void onStringSelectInteraction(@Nonnull StringSelectInteractionEvent event) {
        String selectId = event.getComponentId();
        if (selectId == null || selectId.isBlank()) return;

        DiscordInteraction ctx = registry.findByComponentId(selectId);
        if (ctx == null) ctx = registry.findByMessageId(event.getMessage().getIdLong());
        
        if (ctx == null) {
            ConsoleLogger.warn("Error de Discord: Interacción de selector con ID '" + selectId + "' no encontrada en el registro.");
            return;
        }

        if (ctx.isExpired()) {
            ConsoleLogger.debug("Interacción de selector expirada: " + selectId + ", " + ctx.getInteractionKey());
            event.reply(lang.getString("discord-interaction-expired")).setEphemeral(true).queue();
            registry.removeInteraction(ctx);
            return;
        }

        SelectAction action = registry.getSelectAction(ctx.getInteractionKey());
        if (action == null) {
            ConsoleLogger.warn("Error de Discord: No hay una acción de selector registrada para la clave: " + ctx.getInteractionKey());
            event.reply(lang.getString("discord-internal-error")).setEphemeral(true).queue();
            return;
        }

        try {
            action.handle(event, ctx);
        } catch (Exception e) {
            ConsoleLogger.error("Error de Discord: Error al manejar la interacción de selector: " + e.getMessage());
            event.reply(lang.getString("discord-internal-error")).setEphemeral(true).queue();
        }
    }

}
