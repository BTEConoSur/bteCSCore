package com.bteconosur.discord.command;

import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.discord.util.CommandMode;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class DsHelpCommand extends DsCommand {

    public DsHelpCommand() {
        super("help", LanguageHandler.getText("ds-help.description"), 
            null,
            null,
            CommandMode.GLOBAL
        );
        
        addSubcommand(new DsHelpMinecraftCommand());
        addSubcommand(new DsHelpDiscordCommand());
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.reply("No deberías ver esto :)").setEphemeral(true).queue();
    }
    
}
