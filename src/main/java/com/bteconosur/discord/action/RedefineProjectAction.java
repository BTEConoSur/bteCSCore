package com.bteconosur.discord.action;

import java.time.Instant;

import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.config.ConfigHandler;
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

    private final YamlConfiguration lang = ConfigHandler.getInstance().getLang();
    private final YamlConfiguration config = ConfigHandler.getInstance().getConfig();

    @SuppressWarnings("null")
    @Override
    public void handle(ButtonInteractionEvent event, Interaction ctx) {
        String buttonId = event.getComponentId();
        Player player = PlayerRegistry.getInstance().findByDiscordId(event.getUser().getIdLong());
        if (player == null) {
            event.reply(lang.getString("discord-link-needed")).setEphemeral(true).queue();
            return;
        }
        Pais pais = PaisRegistry.getInstance().findByRequestId(event.getChannelIdLong());
        if (!PermissionManager.getInstance().isReviewer(player)) {
            event.reply(lang.getString("ds-reviewer-needed").replace("%pais%", pais.getNombrePublico())).setEphemeral(true).queue();
            return;
        }

        TextInput comentario = TextInput.create("comentario", TextInputStyle.PARAGRAPH)
            .setPlaceholder("(Opcional) Agrega un comentario sobre el proyecto...")
            .setRequired(false)
            .setMaxLength(300)
            .build();

        int expiration = config.getInt("interaction-expirations.accept-reject-redefine-project");

        if (buttonId.equals("accept")) {
            String modalId = "accept_redefine_project:" + ctx.getProjectId();
            Modal modal = Modal.create(modalId, "Confirmar redefinición de proyecto")
                .addComponents(Label.of("Comentario", comentario))
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
            Modal modal = Modal.create(modalId, "Confirmar rechazo de redefinición de proyecto")
                .addComponents(Label.of("Comentario", comentario))
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
            event.reply(lang.getString("discord-invalid-action")).setEphemeral(true).queue();
        }
    }

}
