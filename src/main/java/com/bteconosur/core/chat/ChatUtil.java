package com.bteconosur.core.chat;

import java.util.Date;
import java.util.List;

import org.bukkit.configuration.file.YamlConfiguration;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import com.bteconosur.core.ProjectManager;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.ConsoleLogger;
import com.bteconosur.core.util.DateUtils;
import com.bteconosur.core.util.TerraUtils;
import com.bteconosur.db.model.Pais;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.Proyecto;
import com.bteconosur.db.model.RangoUsuario;
import com.bteconosur.db.model.TipoUsuario;
import com.bteconosur.db.registry.ProyectoRegistry;
import com.bteconosur.db.registry.TipoUsuarioRegistry;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class ChatUtil {

    private final static YamlConfiguration embedColors = ConfigHandler.getInstance().getEmbedColors();
    private final static YamlConfiguration config = ConfigHandler.getInstance().getConfig();

    public static String getMcFormatedMessage(Player player, String message, Language language) {
        String formatedMessage = LanguageHandler.replaceMC("mc-message", language, player);
        return formatedMessage.replace("%mensaje%", message);
    }

    public static String getMcFormatedMessage(String username, String message, Language language, Pais dsPais) {
        String formatedMessage = LanguageHandler.getText(language, "from-ds-message-not-player");
        String dsPrefix = LanguageHandler.replaceMC("placeholder.chat-mc.ds", language, dsPais);
        formatedMessage = formatedMessage.replace("%dsPrefix%", dsPrefix);
        return formatedMessage.replace("%mensaje%", message).replace("%username%", username);
    }

    public static String getMcFormatedMessage(Player player, String message, Language language, Pais dsPais) {
        String formatedMessage = LanguageHandler.replaceMC("from-ds-message",language, player);
        String dsPrefix = LanguageHandler.replaceMC("placeholder.chat-mc.ds", language, dsPais);
        return formatedMessage.replace("%mensaje%", message).replace("%dsPrefix%", dsPrefix);
    }

    public static String getDsFormatedMessage(Player player, String message, Language language) {
        String formatedMessage = LanguageHandler.replaceDS("from-mc-message", language, player);
        String mcPrefix = LanguageHandler.getText(language, "placeholder.chat-ds.mc");
        return formatedMessage.replace("%mcPrefix%", mcPrefix).replace("%mensaje%", message);
    }

    public static String getDsFormatedMessage(Player player, String message, Language language, Pais dsPais) {
        String formatedMessage = LanguageHandler.replaceDS("ds-message", language, player);
        String dsPrefix = LanguageHandler.replaceDS("placeholder.chat-ds.ds", language, dsPais);
        return formatedMessage.replace("%dsPais%", dsPrefix).replace("%mensaje%", message);
    }

    public static String getDsFormatedMessage(String username, String message, Language language, Pais dsPais) {
        String formatedMessage = LanguageHandler.getText(language, "ds-message-not-player");
        String dsPrefix = LanguageHandler.replaceDS("placeholder.chat-ds.ds", language, dsPais);
        formatedMessage = formatedMessage.replace("%mensaje%", message);
        return formatedMessage.replace("%dsPais%", dsPrefix).replace("%username%", username);
    }

    private static MessageEmbed builEmbed(String key, Language language, String title, String description, String author, String iconUrl) {
        EmbedBuilder eb = new EmbedBuilder();
        if (title == null)  title = LanguageHandler.getTextWithouthWarn(language, key + ".title");
        if (description == null) description = LanguageHandler.getTextWithouthWarn(language, key + ".description");
        if (author == null) author = LanguageHandler.getTextWithouthWarn(language, key + ".author");
        if (!title.equals("ERROR_KEY_NF")) eb.setTitle(title);
        if (!description.equals("ERROR_KEY_NF")) eb.setDescription(description);
        if (!author.equals("ERROR_KEY_NF")) eb.setAuthor(author, null, iconUrl);
        
        int color = embedColors.getInt(key);
        if (color == 0) {
            ConsoleLogger.warn("Color no encontrado para la clave " + key + ", usando color por defecto.");
            color = embedColors.getInt("default");
        }
        return eb.setColor(color).build();
    }

    private static MessageEmbed buildChatNotification(String key) {
        Language language = Language.getDefault();
        return builEmbed("ds-chat-notifications." + key, language, LanguageHandler.getText(language, "ds-chat-notifications." + key + ".title"), LanguageHandler.getTextWithouthWarn(language, "ds-chat-notifications." + key + ".description"), null, null);
    }

    private static MessageEmbed buildChatNotification(String key, Player player) {
        Language language = Language.getDefault();
        return builEmbed("ds-chat-notifications." + key, language, null, null, LanguageHandler.replaceDS("ds-chat-notifications." + key + ".author", language, player),
            config.getString("avatar-url").replace("%uuid%", player.getUuid().toString()));
    }

    public static MessageEmbed getServerStarted() {
        return buildChatNotification("start");
    }

    public static MessageEmbed getServerStopped() {
        return buildChatNotification("stop");
    }

    public static MessageEmbed getDsPlayerJoined(Player player) {
        return buildChatNotification("player-join", player);
    }

    public static MessageEmbed getDsPlayerLeft(Player player) {
        return buildChatNotification("player-left", player);
    }

    public static MessageEmbed getDsChatJoined(Player player) {
        return buildChatNotification("player-chat-join", player);
    }

    public static MessageEmbed getDsChatLeft(Player player) {
        return buildChatNotification("player-chat-left", player);
    }

    public static String getMcPlayerJoined(Player player, Language language) {
        return LanguageHandler.replaceMC("player-join-message", language, player);
    }

    public static String getMcPlayerLeft(Player player, Language language) {
        return LanguageHandler.replaceMC("player-leave-message", language, player);
    }

    public static String getMcChatJoined(Player player, Language language) {
        return LanguageHandler.replaceMC("player-join-chat-message", language, player);
    }

    public static String getMcChatLeft(Player player, Language language) {
        return LanguageHandler.replaceMC("player-leave-chat-message", language, player);
    }

    public static MessageEmbed buildDMNotification(String key, Player player, String description) {
        Language language = player.getLanguage();
        key = "ds-notifications." + key;
        return builEmbed(key, language, LanguageHandler.replaceDS(key, language, player), description, null, null);
    }

    public static MessageEmbed buildDMNotification(String key, Player player, Language language, String description) {
        key = "ds-notifications." + key;
        return builEmbed(key, language, LanguageHandler.replaceDS(key + ".title", language, player), description, null, null);
    }

    public static MessageEmbed buildDMNotification(String key, TipoUsuario tipoUsuario, Language language, String description) {
        key = "ds-notifications." + key;
        return builEmbed(key, language, LanguageHandler.replaceDS(key + ".title", language, tipoUsuario), description, null, null);
    }

    public static MessageEmbed buildDMNotification(String key, RangoUsuario rangoUsuario, Language language, String description) {
        key = "ds-notifications." + key;
        return builEmbed(key, language, LanguageHandler.replaceDS(key + ".title", language, rangoUsuario), description, null, null);
    }

    public static MessageEmbed buildDMNotification(String key, Pais pais, Language language, String description) {
        key = "ds-notifications." + key;
        return builEmbed(key, language, LanguageHandler.replaceDS(key, language, pais), description, null, null);
    }

    public static MessageEmbed buildDMNotification(String key, Language language, String description) {
        key = "ds-notifications." + key;
        return builEmbed(key, language, LanguageHandler.getText(language, key), description, null, null);
    }
    

    public static MessageEmbed getDsRangoUsuarioSwitched(RangoUsuario rangoUsuario, Language language) {
        return buildDMNotification("rango-switched", rangoUsuario, language, LanguageHandler.replaceDS("ds-notifications.rango-switched.description", language, rangoUsuario));
    }

    public static MessageEmbed getDsTipoUsuarioSwitched(TipoUsuario tipoUsuario, Language language) {
        String description = LanguageHandler.replaceDS("ds-notifications.tipo-switched.description", language, tipoUsuario)
            + "\n" + LanguageHandler.replaceDS("ds-notifications.tipo-switched.max-projects", language, tipoUsuario);
        return buildDMNotification("tipo-switched", tipoUsuario, language, description);
    }

    public static MessageEmbed getDsManagerAdded(Pais pais, Language language) {
        return buildDMNotification("manager-target-added", pais, language, null);
    }

    public static MessageEmbed getDsManagerRemoved(Pais pais, Language language) {
        return buildDMNotification("manager-target-removed", pais, language, null);
    }

    public static MessageEmbed getDsReviewerAdded(Pais pais, Language language) {
        return buildDMNotification("reviewer-target-added", pais, language, null);
    }

    public static MessageEmbed getDsReviewerRemoved(Pais pais, Language language) {
        return buildDMNotification("reviewer-target-removed", pais, language, null);
    }

    public static MessageEmbed getDsLinkSuccess(Player player) {
        return buildDMNotification("link-success", player, null);
    }

    private static MessageEmbed buildDMNotification(String key, Proyecto proyecto, Language language, String extraDescription) {
        key = "ds-notifications.project." + key;
        String description = LanguageHandler.replaceDS("ds-notifications.project.id", language, proyecto);
        if (proyecto.getNombre() != null && !proyecto.getNombre().isBlank()) description = description 
            + "\n" + LanguageHandler.replaceDS("ds-notifications.project.nombre", language, proyecto);
        if (extraDescription != null && !extraDescription.isBlank()) description = description + "\n" + extraDescription;
        return builEmbed(key, language, LanguageHandler.getText(language, key), description, null, null);
    }

    private static MessageEmbed buildDMNotification(String key, Proyecto proyecto, Player player, Language language, String extraDescription) {
        key = "ds-notifications.project." + key;
        String description = LanguageHandler.replaceDS("ds-notifications.project.id", language, proyecto);
        if (proyecto.getNombre() != null && !proyecto.getNombre().isBlank()) description = description 
            + "\n" + LanguageHandler.replaceDS("ds-notifications.project.nombre", language, proyecto);
        if (extraDescription != null && !extraDescription.isBlank()) description = description + "\n" + extraDescription;
        return builEmbed(key, language, LanguageHandler.replaceDS(key, language, player), description, null, null);
    }

    public static MessageEmbed getDsProjectAccepted(Proyecto proyecto, String comentario, Language language) {
        String description = null;
        if (comentario != null && !comentario.isBlank()) description = LanguageHandler.getText(language, "ds-notifications.project.comment").replace("%comentario%", comentario);
        return buildDMNotification("project-accepted", proyecto, language, description);
    }

    public static MessageEmbed getDsProjectRejected(Proyecto proyecto, String comentario, Language language) {
        String description = null;
        if (comentario != null && !comentario.isBlank()) description = LanguageHandler.getText(language, "ds-notifications.project.comment").replace("%comentario%", comentario);
        return buildDMNotification("project-rejected", proyecto, language, description);
    }

    public static MessageEmbed getDsProjectRequestExpired(Proyecto proyecto, Language language) {
        return buildDMNotification("project-request-expired", proyecto, language, null);
    }

    public static MessageEmbed getDsProjectFinishAccepted(Proyecto proyecto, String comentario, Language language) {
        String description = null;
        if (comentario != null && !comentario.isBlank()) description = LanguageHandler.getText(language, "ds-notifications.project.comment").replace("%comentario%", comentario);
        return buildDMNotification("project-finish-accepted", proyecto, language, description);
    }

    public static MessageEmbed getDsProjectFinishRejected(Proyecto proyecto, String comentario, Language language) {
        String description = null;
        if (comentario != null && !comentario.isBlank()) description = LanguageHandler.getText(language, "ds-notifications.project.comment").replace("%comentario%", comentario);
        return buildDMNotification("project-finish-rejected", proyecto, language, description);
    }

    public static MessageEmbed getDsProjectFinishRequestExpired(Proyecto proyecto, Language language) {
        return buildDMNotification("project-finish-expired", proyecto, language, null);
    }

    public static MessageEmbed getDsProjectFinishRequested(Proyecto proyecto, Player requester, Language language) {
        return buildDMNotification("project-finish-requested", proyecto, requester, language, null);
    }

    public static MessageEmbed getDsMemberAdded(Proyecto proyecto, Language language) {
        return buildDMNotification("project-member-added", proyecto, language, null);
    }

    public static MessageEmbed getDsMemberRemoved(Proyecto proyecto, Language language) {
        return buildDMNotification("project-member-removed", proyecto, language, null);
    }

    public static MessageEmbed getDsLeaderRemoved(Proyecto proyecto, Language language) {
        return buildDMNotification("project-leader-removed", proyecto, language, null);
    }

    public static MessageEmbed getDsMemberJoinRequest(Proyecto proyecto, Player requester, Language language) {
        return buildDMNotification("project-member-join-request", proyecto, requester, language, null);
    }

    public static MessageEmbed getDsMemberJoinRequestExpired(Proyecto proyecto, Language language) {
        return buildDMNotification("project-join-request-expired", proyecto, language, null);
    }

    public static MessageEmbed getDsMemberJoinRequestExpiredLider(Proyecto proyecto, Player requester, Language language) {
        return buildDMNotification("project-join-request-expired-lider", proyecto, requester, language, null);
    }
    
    public static MessageEmbed getDsMemberJoinRequestRejected(Proyecto proyecto, Language language) {
        return buildDMNotification("project-join-request-rejected", proyecto, language, null);
    }

    public static MessageEmbed getDsMemberJoinRequestAccepted(Proyecto proyecto, Language language) {
        return buildDMNotification("project-join-request-accepted", proyecto, language, null);
    }

    public static MessageEmbed getDsMemberAddedMember(Proyecto proyecto, Player added, Language language) {
        return buildDMNotification("project-member-added-member", proyecto, added, language, null);
    }

    public static MessageEmbed getDsMemberRemovedMember(Proyecto proyecto, Player removed, Language language) {
        return buildDMNotification("project-member-removed-member", proyecto, removed, language, null);
    }

    public static MessageEmbed getDsMemberLeftMember(Proyecto proyecto, Player removed, Language language) {
        return buildDMNotification("project-member-left-member", proyecto, removed, language, null);
    }

    public static MessageEmbed getDsLeaderSwitched(Proyecto proyecto, Language language) {
        return buildDMNotification("project-leader-switched", proyecto, language, null);
    }

    public static MessageEmbed getDsLeaderSwitchedLeader(Proyecto proyecto, Player newLeader, Language language) {
        return buildDMNotification("project-leader-switched-leader", proyecto, newLeader, language, null);
    }

    public static MessageEmbed getDsLeaderSwitchedMember(Proyecto proyecto, Player newLeader, Language language) {
        return buildDMNotification("project-leader-switched-member", proyecto, newLeader, language, null);
    }

    public static MessageEmbed getDsProjectRedefineRequestedMember(Proyecto proyecto, Player requester, Language language) {
        return buildDMNotification("project-redefine-request-member", proyecto, requester, language, null);
    }

    public static MessageEmbed getDsProjectRedefineExpired(Proyecto proyecto, Language language) {
        return buildDMNotification("project-redefine-expired", proyecto, language, null);
    }

    public static MessageEmbed getDsProjectRedefineAccepted(Proyecto proyecto, String comentario, Language language) {
        String description = null;
        if (comentario != null && !comentario.isBlank()) description = LanguageHandler.getText(language, "ds-notifications.project.comment").replace("%comentario%", comentario);
        return buildDMNotification("project-redefine-accepted", proyecto, language, description);
    }

    public static MessageEmbed getDsProjectRedefineRejected(Proyecto proyecto, String comentario, Language language) {
        String description = null;
        if (comentario != null && !comentario.isBlank()) description = LanguageHandler.getText(language, "ds-notifications.project.comment").replace("%comentario%", comentario);
        return buildDMNotification("project-redefine-rejected", proyecto, language, description);
    }

    public static MessageEmbed getDsProjectEditActiveMember(Proyecto proyecto, Player requester, Language language) {
        return buildDMNotification("project-edit-active-member", proyecto, requester, language, null);
    }

    public static MessageEmbed getDsProjectFinishEditRequested(Proyecto proyecto, Player requester, Language language) {
        return buildDMNotification("project-finish-edit-requested", proyecto, requester, language, null);
    }

    public static MessageEmbed getDsProjectFinishEditRequestExpired(Proyecto proyecto, Language language) {
        return buildDMNotification("project-finish-edit-expired", proyecto, language, null);
    }

    public static MessageEmbed getDsProjectFinishEditAccepted(Proyecto proyecto, String comentario, Language language) {
        String description = null;
        if (comentario != null && !comentario.isBlank()) description = LanguageHandler.getText(language, "ds-notifications.project.comment").replace("%comentario%", comentario);
        return buildDMNotification("project-finish-edit-accepted", proyecto, language, description);
    }

    public static MessageEmbed getDsProjectFinishEditRejected(Proyecto proyecto, String comentario, Language language) {
        String description = null;
        if (comentario != null && !comentario.isBlank()) description = LanguageHandler.getText(language, "ds-notifications.project.comment").replace("%comentario%", comentario);
        return buildDMNotification("project-finish-edit-rejected", proyecto, language, description);
    }

    public static MessageEmbed getDsProjectDeleted(Proyecto proyecto, Language language) {
        return buildDMNotification("project-deleted", proyecto, language, null);
    }

    public static MessageEmbed getDsProjectNameUpdated(Proyecto proyecto, String newNombre, Language language) {
        String key = "ds-notifications.project.project-name-updated";
        String description = LanguageHandler.replaceDS("ds-notifications.project.id", language, proyecto);
        description = description + "\n" + LanguageHandler.getText(language, "ds-notifications.project.nombre-nuevo").replace("%nombreNuevo%", newNombre);
        return builEmbed(key, language, LanguageHandler.getText(language, key), description, null, null);
    }

    public static MessageEmbed getDsProjectDescriptionUpdated(Proyecto proyecto, String newDescripcion, Language language) {
        String key = "ds-notifications.project.project-description-updated";
        String description = LanguageHandler.replaceDS("ds-notifications.project.id", language, proyecto);
        description = description + "\n" + LanguageHandler.getText(language, "ds-notifications.project.descripcion-nueva").replace("%descripcionNueva%", newDescripcion);
        return builEmbed(key, language, LanguageHandler.getText(language, key), description, null, null);
    }

    @SuppressWarnings("null")
    public static MessageEmbed getDsProjectCreated(Proyecto proyecto, Date expiredDate) {
        ProjectManager pm = ProjectManager.getInstance();
        Player player = pm.getLider(proyecto);
        ProyectoRegistry pr = ProyectoRegistry.getInstance();
        int[] counts = ProyectoRegistry.getInstance().getCounts(player);   
        String title = LanguageHandler.replaceDS("ds-embeds.project-created.title", Language.getDefault(), player);
        EmbedBuilder eb = new EmbedBuilder().setTitle(title);
        Polygon polygon = proyecto.getPoligono();
        Point centroid = polygon.getCentroid();
        double[] geoCoords = TerraUtils.toGeo(centroid.getX(), centroid.getY());
        String coords = geoCoords[1] + ", " + geoCoords[0];
        TipoUsuarioRegistry tur = TipoUsuarioRegistry.getInstance();
        if (tur.getVisita().equals(player.getTipoUsuario())) eb.appendDescription("\n" + LanguageHandler.getText("ds-embeds.project-created.player-is-visita"));
        if (tur.getPostulante().equals(player.getTipoUsuario())) eb.appendDescription("\n" + LanguageHandler.getText("ds-embeds.project-created.player-is-postulante"));
        if (pr.hasCollisions(proyecto.getId(), proyecto.getPoligono())) eb.appendDescription("\n" + LanguageHandler.getText("ds-embeds.project-created.has-collisions"));

        String footer = LanguageHandler.getText("ds-embeds.project-created.footer").replace("%fechaHoraVencimiento%", DateUtils.formatDateHour(expiredDate, Language.getDefault()));
        eb.addField(LanguageHandler.getText("ds-embeds.project-created.fields.rango"), player.getRangoUsuario().getNombre(), true)   
            .addField(LanguageHandler.getText("ds-embeds.project-created.fields.tipo"), player.getTipoUsuario().getNombre(), true)
            .addField(LanguageHandler.getText("ds-embeds.project-created.fields.fecha-ingreso"), DateUtils.getDsTimestamp(player.getFechaIngreso(), Language.getDefault()), true)
            .addField(LanguageHandler.getText("ds-embeds.project-created.fields.proyectos-completados"), String.valueOf(counts[0]), true)
            .addField(LanguageHandler.getText("ds-embeds.project-created.fields.proyectos-activos"), String.valueOf(counts[1]), true)
            .addField(LanguageHandler.getText("ds-embeds.project-created.fields.separator"), "", false)
            .addField(LanguageHandler.getText("ds-embeds.project-created.fields.tipo-proyecto"), proyecto.getTipoProyecto().getNombre(), true)
            .addField(LanguageHandler.getText("ds-embeds.project-created.fields.max-miembros"), String.valueOf(proyecto.getTipoProyecto().getMaxMiembros()), true)
            .addField(LanguageHandler.getText("ds-embeds.project-created.fields.tamaño"), String.valueOf(polygon.getArea()), true)
            .addField(LanguageHandler.getText("ds-embeds.project-created.fields.coordenadas"), coords, false)
            .setImage("attachment://map.png")
            .setColor(embedColors.getInt("ds-embeds.project-created"))
            .setFooter(footer);
        if (proyecto.getNombre() != null && !proyecto.getNombre().isBlank()) eb.addField(LanguageHandler.getText("ds-embeds.project-created.fields.nombre-proyecto"), proyecto.getNombre(), false);
        if (proyecto.getDescripcion() != null && !proyecto.getDescripcion().isBlank()) eb.addField(LanguageHandler.getText("ds-embeds.project-created.fields.descripcion"), proyecto.getDescripcion(), false);
        List<String> colors = LanguageHandler.getTextList(Language.getDefault(), "ds-embeds.project-created.polygons-colors");
        for (String color : colors) {
            eb.addField("", color, true);
        }
        return eb.build();
    }

    @SuppressWarnings("null")
    public static MessageEmbed getDsProjectRedefineRequested(Proyecto proyecto, Player commandPlayer, Polygon newPolygon, Date expiredDate) {
        ProyectoRegistry pr = ProyectoRegistry.getInstance();
        String title = LanguageHandler.replaceDS("ds-embeds.project-redefine-requested.title", Language.getDefault(), List.of(commandPlayer), List.of(proyecto));
        Polygon polygon = proyecto.getPoligono();
        Point centroid = polygon.getCentroid();
        double[] geoCoords = TerraUtils.toGeo(centroid.getX(), centroid.getY());
        String coords = geoCoords[1] + ", " + geoCoords[0];
        EmbedBuilder eb = new EmbedBuilder().setTitle(title);
        if (pr.hasCollisions(proyecto.getId(), newPolygon)) eb.appendDescription("\n" + LanguageHandler.getText("ds-embeds.project-redefine-requested.has-collisions"));

        String footer = LanguageHandler.getText("ds-embeds.project-redefine-requested.footer").replace("%fechaHoraVencimiento%", DateUtils.formatDateHour(expiredDate, Language.getDefault()));
        eb.addField(LanguageHandler.getText("ds-embeds.project-redefine-requested.fields.tipo-proyecto"), proyecto.getTipoProyecto().getNombre(), true)
            .addField(LanguageHandler.getText("ds-embeds.project-redefine-requested.fields.max-miembros"), String.valueOf(proyecto.getTipoProyecto().getMaxMiembros()), true)
            .addField(LanguageHandler.getText("ds-embeds.project-redefine-requested.fields.tamaño"), String.valueOf(polygon.getArea()), true)
            .addField(LanguageHandler.getText("ds-embeds.project-redefine-requested.fields.coordenadas"), coords, false)
            .setImage("attachment://map.png")
            .setColor(embedColors.getInt("ds-embeds.project-redefine-requested"))
            .setFooter(footer);
        if (proyecto.getNombre() != null && !proyecto.getNombre().isBlank()) eb.addField(LanguageHandler.getText("ds-embeds.project-redefine-requested.fields.nombre-proyecto"), proyecto.getNombre(), false);
        if (proyecto.getDescripcion() != null && !proyecto.getDescripcion().isBlank()) eb.addField(LanguageHandler.getText("ds-embeds.project-redefine-requested.fields.descripcion"), proyecto.getDescripcion(), false);
        List<String> colors = LanguageHandler.getTextList(Language.getDefault(), "ds-embeds.project-redefine-requested.polygons-colors");
        for (String color : colors) {
            eb.addField("", color, true);
        }
        return eb.build();
    }

}
