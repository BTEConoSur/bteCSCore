package com.bteconosur.discord.command;

import java.io.File;
import java.util.Arrays;

import com.bteconosur.core.BTEConoSur;
import com.bteconosur.core.chat.ChatUtil;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.ConsoleLogger;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.Proyecto;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.db.registry.ProyectoRegistry;
import com.bteconosur.discord.util.CommandMode;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.FileUpload;

public class DsProyectoCommand extends DsCommand {

    public DsProyectoCommand() {
        super("proyecto", LanguageHandler.getText("ds-help.discord.commands.proyecto.description"), 
            Arrays.asList(
                new OptionData(OptionType.STRING, "id", "Id del proyecto.", true)
            ),
            null,
            CommandMode.GLOBAL
        );
    }

    @SuppressWarnings("null")
    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Player player = PlayerRegistry.getInstance().findByDiscordId(event.getUser().getIdLong());
        Language language = player != null ? player.getLanguage() : Language.getDefault();
        if (player == null) {
            event.reply(LanguageHandler.getText(language, "link.ds-link-needed")).setEphemeral(true).queue();
            return;
        }
        String id = event.getOption("id").getAsString();
        if (id == null || id.isEmpty()) {
            String message = LanguageHandler.getText(language, "ds-proyecto-info.id-needed");
            event.reply(message).setEphemeral(true).queue();
            return;
        }
        Proyecto proyecto = ProyectoRegistry.getInstance().get(id);

        if (proyecto == null) {
            event.reply(LanguageHandler.getText(language, "ds-proyecto-info.invalid-id").replace("%id%", id)).setEphemeral(true).queue();
            return;
        }

        MessageEmbed embed = ChatUtil.getDsProyectoInfo(proyecto, language);
        
        File folder = new File(BTEConoSur.getInstance().getDataFolder(), "images/projects");
        File imageFile = new File(folder, proyecto.getId() + ".png");
        
        if (imageFile.exists()) {
            event.replyEmbeds(embed).addFiles(FileUpload.fromData(imageFile, "map.png")).setEphemeral(true).queue();
        } else {
            event.replyEmbeds(embed).setEphemeral(true).queue();
            ConsoleLogger.error("Imágen del proyecto " + proyecto.getId() + " no encontrada en " + imageFile.getAbsolutePath());
        }
    }

}
