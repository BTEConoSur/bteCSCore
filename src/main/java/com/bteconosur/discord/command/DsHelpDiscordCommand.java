package com.bteconosur.discord.command;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;


public class DsHelpDiscordCommand extends DsSubcommand {

    private static List<DsCommand> commands = new ArrayList<>();

    public DsHelpDiscordCommand() {
        super("discord", LanguageHandler.getText("ds-help.discord.commands.help.discord.description"), 
            Arrays.asList()
        );
    }

    @SuppressWarnings("null")
    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Long userId = event.getUser().getIdLong();
        Player player = PlayerRegistry.getInstance().findByDiscordId(userId);
        Language language = player != null ? player.getLanguage() : Language.getDefault();
        
        int page = 1;
        MessageEmbed embed = ChatUtil.getDsHelpDiscord(language, page);

        List<Button> buttons = new ArrayList<>();
        Button previousButton = Button.success("ds-help-previous", LanguageHandler.getText(language, "ds-help.previous-page"));
        Button nextButton = Button.success("ds-help-next", LanguageHandler.getText(language, "ds-help.next-page"));
        String pageButton = LanguageHandler.getText(language, "ds-help.page").replace("%currentPage%", String.valueOf(page)).replace("%totalPages%", String.valueOf(ChatUtil.getDsHelpTotalPages()));
        if (!ChatUtil.hasDsHelpPreviousPage(page)) previousButton = previousButton.asDisabled();
        if (!ChatUtil.hasDsHelpNextPage(page)) nextButton = nextButton.asDisabled();
        
        buttons.add(previousButton);
        buttons.add(Button.secondary("help-page", pageButton).asDisabled());
        buttons.add(nextButton);
        
        buttons.add(Button.danger("help-cancel", LanguageHandler.getText(language, "ds-help.cancel"))); 
        Instant now = DateUtils.instantOffset();
        Instant expiration = now.plusSeconds(config.getInt("interaction-expirations.help-command") * 60L);
        event.replyEmbeds(embed)
            .addComponents(ActionRow.of(buttons))
            .setEphemeral(true)
            .queue(
                hook -> {
                    hook.retrieveOriginal().queue(message -> {
                        Interaction ctx = new Interaction(
                            player != null ? player.getUuid() : null,
                            InteractionKey.HELP_COMMAND,
                            now,
                            expiration
                        );
                        ctx.setMessageId(message.getIdLong());
                        ctx.setChannelId(event.getChannel().getIdLong());
                        ctx.addPayloadValue("page", 1);
                        InteractionRegistry.getInstance().load(ctx);
                    });
                },
                error -> {
                    ConsoleLogger.error(LanguageHandler.getText(language, "ds-error.ds-help"), error);
                }
            );
    }

    public static void addHelpCommand(DsCommand command) {
        commands.add(command);
    }

    public static List<DsCommand> getCommands() {
        return new ArrayList<>(commands);
    }

}
