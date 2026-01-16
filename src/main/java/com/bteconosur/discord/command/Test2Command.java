package com.bteconosur.discord.command;

import java.util.Arrays;

import com.bteconosur.discord.util.CommandMode;

import net.dv8tion.jda.api.Permission;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class Test2Command extends DsCommand {
    public Test2Command() {
        super("test2", "Segundo comando de prueba con opciones", 
            Arrays.asList(
                new OptionData(OptionType.STRING, "texto", "Texto de prueba", true),
                new OptionData(OptionType.INTEGER, "numero", "Número opcional", false)
            ),
            Arrays.asList(Permission.ADMINISTRATOR),
            CommandMode.COUNTRY_AND_STAFFHUB
        );
    }

    @SuppressWarnings("null")
    @Override
    public void execute(SlashCommandInteractionEvent event) {
        String texto = event.getOption("texto").getAsString();
        Long numero = event.getOption("numero") != null ? event.getOption("numero").getAsLong() : null;
        String respuesta = "Comando /test2 ejecutado! Texto: '" + texto + "'";
        if (numero != null) respuesta += ", Número: " + numero;
        event.reply(respuesta).setEphemeral(true).queue();
    }
}
