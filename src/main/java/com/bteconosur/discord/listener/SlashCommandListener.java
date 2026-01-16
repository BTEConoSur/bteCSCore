package com.bteconosur.discord.listener;

import javax.annotation.Nonnull;

import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.BTEConoSur;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.util.ConsoleLogger;
import com.bteconosur.discord.command.DsCommand;
import com.bteconosur.discord.command.DsCommandManager;
import com.bteconosur.discord.command.DsSubcommand;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class SlashCommandListener extends ListenerAdapter {

    private final ConsoleLogger logger = BTEConoSur.getConsoleLogger();
    private final YamlConfiguration lang = ConfigHandler.getInstance().getLang();

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
            logger.warn("Error de Discord: Subcomando no encontrado: /" + commandName + " " + subCommandName);
        }

        if (command != null) {
            command.execute(event);
            return;
        } else logger.warn("Error de Discord: Comando no encontrado: /" + commandName);
        
        event.reply(lang.getString("discord-internal-error")).setEphemeral(true).queue();
    }

}
