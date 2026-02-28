package com.bteconosur.discord.listener;

import javax.annotation.Nonnull;

import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.ConsoleLogger;
import com.bteconosur.discord.command.DsCommandManager;
import com.bteconosur.discord.command.DsContextMessageCommand;
import com.bteconosur.discord.command.DsContextUserCommand;

import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ContextCommandListener extends ListenerAdapter {

    @SuppressWarnings("null")
    @Override
    public void onMessageContextInteraction(@Nonnull MessageContextInteractionEvent event) {
        String commandName = event.getName();

        DsCommandManager commandManager = DsCommandManager.getInstance();
        DsContextMessageCommand command = commandManager.getContextMessageCommand(commandName);

        if (command != null) {
            command.execute(event);
            return;
        } else ConsoleLogger.warn(LanguageHandler.getText("ds-error.context-message-command-not-found").replace("%command%", commandName));
        
        event.reply(LanguageHandler.getText("ds-internal-error")).setEphemeral(true).queue();
    }

    @SuppressWarnings("null")
    @Override
    public void onUserContextInteraction(@Nonnull UserContextInteractionEvent event) {
        String commandName = event.getName();

        DsCommandManager commandManager = DsCommandManager.getInstance();
        DsContextUserCommand command = commandManager.getContextUserCommand(commandName);

        if (command != null) {
            command.execute(event);
            return;
        } else ConsoleLogger.warn(LanguageHandler.getText("ds-error.context-user-command-not-found").replace("%command%", commandName));
        
        event.reply(LanguageHandler.getText("ds-internal-error")).setEphemeral(true).queue();
    }

}
