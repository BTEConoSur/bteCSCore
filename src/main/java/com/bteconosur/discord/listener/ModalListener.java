package com.bteconosur.discord.listener;

import javax.annotation.Nonnull;

import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.util.ConsoleLogger;
import com.bteconosur.db.model.Interaction;
import com.bteconosur.db.registry.InteractionRegistry;
import com.bteconosur.db.util.InteractionKey;
import com.bteconosur.discord.action.ModalAction;

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ModalListener extends ListenerAdapter {

    private static final YamlConfiguration lang = ConfigHandler.getInstance().getLang();
    private static final InteractionRegistry registry = InteractionRegistry.getInstance();

    @SuppressWarnings("null")
    @Override
    public void onModalInteraction(@Nonnull ModalInteractionEvent event) {
        String modalId = event.getModalId();
        if (modalId == null || modalId.isBlank()) return;

        Interaction ctx = registry.findByComponentId(modalId);
        if (ctx == null) {
            ConsoleLogger.warn("Error de Discord: Interacción de modal con ID '" + modalId + "' / mensaje con ID '" + event.getMessage().getId() + "' no encontrada en el registro.");
            event.reply(lang.getString("discord-interaction-expired")).setEphemeral(true).queue();
            return;
        }

        if (ctx.isExpired()) {
            ConsoleLogger.debug("Interacción de modal expirada: " + modalId + ", " + ctx.getInteractionKey());
            InteractionRegistry ir = InteractionRegistry.getInstance();
            if (ctx.getInteractionKey() == InteractionKey.ACCEPT_CREATE_PROJECT) ir.unload(ctx.getId());
            if (ctx.getInteractionKey() == InteractionKey.REJECT_CREATE_PROJECT) ir.unload(ctx.getId());
            event.reply(lang.getString("discord-interaction-expired")).setEphemeral(true).queue();
            return;
        }

        ModalAction action = registry.getModalAction(ctx.getInteractionKey());
        if (action == null) {
            ConsoleLogger.warn("Error de Discord: No hay una acción de modal registrada para la clave: " + ctx.getInteractionKey());
            event.reply(lang.getString("discord-internal-error")).setEphemeral(true).queue();
            return;
        }

        try {
            action.handle(event, ctx);
        } catch (Exception e) {
            ConsoleLogger.error("Error de Discord: Error al manejar la interacción de modal: " + e.getMessage());
            event.reply(lang.getString("discord-internal-error")).setEphemeral(true).queue();
            e.printStackTrace();
        }
    }

}
