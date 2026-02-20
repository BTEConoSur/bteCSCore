package com.bteconosur.discord.action;

import java.time.Instant;

import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.db.PermissionManager;
import com.bteconosur.db.model.Interaction;
import com.bteconosur.db.model.Pais;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.InteractionRegistry;
import com.bteconosur.db.registry.PaisRegistry;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.db.util.InteractionKey;

import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.modals.Modal;

public class RedefineProjectAction implements ButtonAction {

    private final YamlConfiguration config = ConfigHandler.getInstance().getConfig();

    @SuppressWarnings("null")
    @Override
    public void handle(ButtonInteractionEvent event, Interaction ctx) {
        String buttonId = event.getComponentId();
        Player player = PlayerRegistry.getInstance().findByDiscordId(event.getUser().getIdLong());
        Language language = player != null ? player.getLanguage() : Language.getDefault();
        if (player == null) {
            event.reply(LanguageHandler.getText(language, "link.ds-link-needed")).setEphemeral(true).queue();
            return;
        }
        Pais pais = PaisRegistry.getInstance().findByRequestId(event.getChannelIdLong());
        if (!PermissionManager.getInstance().isReviewer(player)) {
            event.reply(LanguageHandler.replaceDS("reviewer.ds-not-reviewer-country", language, pais)).setEphemeral(true).queue();
            return;
        }

        TextInput comentario = TextInput.create("comentario", TextInputStyle.PARAGRAPH)
            .setPlaceholder(LanguageHandler.getText(language, "ds-field-comment-placeholder"))
            .setRequired(false)
            .setMaxLength(300)
            .build();

        int expiration = config.getInt("interaction-expirations.accept-reject-redefine-project");

        if (buttonId.equals("accept")) {
            String modalId = "accept_redefine_project:" + ctx.getProjectId();
            Modal modal = Modal.create(modalId, LanguageHandler.getText(language, "ds-modals.accept-project-redefinition"))
                .addComponents(Label.of(LanguageHandler.getText(language, "ds-field-comment-name"), comentario))
                .build();
            Interaction ctxAccept = new Interaction(
                InteractionKey.ACCEPT_REDEFINE_PROJECT,
                Instant.now(),
                Instant.now().plusSeconds(expiration * 60L)
            );
            ctxAccept.setComponentId(modalId);
            ctxAccept.addPayloadValue("parentCtxId", ctx.getId());
            InteractionRegistry.getInstance().load(ctxAccept);
            event.replyModal(modal).queue();
        } else if (buttonId.equals("cancel")) {
            String modalId = "reject_redefine_project:" + ctx.getProjectId();
            Modal modal = Modal.create(modalId, LanguageHandler.getText(language, "ds-modals.reject-project-redefinition"))
                .addComponents(Label.of(LanguageHandler.getText(language, "ds-field-comment-name"), comentario))
                .build();
            Interaction ctxAccept = new Interaction(
                InteractionKey.REJECT_REDEFINE_PROJECT,
                Instant.now(),
                Instant.now().plusSeconds(expiration * 60L)
            );
            ctxAccept.setComponentId(modalId);
            ctxAccept.addPayloadValue("parentCtxId", ctx.getId());
            InteractionRegistry.getInstance().load(ctxAccept);
            event.replyModal(modal).queue();
        } else {
            event.reply(LanguageHandler.getText(language, "ds-invalid-action")).setEphemeral(true).queue();
        }
    }

}
