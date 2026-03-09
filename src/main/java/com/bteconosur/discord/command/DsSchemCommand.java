package com.bteconosur.discord.command;

import java.io.File;
import java.util.Arrays;

import com.bteconosur.core.BTEConoSur;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.discord.util.CommandMode;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.FileUpload;

public class DsSchemCommand extends DsCommand {

    public DsSchemCommand() {
        super("schematic", "Obtén un schematic de WorldEdit por su nombre.", 
            Arrays.asList(
                new OptionData(OptionType.STRING, "nombre", "Nombre de la Schematic",  true)
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
        
        String nombre = event.getOption("nombre").getAsString();
        if (nombre == null || nombre.isEmpty()) {
            String message = LanguageHandler.getText(language, "ds-schematic.name-needed");
            event.reply(message).setEphemeral(true).queue();
            return;
        }

        WorldEditPlugin we = BTEConoSur.getWorldEditPlugin();
        File schemFile = new File(we.getDataFolder(), config.getString("schematic-path").replace("%nombre%", nombre));
        if (!schemFile.exists()) {
            String message = LanguageHandler.getText(language, "ds-schematic.not-found").replace("%nombre%", nombre);
            event.reply(message).setEphemeral(true).queue();
            return;
        }
        String respuesta = LanguageHandler.getText(language, "ds-schematic.success");
        event.reply(respuesta).addFiles(FileUpload.fromData(schemFile)).setEphemeral(true).queue();
    }

}
