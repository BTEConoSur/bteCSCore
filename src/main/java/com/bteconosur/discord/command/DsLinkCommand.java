package com.bteconosur.discord.command;

import java.util.Arrays;

import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.ConsoleLogger;
import com.bteconosur.core.util.DiscordLogger;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.discord.util.CommandMode;
import com.bteconosur.discord.util.LinkService;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class DsLinkCommand extends DsCommand {

    public DsLinkCommand() {
        super("link", LanguageHandler.getText("ds-help.discord.commands.link.description"), 
            Arrays.asList(
                new OptionData(OptionType.STRING, "código", "Código de vinculación.", false)
            ),
            null,
            CommandMode.GLOBAL
        );
    }

      @Override
    public void execute(SlashCommandInteractionEvent event) {
        Long userId = event.getUser().getIdLong();
        Player player = PlayerRegistry.getInstance().findByDiscordId(userId);
        Language language = player != null ? player.getLanguage() : Language.getDefault();
        if (player != null) {
            event.reply(LanguageHandler.getText(language, "link.ds-already-linked")).setEphemeral(true).queue(
                success -> {},
                error -> ConsoleLogger.error(LanguageHandler.getText("ds-error.reply"), error)
            );
            return;
        }

        OptionMapping codigoOption = event.getOption("código");
        if (codigoOption == null || codigoOption.getAsString().isEmpty()) {
            String codigoGenerado;
            if (LinkService.hasDiscordCode(userId)) codigoGenerado = LinkService.getDiscordCode(userId);
            else codigoGenerado = LinkService.generateDiscordCode(userId);
            String message = LanguageHandler.getText(language, "link.ds-code").replace("%code%", codigoGenerado);
            event.reply(message).setEphemeral(true).queue(
                success -> {},
                error -> ConsoleLogger.error(LanguageHandler.getText("ds-error.reply"), error)
            );
            return;
        }
        
        String codigo = codigoOption.getAsString();
        if (!LinkService.isMinecraftCodeValid(codigo)) {
            event.reply(LanguageHandler.getText(language, "link.invalid-code")).setEphemeral(true).queue(
                success -> {},
                error -> ConsoleLogger.error(LanguageHandler.getText("ds-error.reply"), error)
            );
            return;
        }
        
        player = LinkService.linkDiscord(codigo, userId);
        if (player == null) {
            event.reply(LanguageHandler.getText(language, "ds-internal-error")).setEphemeral(true).queue(
                success -> {},
                error -> ConsoleLogger.error(LanguageHandler.getText("ds-error.reply"), error)
            );
            return;
        }
        
        String message = LanguageHandler.replaceMC("link.ds-success", language, player);
        DiscordLogger.staffLog(LanguageHandler.replaceDS("link.link-log", Language.getDefault(), player));
        
        event.reply(message).setEphemeral(true).queue(
            success -> {},
            error -> ConsoleLogger.error(LanguageHandler.getText("ds-error.reply"), error)
        );
    }

}
