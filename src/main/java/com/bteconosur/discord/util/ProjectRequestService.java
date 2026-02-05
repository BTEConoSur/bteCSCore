package com.bteconosur.discord.util;

import java.io.File;
import java.time.Instant;

import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.BTEConoSur;
import com.bteconosur.core.ProjectManager;
import com.bteconosur.core.chat.ChatUtil;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.util.ConsoleLogger;
import com.bteconosur.core.util.DiscordLogger;
import com.bteconosur.core.util.PlaceholderUtil;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.model.Interaction;
import com.bteconosur.db.model.Pais;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.Proyecto;
import com.bteconosur.db.registry.InteractionRegistry;
import com.bteconosur.db.util.InteractionKey;

import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.utils.FileUpload;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

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
                String dsNotification = lang.getString("ds-reviewer-notification-new-project").replace("%link%", message.getJumpUrl()).replace("%pais%", pais.getNombrePublico());
                TagResolver tagResolver = PlaceholderUtil.getLinkText("link", message.getJumpUrl(), "Ver solicitud");
                String mcNotification = lang.getString("reviewer-notification-new-project").replace("%pais%", pais.getNombrePublico());
                DiscordLogger.notifyReviewers(mcNotification, dsNotification, pais, tagResolver);
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

    @SuppressWarnings("null")
    public static void sendProjectJoinRequest(Proyecto proyecto, Player player) {
        Player lider = ProjectManager.getInstance().getLider(proyecto);
        InteractionRegistry ir = InteractionRegistry.getInstance();
        Interaction ctx = new Interaction(
            player.getUuid(),
            proyecto.getId(),
            InteractionKey.JOIN_PROJECT,
            Instant.now(),
            Instant.now().plusSeconds(config.getInt("interaction-expirations.join-project") * 60L)
        );
        ir.load(ctx);
        if (LinkService.isPlayerLinked(lider)) {
            BTEConoSur.getDiscordManager().getJda().retrieveUserById(lider.getDsIdUsuario()).queue(user -> {
                user.openPrivateChannel().queue(privateChannel -> {
                    privateChannel.sendMessageEmbeds(ChatUtil.getDsMemberJoinRequest(proyecto.getId(), proyecto.getNombre(), player.getNombre()))
                        .addComponents(ActionRow.of(Button.success("accept", "Aceptar"), Button.danger("cancel", "Rechazar")))
                        .queue(message -> {
                            Interaction ctx2 = ir.findJoinRequest(proyecto.getId(), player.getUuid());
                            ctx2.setMessageId(message.getIdLong());
                            ir.merge(ctx2.getId());
                        }, error -> {
                            ConsoleLogger.error("Error al enviar solicitud de unirse al proyecto por DM en Discord: " + error.getMessage());
                            error.printStackTrace();
                        });
                });
            });
        }
        String notification = lang.getString("leader-notificaction-member-join").replace("%player%", player.getNombre()).replace("%proyectoId%", proyecto.getId());
        PlayerLogger.info(lider, notification, (String) null);
    }
}
