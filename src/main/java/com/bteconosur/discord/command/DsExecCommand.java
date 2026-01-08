package com.bteconosur.discord.command;

import java.util.Arrays;

import org.bukkit.Bukkit;

import com.bteconosur.core.BTEConoSur;
import com.bteconosur.discord.util.CommandMode;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class DsExecCommand extends DsCommand {

    public DsExecCommand() {
        super("exec", "Ejecutar un comando de consola en el servidor.", 
            Arrays.asList(
                new OptionData(OptionType.STRING, "comando", "Comando completo a ejecutar.", true)
            ),
            Arrays.asList(Permission.ADMINISTRATOR),
            CommandMode.STAFFHUB_ONLY
        );
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        OptionMapping comando = event.getOption("comando");
        if (comando == null || comando.getAsString().isEmpty()) {
            event.reply("Por favor, proporciona un comando.").setEphemeral(true).queue();
            return;
        }
        
        String commandStr = comando.getAsString();
        
        Bukkit.getScheduler().runTask(BTEConoSur.getInstance(), () -> {
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), commandStr);
        });
        
        event.reply("Comando ejecutado: `" + commandStr + "`").setEphemeral(false).queue();
    }
}
