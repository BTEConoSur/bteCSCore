package com.bteconosur.discord.command;

import java.util.Arrays;

import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.discord.util.CommandMode;
import com.bteconosur.discord.util.MessageService;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;

public class DsContextDeleteChatCommand extends DsContextMessageCommand {

    public DsContextDeleteChatCommand() {
        super("Borrar Global Chat", 
            Arrays.asList(Permission.ADMINISTRATOR),
            CommandMode.COUNTRY_ONLY
        );
    }

    @SuppressWarnings("null")
    @Override
    public void execute(MessageContextInteractionEvent event) {
        Player player = PlayerRegistry.getInstance().findByDiscordId(event.getUser().getIdLong());
        Language language = player != null ? player.getLanguage() : Language.getDefault();
        Boolean success = MessageService.deleteByMessageId(event.getTarget().getIdLong());
        event.reply(LanguageHandler.getText(language, success ? "ds-delete-global-chat.success" : "ds-delete-global-chat.not-found")).setEphemeral(true).queue();
    }

}
