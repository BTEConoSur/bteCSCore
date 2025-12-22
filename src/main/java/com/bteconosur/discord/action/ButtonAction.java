package com.bteconosur.discord.action;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import com.bteconosur.db.model.DiscordInteraction;

public interface ButtonAction {
    void handle(ButtonInteractionEvent event, DiscordInteraction ctx);
}
