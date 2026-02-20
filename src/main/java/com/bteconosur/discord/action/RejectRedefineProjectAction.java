package com.bteconosur.discord.action;

import com.bteconosur.core.ProjectManager;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.ConsoleLogger;
import com.bteconosur.db.model.Interaction;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.InteractionRegistry;
import com.bteconosur.db.registry.PlayerRegistry;

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;

public class RejectRedefineProjectAction implements ModalAction {
    
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
            ConsoleLogger.debug("Debug: Parent context not found for RejectRedefineProjectAction, parentCtxId: " + parentCtxId);
            event.reply(LanguageHandler.getText(language, "discord-interaction-expired")).setEphemeral(true).queue();
            return;
        }
        ir.unload(ctx.getId());
        pm.rejectRedefineRequest(parentCtx.getProjectId(), player, parentCtxId, comentario); 
        event.reply(LanguageHandler.getText(language, "project.redefine.reject.ds-success")).setEphemeral(true).queue();
    }

}
