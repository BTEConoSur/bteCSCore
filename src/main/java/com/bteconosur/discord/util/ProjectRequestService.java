package com.bteconosur.discord.util;

import java.io.File;
import java.time.Instant;

import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.chat.ChatUtil;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.util.ConsoleLogger;
import com.bteconosur.core.util.DiscordLogger;
import com.bteconosur.db.model.Interaction;
import com.bteconosur.db.model.Pais;
import com.bteconosur.db.model.Proyecto;
import com.bteconosur.db.registry.InteractionRegistry;
import com.bteconosur.db.util.InteractionKey;

import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.utils.FileUpload;

public class ProjectRequestService {

    public static final YamlConfiguration config = ConfigHandler.getInstance().getConfig();
    public static final YamlConfiguration lang = ConfigHandler.getInstance().getLang();

    @SuppressWarnings("null")
    public static boolean sendProjectRequest(Proyecto proyecto, File mapImage) {
        MessageEmbed embed = ChatUtil.getDsProjectCreated(proyecto);
        Pais pais = proyecto.getPais();
        TextChannel channel = MessageService.getTextChannelById(pais.getDsIdRequest());
        if (channel == null) {
            ConsoleLogger.error("Canal de Discord no encontrado para país: " + pais.getNombre());
            return false;
        }
        
        if (mapImage == null || !mapImage.exists()) {
            ConsoleLogger.error("Imagen del mapa no disponible, no se puede enviar proyecto a Discord");
            return false;
        }
                
        channel.sendMessageEmbeds(embed)
            .addFiles(FileUpload.fromData(mapImage, "map.png"))
            .addComponents(
                ActionRow.of(
                    Button.success("accept", "Aceptar"),
                    Button.danger("cancel", "Rechazar")
                )
            )
            .queue(message -> {
                String notification = lang.getString("ds-reviewer-notification-new-project").replace("%link%", message.getJumpUrl()).replace("%pais%", pais.getNombre());
                DiscordLogger.notifyReviewers(notification, pais);
                Interaction ctx = new Interaction(
                    proyecto.getId(),
                    InteractionKey.CREATE_PROJECT,
                    Instant.now(),
                    Instant.now().plusSeconds(config.getInt("interaction-expirations.create-project") * 60L)
                );
                ctx.setMessageId(message.getIdLong());
                InteractionRegistry.getInstance().load(ctx);

            }, error -> {
                ConsoleLogger.error("Error al enviar proyecto a Discord: " + error.getMessage());
                error.printStackTrace();
            });
        
        return true;
    }
}
