package com.bteconosur.discord.action;

import java.util.ArrayList;
import java.util.List;

import com.bteconosur.core.chat.ChatUtil;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.db.model.Interaction;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.InteractionRegistry;
import com.bteconosur.db.registry.PlayerRegistry;

import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

public class DiscordHelpAction implements ButtonAction {

    @SuppressWarnings("null")
    @Override
    public void handle(ButtonInteractionEvent event, Interaction ctx) {
        String buttonId = event.getComponentId();
        Player player = PlayerRegistry.getInstance().findByDiscordId(event.getUser().getIdLong());
        Language language = player != null ? player.getLanguage() : Language.getDefault();
        InteractionRegistry ir = InteractionRegistry.getInstance();
        if (ctx.isExpired()) {
            event.getMessage().delete().queue();
            event.reply(LanguageHandler.getText(language, "ds-interaction-expired")).setEphemeral(true).queue();
            ir.unload(ctx.getId());
            return;
        }
        if (buttonId.equals("help-cancel")) {
            event.getMessage().delete().queue();
            event.reply(LanguageHandler.getText(language, "ds-help.cancel-message")).setEphemeral(true).queue();
            ir.unload(ctx.getId());
            return;
        }
        int page = ((Number) ctx.getPayloadValue("page")).intValue();
        MessageEmbed embed;
        List<Button> buttons = new ArrayList<>();
        
        Button previousButton = Button.success("mc-help-previous", LanguageHandler.getText(language, "ds-help.previous-page"));
        Button nextButton = Button.success("mc-help-next", LanguageHandler.getText(language, "ds-help.next-page"));
        String pageButton = LanguageHandler.getText(language, "ds-help.page");
        switch (buttonId) {
            case "mc-help-previous":
                page--;
                if (!ChatUtil.hasMcHelpPreviousPage(page)) previousButton = previousButton.asDisabled().withCustomId("mc-help-previous");
                 if (!ChatUtil.hasMcHelpNextPage(page)) nextButton = nextButton.asDisabled().withCustomId("mc-help-next");
                if (!ChatUtil.hasMcHelpNextPage(page)) nextButton = nextButton.asDisabled();
                embed = ChatUtil.getDsHelpMinecraft(language, page);
                pageButton = pageButton.replace("%totalPages%", String.valueOf(ChatUtil.getMcHelpTotalPages()));
                break;
            case "mc-help-next":
                page++;
                if (!ChatUtil.hasMcHelpPreviousPage(page)) previousButton = previousButton.asDisabled().withCustomId("mc-help-previous");
                if (!ChatUtil.hasMcHelpNextPage(page)) nextButton = nextButton.asDisabled().withCustomId("mc-help-next");
                embed = ChatUtil.getDsHelpMinecraft(language, page);
                pageButton = pageButton.replace("%totalPages%", String.valueOf(ChatUtil.getMcHelpTotalPages()));
                break;
            case "ds-help-previous":
                page--;
                if (!ChatUtil.hasDsHelpPreviousPage(page)) previousButton = previousButton.asDisabled().withCustomId("ds-help-previous");
                if (!ChatUtil.hasDsHelpNextPage(page)) nextButton = nextButton.asDisabled().withCustomId("ds-help-next");
                embed = ChatUtil.getDsHelpDiscord(language, page);
                pageButton = pageButton.replace("%totalPages%", String.valueOf(ChatUtil.getDsHelpTotalPages()));
                break;
            case "ds-help-next":
                page++;
                if (!ChatUtil.hasDsHelpPreviousPage(page)) previousButton = previousButton.asDisabled().withCustomId("ds-help-previous");
                if (!ChatUtil.hasDsHelpNextPage(page)) nextButton = nextButton.asDisabled().withCustomId("ds-help-next");
                embed = ChatUtil.getDsHelpDiscord(language, page);
                pageButton = pageButton.replace("%totalPages%", String.valueOf(ChatUtil.getDsHelpTotalPages()));
                break;
            default:
                event.reply(LanguageHandler.getText(language, "ds-invalid-action")).setEphemeral(true).queue();
                return;
        }
        pageButton = pageButton.replace("%currentPage%", String.valueOf(page));
        buttons.add(previousButton);
        buttons.add(Button.secondary("help-page", pageButton).asDisabled());
        buttons.add(nextButton);
        
        buttons.add(Button.danger("help-cancel", LanguageHandler.getText(language, "ds-help.cancel")));
        
        ctx.clearPayload();
        ctx.addPayloadValue("page", page);
        ir.merge(ctx.getId());
        
        event.editMessageEmbeds(embed)
            .setComponents(ActionRow.of(buttons))
            .queue();
    }

}
