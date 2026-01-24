package com.bteconosur.discord.action;

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import com.bteconosur.db.model.Interaction;

public interface ModalAction {
    void handle(ModalInteractionEvent event, Interaction ctx);
}
