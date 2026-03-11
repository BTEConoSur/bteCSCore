package com.bteconosur.discord.action;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.bteconosur.core.chat.ChatUtil;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.ConsoleLogger;
import com.bteconosur.db.model.Interaction;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.InteractionRegistry;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.discord.DiscordManager;

import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

public class PlayerInfoAction implements ButtonAction {

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
        if (buttonId.equals("player-info-cancel")) {
            event.getMessage().delete().queue();
            event.reply(LanguageHandler.getText(language, "ds-embeds.player-info.cancel-message")).setEphemeral(true).queue();
            ir.unload(ctx.getId());
            return;
        }

        String targetUuid = (String) ctx.getPayloadValue("targetUuid");
        Player targetPlayer = PlayerRegistry.getInstance().get(UUID.fromString(targetUuid));
        if (targetPlayer == null) {
            event.reply(LanguageHandler.getText(language, "player-not-found").replace("%player%", targetUuid)).setEphemeral(true).queue();
            return;
        }

        switch (buttonId) {
            case "see-projects": {
                int page = 1;
                MessageEmbed embed = ChatUtil.getDsProyectoList(targetPlayer, language, page);
                List<Button> buttons = buildProjectListButtons(targetPlayer, language, page);
                List<Button> buttons2 = buildProjectListButtons2(language);
                ctx.clearPayload();
                ctx.addPayloadValue("targetUuid", targetUuid);
                ctx.addPayloadValue("page", page);
                ir.merge(ctx.getId());
                event.editMessageEmbeds(embed).setComponents(ActionRow.of(buttons), ActionRow.of(buttons2)).queue();
                break;
            }
            case "see-player": {
                Long targetDsId = targetPlayer.getDsIdUsuario();
                if (targetDsId == null) {
                    MessageEmbed embed = ChatUtil.getDsPlayerInfo(targetPlayer, null, language);
                    List<Button> buttons = buildPlayerInfoButtons(language);
                    ctx.clearPayload();
                    ctx.addPayloadValue("targetUuid", targetUuid);
                    ir.merge(ctx.getId());
                    event.editMessageEmbeds(embed).setComponents(ActionRow.of(buttons)).queue();
                } else {
                    DiscordManager.getInstance().getJda().retrieveUserById(targetDsId).queue(user -> {
                        MessageEmbed embed = ChatUtil.getDsPlayerInfo(targetPlayer, user, language);
                        List<Button> buttons = buildPlayerInfoButtons(language);
                        ctx.clearPayload();
                        ctx.addPayloadValue("targetUuid", targetUuid);
                        ir.merge(ctx.getId());
                        event.editMessageEmbeds(embed).setComponents(ActionRow.of(buttons)).queue();
                    }, error -> {
                        ConsoleLogger.error("Error al obtener el usuario de Discord: " + error.getMessage());
                        event.reply(LanguageHandler.getText(language, "ds-internal-error")).setEphemeral(true).queue();
                    });
                }
                break;
            }
            case "project-list-previous": {
                int page = ((Number) ctx.getPayloadValue("page")).intValue() - 1;
                MessageEmbed embed = ChatUtil.getDsProyectoList(targetPlayer, language, page);
                List<Button> buttons = buildProjectListButtons(targetPlayer, language, page);
                List<Button> buttons2 = buildProjectListButtons2(language);
                ctx.clearPayload();
                ctx.addPayloadValue("targetUuid", targetUuid);
                ctx.addPayloadValue("page", page);
                ir.merge(ctx.getId());
                event.editMessageEmbeds(embed).setComponents(ActionRow.of(buttons), ActionRow.of(buttons2)).queue();
                break;
            }
            case "project-list-next": {
                int page = ((Number) ctx.getPayloadValue("page")).intValue() + 1;
                MessageEmbed embed = ChatUtil.getDsProyectoList(targetPlayer, language, page);
                List<Button> buttons = buildProjectListButtons(targetPlayer, language, page);
                List<Button> buttons2 = buildProjectListButtons2(language);
                ctx.clearPayload();
                ctx.addPayloadValue("targetUuid", targetUuid);
                ctx.addPayloadValue("page", page);
                ir.merge(ctx.getId());
                event.editMessageEmbeds(embed).setComponents(ActionRow.of(buttons), ActionRow.of(buttons2)).queue();
                break;
            }
            default:
                event.reply(LanguageHandler.getText(language, "ds-invalid-action")).setEphemeral(true).queue();
                break;
        }
    }

    @SuppressWarnings("null")
    private List<Button> buildPlayerInfoButtons(Language language) {
        List<Button> buttons = new ArrayList<>();
        buttons.add(Button.primary("see-projects", LanguageHandler.getText(language, "ds-embeds.player-info.see-projects")));
        buttons.add(Button.danger("player-info-cancel", LanguageHandler.getText(language, "ds-embeds.player-info.cancel")));
        return buttons;
    }

    @SuppressWarnings("null")
    private List<Button> buildProjectListButtons(Player targetPlayer, Language language, int page) {
        List<Button> buttons = new ArrayList<>();
        Button previousButton = Button.success("project-list-previous", LanguageHandler.getText(language, "ds-embeds.project-list.previous-page"));
        Button nextButton = Button.success("project-list-next", LanguageHandler.getText(language, "ds-embeds.project-list.next-page"));
        if (!ChatUtil.hasDsProjectListPreviousPage(page)) previousButton = previousButton.asDisabled();
        if (!ChatUtil.hasDsProjectListNextPage(targetPlayer, page)) nextButton = nextButton.asDisabled();
        String pageButton = LanguageHandler.getText(language, "ds-embeds.project-list.page")
            .replace("%currentPage%", String.valueOf(page))
            .replace("%totalPages%", String.valueOf(ChatUtil.getDsProjectListTotalPages(targetPlayer)));
        buttons.add(previousButton);
        buttons.add(Button.secondary("project-list-page", pageButton).asDisabled());
        buttons.add(nextButton);
        return buttons;
    }

    @SuppressWarnings("null")
    private List<Button> buildProjectListButtons2(Language language) {
        List<Button> buttons = new ArrayList<>();
        buttons.add(Button.primary("see-player", LanguageHandler.getText(language, "ds-embeds.project-list.see-player")));
        buttons.add(Button.danger("player-info-cancel", LanguageHandler.getText(language, "ds-embeds.project-list.cancel")));
        return buttons;
    }

}
