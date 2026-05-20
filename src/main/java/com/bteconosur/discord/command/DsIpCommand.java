package com.bteconosur.discord.command;

import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.discord.util.CommandMode;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class DsIpCommand extends DsCommand {

    public DsIpCommand() {
        super("ip", "Muestra la IP del servidor.",
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

        event.reply(LanguageHandler.getText(language, "ds-ip")).setEphemeral(true).queue();
    }

}
