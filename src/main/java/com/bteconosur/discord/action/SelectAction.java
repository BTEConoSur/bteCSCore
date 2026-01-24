package com.bteconosur.discord.action;

import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import com.bteconosur.db.model.Interaction;

public interface SelectAction {
    void handle(StringSelectInteractionEvent event, Interaction ctx);
}
