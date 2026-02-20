package com.bteconosur.discord.action;

import com.bteconosur.core.ProjectManager;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.db.model.Interaction;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.InteractionRegistry;
import com.bteconosur.db.registry.PlayerRegistry;

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;

public class AcceptRedefineProjectAction implements ModalAction {

    @SuppressWarnings("null")
    @Override
    public void handle(ModalInteractionEvent event, Interaction ctx) {
        String comentario = event.getValue("comentario").getAsString();
        ProjectManager pm = ProjectManager.getInstance();
        Player player = PlayerRegistry.getInstance().findByDiscordId(event.getUser().getIdLong());
        Language language = player != null ? player.getLanguage() : Language.getDefault();
        if (player == null) {
            event.reply(LanguageHandler.getText(language, "link.ds-link-needed")).setEphemeral(true).queue();
            return;
        }

        Long parentCtxId = ((Number) ctx.getPayloadValue("parentCtxId")).longValue();
        InteractionRegistry ir = InteractionRegistry.getInstance();
        Interaction parentCtx = ir.get(parentCtxId);
        if (parentCtx == null) {
            event.reply(LanguageHandler.getText(language, "ds-interaction-expired")).setEphemeral(true).queue();
            return;
        }
        ir.unload(ctx.getId());
        pm.acceptRedefineRequest(parentCtx.getProjectId(), player, parentCtxId, comentario);
        event.reply(LanguageHandler.getText(language, "project.redefine.accept.ds-success")).setEphemeral(true).queue();
    }
}
