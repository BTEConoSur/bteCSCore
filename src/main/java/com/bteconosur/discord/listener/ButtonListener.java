package com.bteconosur.discord.listener;

import javax.annotation.Nonnull;

import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.util.ConsoleLogger;
import com.bteconosur.db.model.Interaction;
import com.bteconosur.db.registry.InteractionRegistry;
import com.bteconosur.discord.action.ButtonAction;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ButtonListener extends ListenerAdapter {

    private static final YamlConfiguration lang = ConfigHandler.getInstance().getLang();
    private static final InteractionRegistry registry = InteractionRegistry.getInstance();

    @SuppressWarnings("null")
    @Override
    public void onButtonInteraction(@Nonnull ButtonInteractionEvent event) {
        String buttonId = event.getComponentId();
        if (buttonId == null || buttonId.isBlank()) return;

        Interaction ctx = registry.findByComponentId(buttonId);
        if (ctx == null) ctx = registry.findByMessageId(event.getMessage().getIdLong());
        
        if (ctx == null) {
            ConsoleLogger.warn("Error de Discord: Interacción de botón con ID ''" + buttonId + "'no encontrada en el registro.");
            return;
        }

        if (ctx.isExpired()) {
            ConsoleLogger.debug("Interacción de botón expirada: " + buttonId + ", " + ctx.getInteractionKey());
            event.reply(lang.getString("discord-interaction-expired")).setEphemeral(true).queue();
            registry.removeInteraction(ctx);
            return;
        }

        ButtonAction action = registry.getButtonAction(ctx.getInteractionKey());
        if (action == null) {
            ConsoleLogger.warn("Error de Discord: No hay una acción de botón registrada para la clave: " + ctx.getInteractionKey());
            event.reply(lang.getString("discord-internal-error")).setEphemeral(true).queue();
            return;
        }

        try {
            action.handle(event, ctx);
        } catch (Exception e) {
            ConsoleLogger.error("Error de Discord: Error al manejar la interacción de botón: " + e.getMessage());
            event.reply(lang.getString("discord-internal-error")).setEphemeral(true).queue();
        }
    }

}
