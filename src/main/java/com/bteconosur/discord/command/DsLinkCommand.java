package com.bteconosur.discord.command;

import java.util.Arrays;

import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.discord.util.CommandMode;
import com.bteconosur.discord.util.LinkService;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class DsLinkCommand extends DsCommand {

    private static final YamlConfiguration lang = ConfigHandler.getInstance().getLang();

    public DsLinkCommand() {
        super("link", "Linkear la cuenta de Discord.", 
            Arrays.asList(
                new OptionData(OptionType.STRING, "código", "Código de vinculación.", false)
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
        if (player != null) {
            event.reply(lang.getString("discord-already-linked")).setEphemeral(true).queue();
            return;
        }

        OptionMapping codigoOption = event.getOption("código");
        if (codigoOption == null || codigoOption.getAsString().isEmpty()) {
            String codigoGenerado;
            if (LinkService.hasDiscordCode(userId)) codigoGenerado = LinkService.getDiscordCode(userId);
            else codigoGenerado = LinkService.generateDiscordCode(userId);
            String message = lang.getString("discord-code").replace("%code%", codigoGenerado);
            event.reply(message).setEphemeral(true).queue();
            return;
        }
        
        String codigo = codigoOption.getAsString();
        if (!LinkService.isMinecraftCodeValid(codigo)) {
            event.reply(lang.getString("invalid-link-code")).setEphemeral(true).queue();
            return;
        }
        
        player = LinkService.linkDiscord(codigo, userId);
        if (player == null) {
            event.reply(lang.getString("internal-error")).setEphemeral(true).queue();
            return;
        }
        
        String message = lang.getString("discord-link-success").replace("%player%", player.getNombre());
        event.reply(message).setEphemeral(true).queue();
    }

}
