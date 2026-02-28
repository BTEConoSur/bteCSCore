package com.bteconosur.discord.command;

import com.bteconosur.core.chat.ChatUtil;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.discord.util.CommandMode;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;

public class DsContextPlayerCommand extends DsContextUserCommand {

    public DsContextPlayerCommand() {
        super("Obtener Información del Jugador", null, CommandMode.GLOBAL);
    }

    @SuppressWarnings("null")
    @Override
    public void execute(UserContextInteractionEvent event) {
        Player player = PlayerRegistry.getInstance().findByDiscordId(event.getUser().getIdLong());
        Language language = player != null ? player.getLanguage() : Language.getDefault();
        if (player == null) {
            event.reply(LanguageHandler.getText(language, "link.ds-link-needed")).setEphemeral(true).queue();
            return;
        }
    

        User discordUser = event.getTarget();
        Player targetPlayer = null;
        targetPlayer = PlayerRegistry.getInstance().findByDiscordId(discordUser.getIdLong());
        if (targetPlayer == null) {
            event.replyEmbeds(ChatUtil.getDsPlayerInfo(discordUser, language)).setEphemeral(true).queue();
            return;
        }

        if (targetPlayer.getDsIdUsuario() == null) {
            event.replyEmbeds(ChatUtil.getDsPlayerInfo(targetPlayer, null, language)).setEphemeral(true).queue();
            return;
        }

        event.replyEmbeds(ChatUtil.getDsPlayerInfo(targetPlayer, discordUser, language)).setEphemeral(true).queue();
    }

}
