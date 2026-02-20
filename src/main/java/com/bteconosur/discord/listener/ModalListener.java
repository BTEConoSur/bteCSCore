package com.bteconosur.discord.listener;

import javax.annotation.Nonnull;

import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.ConsoleLogger;
import com.bteconosur.db.model.Interaction;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.InteractionRegistry;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.db.util.InteractionKey;
import com.bteconosur.discord.action.ModalAction;

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ModalListener extends ListenerAdapter {

    private static final InteractionRegistry registry = InteractionRegistry.getInstance();

    @SuppressWarnings("null")
    @Override
    public void onModalInteraction(@Nonnull ModalInteractionEvent event) {
        String modalId = event.getModalId();
        if (modalId == null || modalId.isBlank()) return;
        Player player = PlayerRegistry.getInstance().findByDiscordId(event.getUser().getIdLong());
        Language language = player != null ? player.getLanguage() : Language.getDefault();
        Interaction ctx = registry.findByComponentId(modalId);
        if (ctx == null) {
            ConsoleLogger.warn(LanguageHandler.getText("ds-error.modal-interaction")
                .replace("%modalId%", modalId)
                .replace("%messageId%", event.getMessage() != null ? event.getMessage().getId() : "null"));
            event.reply(LanguageHandler.getText(language, "ds-interaction-expired")).setEphemeral(true).queue();
            return;
        }

        if (ctx.isExpired()) {
            ConsoleLogger.debug("Interacción de modal expirada: " + modalId + ", " + ctx.getInteractionKey());
            InteractionRegistry ir = InteractionRegistry.getInstance();
            if (ctx.getInteractionKey() == InteractionKey.ACCEPT_CREATE_PROJECT) ir.unload(ctx.getId());
            if (ctx.getInteractionKey() == InteractionKey.REJECT_CREATE_PROJECT) ir.unload(ctx.getId());
            if (ctx.getInteractionKey() == InteractionKey.ACCEPT_REDEFINE_PROJECT) ir.unload(ctx.getId());
            if (ctx.getInteractionKey() == InteractionKey.REJECT_REDEFINE_PROJECT) ir.unload(ctx.getId());
            event.reply(LanguageHandler.getText(language, "ds-interaction-expired")).setEphemeral(true).queue();
            return;
        }

        ModalAction action = registry.getModalAction(ctx.getInteractionKey());
        if (action == null) {
            ConsoleLogger.warn(LanguageHandler.getText("ds-error.modal-action-not-found").replace("%interactionKey%", ctx.getInteractionKey().name())  );
            event.reply(LanguageHandler.getText(language, "discord-internal-error")).setEphemeral(true).queue();
            return;
        }

        try {
            action.handle(event, ctx);
        } catch (Exception e) {
            ConsoleLogger.error(LanguageHandler.getText("ds-error.modal-internal-error") + e.getMessage());
            event.reply(LanguageHandler.getText(language, "discord-internal-error")).setEphemeral(true).queue();
            e.printStackTrace();
        }
    }

}
