package com.bteconosur.discord.listener;

import javax.annotation.Nonnull;

import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.BTEConoSur;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.util.ConsoleLogger;
import com.bteconosur.db.model.DiscordInteraction;
import com.bteconosur.db.registry.DiscordInteractionRegistry;
import com.bteconosur.discord.action.ModalAction;

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ModalListener extends ListenerAdapter {

    private static final ConsoleLogger logger = BTEConoSur.getConsoleLogger();
    private static final YamlConfiguration lang = ConfigHandler.getInstance().getLang();
    private static final DiscordInteractionRegistry registry = DiscordInteractionRegistry.getInstance();

    @SuppressWarnings("null")
    @Override
    public void onModalInteraction(@Nonnull ModalInteractionEvent event) {
        String modalId = event.getModalId();
        if (modalId == null || modalId.isBlank()) return;

        DiscordInteraction ctx = registry.findByComponentId(modalId);
        if (ctx == null) {
            logger.warn("Error de Discord: Interacción de modal con ID '" + modalId + "' no encontrada en el registro.");
            return;
        }

        if (ctx.isExpired()) {
            logger.debug("Interacción de modal expirada: " + modalId + ", " + ctx.getInteractionKey());
            event.reply(lang.getString("discord-interaction-expired")).setEphemeral(true).queue();
            registry.removeInteraction(ctx);
            return;
        }

        ModalAction action = registry.getModalAction(ctx.getInteractionKey());
        if (action == null) {
            logger.warn("Error de Discord: No hay una acción de modal registrada para la clave: " + ctx.getInteractionKey());
            event.reply(lang.getString("discord-internal-error")).setEphemeral(true).queue();
            return;
        }

        try {
            action.handle(event, ctx);
        } catch (Exception e) {
            logger.error("Error de Discord: Error al manejar la interacción de modal: " + e.getMessage());
            event.reply(lang.getString("discord-internal-error")).setEphemeral(true).queue();
        }
    }

}
