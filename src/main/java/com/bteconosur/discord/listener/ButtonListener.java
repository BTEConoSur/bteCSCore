package com.bteconosur.discord.listener;

import javax.annotation.Nonnull;

import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.BTEConoSur;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.util.ConsoleLogger;
import com.bteconosur.db.model.DiscordInteraction;
import com.bteconosur.db.registry.DiscordInteractionRegistry;
import com.bteconosur.discord.action.ButtonAction;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ButtonListener extends ListenerAdapter {

    private static final ConsoleLogger logger = BTEConoSur.getConsoleLogger();
    private static final YamlConfiguration lang = ConfigHandler.getInstance().getLang();
    private static final DiscordInteractionRegistry registry = DiscordInteractionRegistry.getInstance();

    @SuppressWarnings("null")
    @Override
    public void onButtonInteraction(@Nonnull ButtonInteractionEvent event) {
        String buttonId = event.getComponentId();
        if (buttonId == null || buttonId.isBlank()) return;

        DiscordInteraction ctx = registry.findByComponentId(buttonId);
        if (ctx == null) ctx = registry.findByMessageId(event.getMessage().getIdLong());
        
        if (ctx == null) {
            logger.warn("Error de Discord: Interacción de botón con ID ''" + buttonId + "'no encontrada en el registro.");
            return;
        }

        if (ctx.isExpired()) {
            logger.debug("Interacción de botón expirada: " + buttonId + ", " + ctx.getInteractionKey());
            event.reply(lang.getString("discord-interaction-expired")).setEphemeral(true).queue();
            registry.removeInteraction(ctx);
            return;
        }

        ButtonAction action = registry.getButtonAction(ctx.getInteractionKey());
        if (action == null) {
            logger.warn("Error de Discord: No hay una acción de botón registrada para la clave: " + ctx.getInteractionKey());
            event.reply(lang.getString("discord-internal-error")).setEphemeral(true).queue();
            return;
        }

        try {
            action.handle(event, ctx);
        } catch (Exception e) {
            logger.error("Error de Discord: Error al manejar la interacción de botón: " + e.getMessage());
            event.reply(lang.getString("discord-internal-error")).setEphemeral(true).queue();
        }
    }

}
