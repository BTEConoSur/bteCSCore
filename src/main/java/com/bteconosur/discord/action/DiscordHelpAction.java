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
        
        switch (buttonId) {
            case "mc-help-previous":
                page--;
                if (ChatUtil.hasMcHelpPreviousPage(page)) {
                    buttons.add(Button.success("mc-help-previous", LanguageHandler.getText(language, "ds-help.previous-page")));
                }
                if (ChatUtil.hasMcHelpNextPage(page)) {
                    buttons.add(Button.success("mc-help-next", LanguageHandler.getText(language, "ds-help.next-page")));
                }
                embed = ChatUtil.getDsHelpMinecraft(language, page);
                break;
                
            case "mc-help-next":
                page++;
                if (ChatUtil.hasMcHelpPreviousPage(page)) {
                    buttons.add(Button.success("mc-help-previous", LanguageHandler.getText(language, "ds-help.previous-page")));
                }
                if (ChatUtil.hasMcHelpNextPage(page)) {
                    buttons.add(Button.success("mc-help-next", LanguageHandler.getText(language, "ds-help.next-page")));
                }
                embed = ChatUtil.getDsHelpMinecraft(language, page);
                break;
                
            case "ds-help-previous":
                page--;
                if (ChatUtil.hasDsHelpPreviousPage(page)) {
                    buttons.add(Button.success("ds-help-previous", LanguageHandler.getText(language, "ds-help.previous-page")));
                }
                if (ChatUtil.hasDsHelpNextPage(page)) {
                    buttons.add(Button.success("ds-help-next", LanguageHandler.getText(language, "ds-help.next-page")));
                }
                embed = ChatUtil.getDsHelpDiscord(language, page);
                break;
                
            case "ds-help-next":
                page++;
                if (ChatUtil.hasDsHelpPreviousPage(page)) {
                    buttons.add(Button.success("ds-help-previous", LanguageHandler.getText(language, "ds-help.previous-page")));
                }
                if (ChatUtil.hasDsHelpNextPage(page)) {
                    buttons.add(Button.success("ds-help-next", LanguageHandler.getText(language, "ds-help.next-page")));
                }
                embed = ChatUtil.getDsHelpDiscord(language, page);
                break;
            default:
                event.reply(LanguageHandler.getText(language, "ds-invalid-action")).setEphemeral(true).queue();
                return;
        }
        buttons.add(Button.danger("help-cancel", LanguageHandler.getText(language, "ds-help.cancel")));
        
        ctx.clearPayload();
        ctx.addPayloadValue("page", page);
        ir.merge(ctx.getId());
        
        event.editMessageEmbeds(embed)
            .setComponents(ActionRow.of(buttons))
            .queue();
    }

}
