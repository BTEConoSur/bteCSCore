package com.bteconosur.discord.listener;

import javax.annotation.Nonnull;

import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.ConsoleLogger;
import com.bteconosur.db.model.Interaction;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.InteractionRegistry;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.discord.action.SelectAction;

import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class SelectListener extends ListenerAdapter {

    private static final InteractionRegistry registry = InteractionRegistry.getInstance();

    @SuppressWarnings("null")
    @Override
    public void onStringSelectInteraction(@Nonnull StringSelectInteractionEvent event) {
        String selectId = event.getComponentId();
        if (selectId == null || selectId.isBlank()) return;

        Interaction ctx = registry.findByComponentId(selectId);
        if (ctx == null) ctx = registry.findByMessageId(event.getMessage().getIdLong());
        Player player = PlayerRegistry.getInstance().findByDiscordId(event.getUser().getIdLong());
        Language language = player != null ? player.getLanguage() : Language.getDefault();
        if (ctx == null) {
            ConsoleLogger.warn(LanguageHandler.getText("ds-error.selector-interaction")
                .replace("%selectId%", selectId)
                .replace("%messageId%", event.getMessage().getId())
            );
            event.reply(LanguageHandler.getText(language, "ds-interaction-expired")).setEphemeral(true).queue();
            return;
        }

        if (ctx.isExpired()) {
            ConsoleLogger.debug("Interacción de selector expirada: " + selectId + ", " + ctx.getInteractionKey());
            event.reply(LanguageHandler.getText(language, "ds-interaction-expired")).setEphemeral(true).queue();
            return;
        }

        SelectAction action = registry.getSelectAction(ctx.getInteractionKey());
        if (action == null) {
            ConsoleLogger.warn(LanguageHandler.getText("ds-error.select-action-not-found").replace("%interactionKey%", ctx.getInteractionKey().name()));
            event.reply(LanguageHandler.getText(language, "discord-internal-error")).setEphemeral(true).queue();
            return;
        }

        try {
            action.handle(event, ctx);
        } catch (Exception e) {
            ConsoleLogger.error(LanguageHandler.getText("ds-error.select-internal-error") + e.getMessage());
            event.reply(LanguageHandler.getText(language, "discord-internal-error")).setEphemeral(true).queue();
        }
    }

}
