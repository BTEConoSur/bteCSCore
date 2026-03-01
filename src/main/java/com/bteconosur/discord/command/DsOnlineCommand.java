package com.bteconosur.discord.command;

import com.bteconosur.core.chat.ChatUtil;
import com.bteconosur.core.config.Language;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.discord.util.CommandMode;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class DsOnlineCommand extends DsCommand {

    public DsOnlineCommand() {
        super("online", "Muestra los jugadores en línea en el servidor de Minecraft.", 
            null,
            null,
            CommandMode.GLOBAL
        );
    }

    @SuppressWarnings("null")
    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Long userId = event.getUser().getIdLong();
        Player player = PlayerRegistry.getInstance().findByDiscordId(userId);
        Language language = player != null ? player.getLanguage() : Language.getDefault();
        
        event.replyEmbeds(ChatUtil.getDsOnline(language)).setEphemeral(true).queue();
    }

}
