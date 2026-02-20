package com.bteconosur.discord.listener;

import javax.annotation.Nonnull;

import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.ConsoleLogger;
import com.bteconosur.discord.command.DsCommand;
import com.bteconosur.discord.command.DsCommandManager;
import com.bteconosur.discord.command.DsSubcommand;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class SlashCommandListener extends ListenerAdapter {

    @SuppressWarnings("null")
    @Override
    public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {
        String commandName = event.getName();
        String subCommandName = event.getSubcommandName();

        DsCommandManager commandManager = DsCommandManager.getInstance();
        DsCommand command = commandManager.getCommand(commandName);
        
        if (subCommandName != null && command != null) {
            DsSubcommand subcommand = command.getSubcommands().get(subCommandName);
            if (subcommand != null) {
                subcommand.execute(event);
                return;
            }
            ConsoleLogger.warn(LanguageHandler.getText("ds-error.subcommand-not-found")
                .replace("%command%", commandName)
                .replace("%subcommand%", subCommandName)
            );
        }

        if (command != null) {
            command.execute(event);
            return;
        } else ConsoleLogger.warn(LanguageHandler.getText("ds-error.command-not-found").replace("%command%", commandName));
        
        event.reply(LanguageHandler.getText("ds-internal-error")).setEphemeral(true).queue();
    }

}
