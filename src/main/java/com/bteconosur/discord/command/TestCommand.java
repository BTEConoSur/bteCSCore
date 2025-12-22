package com.bteconosur.discord.command;

import java.util.Collections;

import com.bteconosur.discord.util.CommandMode;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class TestCommand extends DsCommand {
    public TestCommand() {
        super("test", "Comando de prueba con subcomandos", Collections.emptyList(), Collections.emptyList(), CommandMode.GLOBAL);
        addSubcommand(new TestSubcommand1());
        addSubcommand(new TestSubcommand2());
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.reply("Usa un subcomando.").setEphemeral(true).queue();
    }

    public static class TestSubcommand1 extends DsSubcommand {
        public TestSubcommand1() {
            super("sub1", "Primer subcomando de test", Collections.emptyList());
        }

        @Override
        public void execute(SlashCommandInteractionEvent event) {
            event.reply("Ejecutaste el subcomando 1 de /test").setEphemeral(true).queue();
        }
    }

    public static class TestSubcommand2 extends DsSubcommand {
        public TestSubcommand2() {
            super("sub2", "Segundo subcomando de test", Collections.emptyList());
        }

        @Override
        public void execute(SlashCommandInteractionEvent event) {
            event.reply("Ejecutaste el subcomando 2 de /test").setEphemeral(true).queue();
        }
    }
}
