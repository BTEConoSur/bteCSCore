package com.bteconosur.discord.command;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import com.bteconosur.core.chat.ChatUtil;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.ConsoleLogger;
import com.bteconosur.core.util.DateUtils;
import com.bteconosur.db.model.Interaction;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.InteractionRegistry;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.db.util.InteractionKey;
import com.bteconosur.discord.DiscordManager;
import com.bteconosur.discord.util.CommandMode;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class DsPlayerCommand extends DsCommand {

    public DsPlayerCommand() {
        super("player", LanguageHandler.getText("ds-help.discord.commands.player.description"), 
            Arrays.asList(
                new OptionData(OptionType.USER, "usuario", "Usuario de Discord del jugador.", false),
                new OptionData(OptionType.STRING, "uuid", "UUID del jugador.", false),
                new OptionData(OptionType.STRING, "nombre", "Nombre del jugador.", false)
            ),
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
        if (player == null) {
            event.reply(LanguageHandler.getText(language, "link.ds-link-needed")).setEphemeral(true).queue();
            return;
        }
        
        if (event.getOptions().size() > 1) {
            String message = LanguageHandler.getText(language, "ds-player-info.invalid-options-size");
            event.reply(message).setEphemeral(true).queue();
            return;
        }

        if (event.getOptions().isEmpty()) {
            String message = LanguageHandler.getText(language, "ds-player-info.invalid-option");
            event.reply(message).setEphemeral(true).queue();
            return;
        }

        JDA jda = DiscordManager.getInstance().getJda();
        OptionMapping codigoOption = event.getOption("uuid");
        OptionMapping nombreOption = event.getOption("nombre");
        OptionMapping discordUserOption = event.getOption("usuario");
        Player targetPlayer = null;
        if (codigoOption != null && !codigoOption.getAsString().isEmpty()) {
            targetPlayer = PlayerRegistry.getInstance().get(UUID.fromString(codigoOption.getAsString()));
            if (targetPlayer == null) {
                String message = LanguageHandler.getText(language, "player-not-found").replace("%player%", codigoOption.getAsString());
                event.reply(message).setEphemeral(true).queue();
                return;
            }
        } else if (nombreOption != null && !nombreOption.getAsString().isEmpty()) { 
            targetPlayer = PlayerRegistry.getInstance().findByName(nombreOption.getAsString());
            if (targetPlayer == null) {
                String message = LanguageHandler.getText(language, "player-not-found").replace("%player%", nombreOption.getAsString());
                event.reply(message).setEphemeral(true).queue();
                return;
            }
        } else {
            User discordUser = discordUserOption.getAsUser();
            targetPlayer = PlayerRegistry.getInstance().findByDiscordId(discordUser.getIdLong());
            if (targetPlayer == null) {
                event.replyEmbeds(ChatUtil.getDsPlayerInfo(discordUser, language)).setEphemeral(true).queue();
                return;
            }
        }
        if (targetPlayer.getDsIdUsuario() == null) {
            replyWithInteraction(event, player, language, targetPlayer, null);
            return;
        }
        final Player finalTargetPlayer = targetPlayer;
        jda.retrieveUserById(targetPlayer.getDsIdUsuario()).queue(user -> {
            replyWithInteraction(event, player, language, finalTargetPlayer, user);
        }, error -> {
            ConsoleLogger.error("Error al obtener el usuario de Discord para el jugador " + finalTargetPlayer.getNombrePublico() + ": " + error.getMessage());
            event.reply(LanguageHandler.getText(language, "ds-internal-error")).setEphemeral(true).queue();
        });

    }

    @SuppressWarnings("null")
    private void replyWithInteraction(SlashCommandInteractionEvent event, Player player, Language language, Player targetPlayer, User discordUser) {
        List<Button> buttons = new ArrayList<>();
        buttons.add(Button.primary("see-projects", LanguageHandler.getText(language, "ds-embeds.player-info.see-projects")));
        buttons.add(Button.danger("player-info-cancel", LanguageHandler.getText(language, "ds-embeds.player-info.cancel")));

        Instant now = DateUtils.instantOffset();
        Instant expiration = now.plusSeconds(config.getInt("interaction-expirations.player-info") * 60L);

        event.replyEmbeds(ChatUtil.getDsPlayerInfo(targetPlayer, discordUser, language))
            .addComponents(ActionRow.of(buttons))
            .setEphemeral(true)
            .queue(
                hook -> {
                    hook.retrieveOriginal().queue(message -> {
                        Interaction ctx = new Interaction(
                            player != null ? player.getUuid() : null,
                            InteractionKey.PLAYER_INFO,
                            now,
                            expiration
                        );
                        ctx.setMessageId(message.getIdLong());
                        ctx.setChannelId(event.getChannel().getIdLong());
                        ctx.addPayloadValue("targetUuid", targetPlayer.getUuid().toString());
                        InteractionRegistry.getInstance().load(ctx);
                    });
                },
                error -> {
                    ConsoleLogger.error("Error al enviar la información del jugador: " + error.getMessage());
                    event.reply(LanguageHandler.getText(language, "ds-internal-error")).setEphemeral(true).queue();
                }
            );
    }

}
