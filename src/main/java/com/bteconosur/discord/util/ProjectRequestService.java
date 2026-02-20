package com.bteconosur.discord.util;

import java.io.File;
import java.time.Instant;
import java.util.Date;

import org.bukkit.configuration.file.YamlConfiguration;
import org.locationtech.jts.geom.Polygon;

import com.bteconosur.core.BTEConoSur;
import com.bteconosur.core.ProjectManager;
import com.bteconosur.core.chat.ChatUtil;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.ConsoleLogger;
import com.bteconosur.core.util.DiscordLogger;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.core.util.TagResolverUtils;
import com.bteconosur.db.model.Interaction;
import com.bteconosur.db.model.Pais;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.Proyecto;
import com.bteconosur.db.registry.InteractionRegistry;
import com.bteconosur.db.registry.ProyectoRegistry;
import com.bteconosur.db.util.Estado;
import com.bteconosur.db.util.InteractionKey;

import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.utils.FileUpload;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

public class ProjectRequestService {

    public static final YamlConfiguration config = ConfigHandler.getInstance().getConfig();

    @SuppressWarnings("null")
    public static boolean sendProjectRequest(Proyecto proyecto, File mapImage) {
        Instant now = Instant.now();
        Instant expiration = now.plusSeconds(config.getInt("interaction-expirations.create-project") * 60L);
        MessageEmbed embed = ChatUtil.getDsProjectCreated(proyecto, Date.from(expiration) );
        Pais pais = proyecto.getPais();
        TextChannel channel = MessageService.getTextChannelById(pais.getDsIdRequest());
        if (channel == null) {
            ConsoleLogger.error(LanguageHandler.replaceDS("ds-error.channel-pais-not-found", Language.getDefault(), pais));
            return false;
        }
        
        if (mapImage == null || !mapImage.exists()) {
            ConsoleLogger.error(LanguageHandler.getText("ds-error.sat-map-not-found"));
            return false;
        }
                
        channel.sendMessageEmbeds(embed)
            .addFiles(FileUpload.fromData(mapImage, "map.png"))
            .addComponents(
                ActionRow.of(
                    Button.success("accept", LanguageHandler.getText("ds-button-accept")),
                    Button.danger("cancel", LanguageHandler.getText("ds-button-reject"))
                )
            )
            .queue(message -> {
                String dsNotification = LanguageHandler.replaceDS("project.create.request.ds-for-reviewer", Language.getDefault(), pais).replace("%link%", message.getJumpUrl());
                TagResolver tagResolver = TagResolverUtils.getLinkText("link", message.getJumpUrl(), LanguageHandler.getText("placeholder.link-display.see-request"), Language.getDefault());
                String mcNotification = LanguageHandler.replaceDS("project.create.request.for-reviewer", Language.getDefault(), pais);
                DiscordLogger.notifyReviewers(mcNotification, dsNotification, pais, tagResolver);
                Interaction ctx = new Interaction(
                    proyecto.getLider().getUuid(),
                    proyecto.getId(),
                    InteractionKey.CREATE_PROJECT,
                    now,
                    expiration
                );
                ctx.setMessageId(message.getIdLong());
                InteractionRegistry.getInstance().load(ctx);

            }, error -> {
                ConsoleLogger.error(LanguageHandler.getText("ds-error.send-project") + error.getMessage());
                error.printStackTrace();
            });
        
        return true;
    }

    @SuppressWarnings("null")
    public static boolean sendProjectRedefineRequest(Proyecto proyecto, Polygon newPolygon, Long tipoProyectoId, Long divisionId, File mapImage, Player requester) {
        Instant now = Instant.now();
        Instant expiration = now.plusSeconds(config.getInt("interaction-expirations.redefine-project") * 60L);
        MessageEmbed embed = ChatUtil.getDsProjectRedefineRequested(proyecto, requester, newPolygon, Date.from(expiration));
        Pais pais = proyecto.getPais();
        TextChannel channel = MessageService.getTextChannelById(pais.getDsIdRequest());
        if (channel == null) {
            ConsoleLogger.error(LanguageHandler.replaceDS("ds-error.request-channel-pais-not-found", Language.getDefault(), pais));
            return false;
        }
        
        if (mapImage == null || !mapImage.exists()) {
            ConsoleLogger.error(LanguageHandler.getText("ds-error.sat-map-not-found"));
            return false;
        }
                
        channel.sendMessageEmbeds(embed)
            .addFiles(FileUpload.fromData(mapImage, "map.png"))
            .addComponents(
                ActionRow.of(
                    Button.success("accept", LanguageHandler.getText("ds-button-accept")),
                    Button.danger("cancel", LanguageHandler.getText("ds-button-reject"))
                )
            )
            .queue(message -> {
                String dsNotification = LanguageHandler.replaceDS("project.redefine.request.ds-for-reviewer", Language.getDefault(), pais).replace("%link%", message.getJumpUrl());
                TagResolver tagResolver = TagResolverUtils.getLinkText("link", message.getJumpUrl(), LanguageHandler.getText("placeholder.link-display.see-request"), Language.getDefault());
                String mcNotification = LanguageHandler.replaceDS("project.redefine.request.for-reviewer", Language.getDefault(), pais);
                DiscordLogger.notifyReviewers(mcNotification, dsNotification, pais, tagResolver);
                
                Interaction ctx = new Interaction(
                    proyecto.getId(),
                    InteractionKey.REDEFINE_PROJECT,
                    now,
                    expiration
                );        
                ctx.setPoligono(newPolygon);
                ctx.setMessageId(message.getIdLong());
                ctx.addPayloadValue("tipoId", tipoProyectoId);
                ctx.addPayloadValue("divisionId", divisionId);
                ctx.addPayloadValue("previousEstado", proyecto.getEstado().name());
                InteractionRegistry.getInstance().load(ctx);
                proyecto.setEstado(Estado.REDEFINIENDO);
                ProyectoRegistry.getInstance().merge(proyecto.getId());
            }, error -> {
                ConsoleLogger.error(LanguageHandler.getText("ds-error.send-project") + error.getMessage());
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
                    privateChannel.sendMessageEmbeds(ChatUtil.getDsMemberJoinRequest(proyecto, player, player.getLanguage()))
                        .addComponents(ActionRow.of(Button.success("accept", LanguageHandler.getText("ds-button-accept")), Button.danger("cancel", LanguageHandler.getText("ds-button-reject"))))
                        .queue(message -> {
                            Interaction ctx2 = ir.findJoinRequest(proyecto.getId(), player.getUuid());
                            ctx2.setMessageId(message.getIdLong());
                            ir.merge(ctx2.getId());
                        }, error -> {
                            ConsoleLogger.error(LanguageHandler.getText("ds-error.send-join-request") + error.getMessage());
                            error.printStackTrace();
                        });
                });
            });
        }
        String notification = LanguageHandler.replaceMC("project.join.request.for-lider", lider.getLanguage(), player, proyecto);
        PlayerLogger.info(lider, notification, (String) null);
    }
}
