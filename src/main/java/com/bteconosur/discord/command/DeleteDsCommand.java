package com.bteconosur.discord.command;

import java.util.Arrays;
import java.util.List;

import com.bteconosur.db.model.Pais;
import com.bteconosur.db.registry.PaisRegistry;
import com.bteconosur.discord.DiscordManager;
import com.bteconosur.discord.util.CommandMode;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class DeleteDsCommand extends DsCommand {

    public DeleteDsCommand() {
        super("delete", "Borrar un comando del registro de Discord,", 
            Arrays.asList(
                new OptionData(OptionType.STRING, "comando", "Nombre del comando a borrar", true)
            ),
            Arrays.asList(Permission.ADMINISTRATOR),
            CommandMode.STAFFHUB_ONLY
        );
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        OptionMapping comando = event.getOption("comando");
        if (comando == null || comando.getAsString().isEmpty()) {
            event.reply("Por favor, proporciona un nombre de comando válido.").setEphemeral(true).queue();
            return;
        }
        String commandName = comando.getAsString();
        event.getJDA().retrieveCommands().queue(commands -> {
            for (Command command : commands) {
                if (command.getName().equalsIgnoreCase(commandName)) {
                    command.delete().queue();
                }
            }
        });

        JDA jda = DiscordManager.getInstance().getJda();
        PaisRegistry paisRegistry = PaisRegistry.getInstance();
        List<Pais> paises = paisRegistry.getList();
        for (Pais pais : paises) {
            Long guildId = pais.getDsIdGuild();
            if (guildId == null) continue;
            Guild guild = jda.getGuildById(guildId);
            if (guild == null) continue;

            guild.retrieveCommands().queue(commands -> {
                for (Command command : commands) {
                    if (command.getName().equalsIgnoreCase(commandName)) {
                        command.delete().queue();
                    }
                }
            });
        }

        Guild guild = jda.getGuildById(1425856269029474304L);
        if (guild == null) return;
        guild.retrieveCommands().queue(commands -> {
                for (Command command : commands) {
                    if (command.getName().equalsIgnoreCase(commandName)) {
                        command.delete().queue();
                    }
                }
        });
        event.reply("Se ha intentado borrar el comando '/" + commandName + "'.").setEphemeral(true).queue();
    }
}
