package com.bteconosur.discord.listener;

import javax.annotation.Nonnull;

import com.bteconosur.core.ProjectManager;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.ConsoleLogger;
import com.bteconosur.db.model.Interaction;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.InteractionRegistry;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.db.util.InteractionKey;
import com.bteconosur.discord.action.ButtonAction;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ButtonListener extends ListenerAdapter {

    private static final InteractionRegistry registry = InteractionRegistry.getInstance();

    @SuppressWarnings("null")
    @Override
    public void onButtonInteraction(@Nonnull ButtonInteractionEvent event) {
        String buttonId = event.getComponentId();
        String messageId = event.getMessage().getId();
        if (buttonId == null || buttonId.isBlank()) return;
        
        Interaction ctx = registry.findByComponentId(buttonId);
        if (ctx == null) ctx = registry.findByMessageId(event.getMessage().getIdLong());
        Player player = PlayerRegistry.getInstance().findByDiscordId(event.getUser().getIdLong());
        Language language = player != null ? player.getLanguage() : Language.getDefault();
        if (ctx == null) {
            ConsoleLogger.warn(LanguageHandler.getText("ds-error.button-interaction")
                .replace("%buttonId%", buttonId)
                .replace("%messageId%", messageId)
            );
            event.reply(LanguageHandler.getText(language, "ds-interaction-expired")).setEphemeral(true).queue();
            return;
        }

        if (ctx.isExpired()) {
            ConsoleLogger.debug("Interacción de botón expirada: " + buttonId + ", " + ctx.getInteractionKey());
            if (ctx.getInteractionKey() == InteractionKey.CREATE_PROJECT) ProjectManager.getInstance().expiredCreateRequest(ctx.getProjectId(), ctx.getId());
            if (ctx.getInteractionKey() == InteractionKey.REDEFINE_PROJECT) ProjectManager.getInstance().expiredRedefineRequest(ctx.getProjectId(), ctx.getId());
            if (ctx.getInteractionKey() == InteractionKey.JOIN_PROJECT) ProjectManager.getInstance().expiredJoinRequest(messageId, ctx.getPlayerId());
            event.reply(LanguageHandler.getText(language, "ds-interaction-expired")).setEphemeral(true).queue();
            return;
        }

        ButtonAction action = registry.getButtonAction(ctx.getInteractionKey());
        if (action == null) {
            ConsoleLogger.warn(LanguageHandler.getText("ds-error.button-action-not-found").replace("%interactionKey%", ctx.getInteractionKey().name())  );
            event.reply(LanguageHandler.getText(language, "ds-internal-error")).setEphemeral(true).queue();
            return;
        }

        try {
            action.handle(event, ctx);
        } catch (Exception e) {
            ConsoleLogger.error(LanguageHandler.getText("ds-error.button-internal-error") + e.getMessage());
            e.printStackTrace();
            event.reply(LanguageHandler.getText(language, "ds-internal-error")).setEphemeral(true).queue();
        }
    }

}
