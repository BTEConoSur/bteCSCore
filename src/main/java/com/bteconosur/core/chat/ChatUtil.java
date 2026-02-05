package com.bteconosur.core.chat;

import java.util.UUID;

import org.bukkit.configuration.file.YamlConfiguration;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import com.bteconosur.core.config.ConfigHandler;
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

    private static YamlConfiguration lang = ConfigHandler.getInstance().getLang();

    public static String getMcFormatedMessage(Player player, String message) {
        String formatedMessage = lang.getString("mc-message");

        Pais pais = player.getPaisPrefix();
        if (pais != null) formatedMessage = formatedMessage.replace("%paisPrefix%", lang.getString("mc-prefixes.pais." + pais.getNombre().toLowerCase()));
        else formatedMessage = formatedMessage.replace("%paisPrefix%", lang.getString("mc-prefixes.pais.internacional"));

        RangoUsuario rango = player.getRangoUsuario();
        if (rango != null) formatedMessage = formatedMessage.replace("%rangoPrefix%", lang.getString("mc-prefixes.rango." + rango.getNombre().toLowerCase()));
        else formatedMessage = formatedMessage.replace("%rangoPrefix%", "");

        TipoUsuario tipo = player.getTipoUsuario();
        if (tipo != null) formatedMessage = formatedMessage.replace("%tipoPrefix%", lang.getString("mc-prefixes.tipo." + tipo.getNombre().toLowerCase()));
        else formatedMessage = formatedMessage.replace("%tipoPrefix%", "");

        formatedMessage = formatedMessage.replace("%player%", player.getNombrePublico());
        formatedMessage = formatedMessage.replace("%message%", message);

        return formatedMessage;
    }

    public static String getMcFormatedMessage(String username, String message, Pais dsPais) {
        String formatedMessage = lang.getString("from-ds-message");

        String dsPrefix = lang.getString("mc-prefixes.ds");
        dsPrefix = dsPrefix.replace("%pais%", dsPais.getNombrePublico());

        formatedMessage = formatedMessage.replace("%dsPrefix%", dsPrefix);
        formatedMessage = formatedMessage.replace("%player%", username);
        formatedMessage = formatedMessage.replace("%message%", message);

        formatedMessage = formatedMessage.replace("%paisPrefix%", "");
        formatedMessage = formatedMessage.replace("%rangoPrefix%", "");
        formatedMessage = formatedMessage.replace("%tipoPrefix%", "");

        return formatedMessage;
    }

    public static String getMcFormatedMessage(Player player, String message, Pais dsPais) {
        String formatedMessage = lang.getString("from-ds-message");

        String dsPrefix = lang.getString("mc-prefixes.ds");
        dsPrefix = dsPrefix.replace("%pais%", dsPais.getNombrePublico());

        formatedMessage = formatedMessage.replace("%dsPrefix%", dsPrefix);

        Pais pais = player.getPaisPrefix();
        if (pais != null) formatedMessage = formatedMessage.replace("%paisPrefix%", lang.getString("mc-prefixes.pais." + pais.getNombre()));
        else formatedMessage = formatedMessage.replace("%paisPrefix%", lang.getString("mc-prefixes.pais.internacional"));

        RangoUsuario rango = player.getRangoUsuario();
        if (rango != null) formatedMessage = formatedMessage.replace("%rangoPrefix%", lang.getString("mc-prefixes.rango." + rango.getNombre().toLowerCase()));
        else formatedMessage = formatedMessage.replace("%rangoPrefix%", "");

        TipoUsuario tipo = player.getTipoUsuario();
        if (tipo != null) formatedMessage = formatedMessage.replace("%tipoPrefix%", lang.getString("mc-prefixes.tipo." + tipo.getNombre().toLowerCase()));
        else formatedMessage = formatedMessage.replace("%tipoPrefix%", "");

        formatedMessage = formatedMessage.replace("%player%", player.getNombrePublico());
        formatedMessage = formatedMessage.replace("%message%", message);

        return formatedMessage;
    }

    public static String getDsFormatedMessage(Player player, String message) {
        String formatedMessage = lang.getString("from-mc-message");

        formatedMessage = formatedMessage.replace("%mcPrefix%", lang.getString("ds-prefixes.mc"));

        Pais pais = player.getPaisPrefix();
        if (pais != null) formatedMessage = formatedMessage.replace("%paisPrefix%", lang.getString("ds-prefixes.pais." + pais.getNombre()));
        else formatedMessage = formatedMessage.replace("%paisPrefix%", lang.getString("ds-prefixes.pais.internacional"));

        RangoUsuario rango = player.getRangoUsuario();
        if (rango != null) formatedMessage = formatedMessage.replace("%rangoPrefix%", lang.getString("ds-prefixes.rango." + rango.getNombre().toLowerCase()));
        else formatedMessage = formatedMessage.replace("%rangoPrefix%", "");

        TipoUsuario tipo = player.getTipoUsuario();
        if (tipo != null) formatedMessage = formatedMessage.replace("%tipoPrefix%", lang.getString("ds-prefixes.tipo." + tipo.getNombre().toLowerCase()));
        else formatedMessage = formatedMessage.replace("%tipoPrefix%", "");

        formatedMessage = formatedMessage.replace("%player%", player.getNombrePublico());
        formatedMessage = formatedMessage.replace("%message%", message);

        return formatedMessage;
    }

    public static String getDsFormatedMessage(Player player, String message, Pais dsPais) {
        String formatedMessage = lang.getString("ds-message");

        String dsPrefix = lang.getString("ds-prefixes.ds");
        dsPrefix = dsPrefix.replace("%paisLogo%", lang.getString("ds-prefixes.pais-logo." + dsPais.getNombre()));

        formatedMessage = formatedMessage.replace("%dsPais%", dsPrefix);

        Pais pais = player.getPaisPrefix();
        if (pais != null) formatedMessage = formatedMessage.replace("%paisPrefix%", lang.getString("ds-prefixes.pais." + pais.getNombre()));
        else formatedMessage = formatedMessage.replace("%paisPrefix%", lang.getString("ds-prefixes.pais.internacional"));

        RangoUsuario rango = player.getRangoUsuario();
        if (rango != null) formatedMessage = formatedMessage.replace("%rangoPrefix%", lang.getString("ds-prefixes.rango." + rango.getNombre().toLowerCase()));
        else formatedMessage = formatedMessage.replace("%rangoPrefix%", "");

        TipoUsuario tipo = player.getTipoUsuario();
        if (tipo != null) formatedMessage = formatedMessage.replace("%tipoPrefix%", lang.getString("ds-prefixes.tipo." + tipo.getNombre().toLowerCase()));
        else formatedMessage = formatedMessage.replace("%tipoPrefix%", "");

        formatedMessage = formatedMessage.replace("%player%", player.getNombrePublico());
        formatedMessage = formatedMessage.replace("%message%", message);

        return formatedMessage;
    }

    public static String getDsFormatedMessage(String username, String message, Pais dsPais) {
        String formatedMessage = lang.getString("ds-message");

        String dsPrefix = lang.getString("ds-prefixes.ds");
        dsPrefix = dsPrefix.replace("%paisLogo%", lang.getString("ds-prefixes.pais-logo." + dsPais.getNombre()));

        formatedMessage = formatedMessage.replace("%dsPais%", dsPrefix);

        formatedMessage = formatedMessage.replace("%player%", username);
        formatedMessage = formatedMessage.replace("%message%", message);

        formatedMessage = formatedMessage.replace("%mcPrefix%", "");
        formatedMessage = formatedMessage.replace("%paisPrefix%", "");
        formatedMessage = formatedMessage.replace("%rangoPrefix%", "");
        formatedMessage = formatedMessage.replace("%tipoPrefix%", "");

        return formatedMessage;
    }

    public static MessageEmbed getServerStarted() {
        return new EmbedBuilder()
            .setTitle(lang.getString("ds-embeds.start.title"))
            .setDescription(lang.getString("ds-embeds.start.description"))
            .setColor(lang.getInt("ds-embeds.start.color"))
            .build();
    }

    public static MessageEmbed getServerStopped() {
        return new EmbedBuilder()
            .setTitle(lang.getString("ds-embeds.stop.title"))
            .setDescription(lang.getString("ds-embeds.stop.description"))
            .setColor(lang.getInt("ds-embeds.stop.color"))
            .build();
    }

    public static MessageEmbed getDsPlayerJoined(String playerName, UUID playerUUID) {
        String message = lang.getString("ds-embeds.player-join.message").replace("%player%", playerName);
        String avatarUrl = lang.getString("avatar-url").replace("%uuid%", playerUUID.toString());
        return new EmbedBuilder()
            .setAuthor(message, null, avatarUrl)
            .setColor(lang.getInt("ds-embeds.player-join.color"))
            .build();
    }

    public static MessageEmbed getDsPlayerLeft(String playerName, UUID playerUUID) {
        String message = lang.getString("ds-embeds.player-left.message").replace("%player%", playerName);
        String avatarUrl = lang.getString("avatar-url").replace("%uuid%", playerUUID.toString());
        return new EmbedBuilder()
            .setAuthor(message, null, avatarUrl)
            .setColor(lang.getInt("ds-embeds.player-left.color"))
            .build();
    }

    public static String getMcPlayerJoined(String playerName) {
        return lang.getString("player-join").replace("%player%", playerName);
    }

    public static String getMcPlayerLeft(String playerName) {
        return lang.getString("player-left").replace("%player%", playerName);
    }

    public static MessageEmbed getDsChatJoined(String playerName, UUID playerUUID) {
        String message = lang.getString("ds-embeds.chat-join.message").replace("%player%", playerName);
        String avatarUrl = lang.getString("avatar-url").replace("%uuid%", playerUUID.toString());
        return new EmbedBuilder()
            .setAuthor(message, null, avatarUrl)
            .setColor(lang.getInt("ds-embeds.chat-join.color"))
            .build();
    }

    public static MessageEmbed getDsChatLeft(String playerName, UUID playerUUID) {
        String message = lang.getString("ds-embeds.chat-left.message").replace("%player%", playerName);
        String avatarUrl = lang.getString("avatar-url").replace("%uuid%", playerUUID.toString());
        return new EmbedBuilder()
            .setAuthor(message, null, avatarUrl)
            .setColor(lang.getInt("ds-embeds.chat-left.color"))
            .build();
    }

    public static String getMcChatJoined(String playerName) {
        return lang.getString("chat-join").replace("%player%", playerName);
    }

    public static String getMcChatLeft(String playerName) {
        return lang.getString("chat-left").replace("%player%", playerName);
    }

    public static MessageEmbed getDsRangoUsuarioSwitched(RangoUsuario rangoUsuario) {
        return new EmbedBuilder()
            .setTitle(lang.getString("ds-embeds.rango-switched.title").replace("%rango%", rangoUsuario.getNombre()))
            .setDescription(lang.getString("ds-embeds.rango-switched.description").replace("%descripcion%", rangoUsuario.getDescripcion()))
            .setColor(lang.getInt("ds-embeds.rango-switched.color"))
            .build();
    }

    public static MessageEmbed getDsTipoUsuarioSwitched(TipoUsuario tipoUsuario) {
        return new EmbedBuilder()
            .setTitle(lang.getString("ds-embeds.tipo-switched.title").replace("%tipo%", tipoUsuario.getNombre()))
            .setDescription(
                lang.getString("ds-embeds.tipo-switched.description").replace("%descripcion%", tipoUsuario.getDescripcion()
                + "\n" + lang.getString("ds-embeds.tipo-switched.max-projects").replace("%maxProyectos%", String.valueOf(tipoUsuario.getCantProyecSim()))
            ))
            .setColor(lang.getInt("ds-embeds.tipo-switched.color"))
            .build();
    }

    public static MessageEmbed getDsManagerAdded(String paisName) {
        String message = lang.getString("ds-embeds.manager-target-added.title").replace("%pais%", paisName);
        return new EmbedBuilder().setTitle(message).setColor(lang.getInt("ds-embeds.manager-target-added.color")).build();
    }

    public static MessageEmbed getDsManagerRemoved(String paisName) {
        String message = lang.getString("ds-embeds.manager-target-removed.title").replace("%pais%", paisName);
        return new EmbedBuilder().setTitle(message).setColor(lang.getInt("ds-embeds.manager-target-removed.color")).build();
    }

    public static MessageEmbed getDsReviewerAdded(String paisName) {
        String message = lang.getString("ds-embeds.reviewer-target-added.title").replace("%pais%", paisName);
        return new EmbedBuilder().setTitle(message).setColor(lang.getInt("ds-embeds.reviewer-target-added.color")).build();
    }

    public static MessageEmbed getDsReviewerRemoved(String paisName) {
        String message = lang.getString("ds-embeds.reviewer-target-removed.title").replace("%pais%", paisName);
        return new EmbedBuilder().setTitle(message).setColor(lang.getInt("ds-embeds.reviewer-target-removed.color")).build();
    }

    public static MessageEmbed getDsLinkSuccess(String playerName) {
        String message = lang.getString("ds-embeds.link-success.title").replace("%player%", playerName);
        return new EmbedBuilder().setTitle(message).setColor(lang.getInt("ds-embeds.link-success.color")).build();
    }

    public static MessageEmbed getDsProjectAccepted(String proyectoId, String comentario, String nombre) {
        String title = lang.getString("ds-embeds.project-accepted.title");
        String description = lang.getString("ds-embeds.project-accepted.id").replace("%proyectoId%", proyectoId);
        if (comentario != null && !comentario.isBlank()) description = description
            + "\n" + lang.getString("ds-embeds.project-accepted.comment").replace("%comentario%", comentario);
        if (nombre != null && !nombre.isBlank()) description = description
            + "\n" + lang.getString("ds-embeds.project-accepted.nombre").replace("%nombre%", nombre);
        return new EmbedBuilder().setTitle(title).setDescription(description).setColor(lang.getInt("ds-embeds.project-accepted.color")).build();
    }

    public static MessageEmbed getDsProjectRejected(String proyectoId, String comentario, String nombre) {
        String title = lang.getString("ds-embeds.project-rejected.title");
        String description = lang.getString("ds-embeds.project-rejected.id").replace("%proyectoId%", proyectoId);
        if (nombre != null && !nombre.isBlank()) description = description
            + "\n" + lang.getString("ds-embeds.project-rejected.nombre").replace("%nombre%", nombre);
        if (comentario != null && !comentario.isBlank()) description = description
            + "\n" + lang.getString("ds-embeds.project-rejected.comment").replace("%comentario%", comentario);
        return new EmbedBuilder().setTitle(title).setDescription(description).setColor(lang.getInt("ds-embeds.project-rejected.color")).build();
    }

    public static MessageEmbed getDsProjectRequestExpired(String proyectoId, String nombre) {
        String title = lang.getString("ds-embeds.project-expired.title");
        String description = lang.getString("ds-embeds.project-expired.id").replace("%proyectoId%", proyectoId);
        if (nombre != null && !nombre.isBlank()) description = description
            + "\n" + lang.getString("ds-embeds.project-expired.nombre").replace("%nombre%", nombre);
        return new EmbedBuilder().setTitle(title).setDescription(description).setColor(lang.getInt("ds-embeds.project-expired.color")).build();
    }

    public static MessageEmbed getDsProjectFinishAccepted(String proyectoId, String comentario, String nombre) {
        String title = lang.getString("ds-embeds.project-finish-accepted.title");
        String description = lang.getString("ds-embeds.project-finish-accepted.id").replace("%proyectoId%", proyectoId);
        if (nombre != null && !nombre.isBlank()) description = description
            + "\n" + lang.getString("ds-embeds.project-finish-accepted.nombre").replace("%nombre%", nombre);
        if (comentario != null && !comentario.isBlank()) description = description
            + "\n" + lang.getString("ds-embeds.project-finish-accepted.comment").replace("%comentario%", comentario);
        return new EmbedBuilder().setTitle(title).setDescription(description).setColor(lang.getInt("ds-embeds.project-finish-accepted.color")).build();
    }

    public static MessageEmbed getDsProjectFinishRejected(String proyectoId, String comentario, String nombre) {
        String title = lang.getString("ds-embeds.project-finish-rejected.title");
        String description = lang.getString("ds-embeds.project-finish-rejected.id").replace("%proyectoId%", proyectoId);
        if (nombre != null && !nombre.isBlank()) description = description
            + "\n" + lang.getString("ds-embeds.project-finish-rejected.nombre").replace("%nombre%", nombre);
        if (comentario != null && !comentario.isBlank()) description = description
            + "\n" + lang.getString("ds-embeds.project-finish-rejected.comment").replace("%comentario%", comentario);
        return new EmbedBuilder().setTitle(title).setDescription(description).setColor(lang.getInt("ds-embeds.project-finish-rejected.color")).build();
    }

    public static MessageEmbed getDsProjectFinishRequestExpired(String proyectoId, String nombre) {
        String title = lang.getString("ds-embeds.project-finish-expired.title");
        String description = lang.getString("ds-embeds.project-finish-expired.id").replace("%proyectoId%", proyectoId);
        if (nombre != null && !nombre.isBlank()) description = description
            + "\n" + lang.getString("ds-embeds.project-finish-expired.nombre").replace("%nombre%", nombre);
        return new EmbedBuilder().setTitle(title).setDescription(description).setColor(lang.getInt("ds-embeds.project-finish-expired.color")).build();
    }

    public static MessageEmbed getDsProjectFinishRequested(String proyectoId, String nombre, String requesterName) {
        String title = lang.getString("ds-embeds.project-finish-requested.title").replace("%player%", requesterName);
        String description = lang.getString("ds-embeds.project-finish-requested.id").replace("%proyectoId%", proyectoId);
        if (nombre != null && !nombre.isBlank()) description = description
            + "\n" + lang.getString("ds-embeds.project-finish-requested.nombre").replace("%nombre%", nombre);
        return new EmbedBuilder().setTitle(title).setDescription(description).setColor(lang.getInt("ds-embeds.project-finish-requested.color")).build();
    }

    public static MessageEmbed getDsMemberAdded(String proyectoId, String nombre) {
        String description = lang.getString("ds-embeds.project-member-added.id").replace("%proyectoId%", proyectoId);
        if (nombre != null && !nombre.isBlank()) description = description
            + "\n" + lang.getString("ds-embeds.project-member-added.nombre").replace("%nombre%", nombre);
        return new EmbedBuilder().setTitle(lang.getString("ds-embeds.project-member-added.title")).setDescription(description).setColor(lang.getInt("ds-embeds.project-member-added.color")).build();
    }

    public static MessageEmbed getDsMemberRemoved(String proyectoId, String nombre) {
        String description = lang.getString("ds-embeds.project-member-removed.id").replace("%proyectoId%", proyectoId);
        if (nombre != null && !nombre.isBlank()) description = description
            + "\n" + lang.getString("ds-embeds.project-member-removed.nombre").replace("%nombre%", nombre);
        return new EmbedBuilder().setTitle(lang.getString("ds-embeds.project-member-removed.title")).setDescription(description).setColor(lang.getInt("ds-embeds.project-member-removed.color")).build();
    }

    public static MessageEmbed getDsMemberJoinRequest(String proyectoId, String nombre, String playerName) {
        String title = lang.getString("ds-embeds.project-member-join-request.title").replace("%player%", playerName);
        String description = lang.getString("ds-embeds.project-member-join-request.id").replace("%proyectoId%", proyectoId);
        if (nombre != null && !nombre.isBlank()) description = description
            + "\n" + lang.getString("ds-embeds.project-member-join-request.nombre").replace("%nombre%", nombre);
        return new EmbedBuilder().setTitle(title).setDescription(description).setColor(lang.getInt("ds-embeds.project-member-join-request.color")).build();
    }

    public static MessageEmbed getDsMemberJoinRequestExpired(String proyectoId, String nombre) {
        String description = lang.getString("ds-embeds.project-join-request-expired.id").replace("%proyectoId%", proyectoId);
        if (nombre != null && !nombre.isBlank()) description = description
            + "\n" + lang.getString("ds-embeds.project-join-request-expired.nombre").replace("%nombre%", nombre);
        return new EmbedBuilder().setTitle(lang.getString("ds-embeds.project-join-request-expired.title")).setDescription(description).setColor(lang.getInt("ds-embeds.project-member-join-request-expired.color")).build();
    }

    public static MessageEmbed getDsMemberJoinRequestExpiredLider(String proyectoId, String nombre, String playerName) {
        String title = lang.getString("ds-embeds.project-join-request-expired-lider.title").replace("%player%", playerName);
        String description = lang.getString("ds-embeds.project-join-request-expired-lider.id").replace("%proyectoId%", proyectoId);
        if (nombre != null && !nombre.isBlank()) description = description
            + "\n" + lang.getString("ds-embeds.project-join-request-expired-lider.nombre").replace("%nombre%", nombre);
        return new EmbedBuilder().setTitle(title).setDescription(description).setColor(lang.getInt("ds-embeds.project-member-join-request-expired-lider.color")).build();
    }
    
    public static MessageEmbed getDsMemberJoinRequestRejected(String proyectoId, String nombre) {
        String description = lang.getString("ds-embeds.project-join-request-rejected.id").replace("%proyectoId%", proyectoId);
        if (nombre != null && !nombre.isBlank()) description = description
            + "\n" + lang.getString("ds-embeds.project-join-request-rejected.nombre").replace("%nombre%", nombre);
        return new EmbedBuilder().setTitle(lang.getString("ds-embeds.project-join-request-rejected.title")).setDescription(description).setColor(lang.getInt("ds-embeds.project-join-request-rejected.color")).build();
    }

    public static MessageEmbed getDsMemberJoinRequestAccepted(String proyectoId, String nombre) {
        String description = lang.getString("ds-embeds.project-join-request-accepted.id").replace("%proyectoId%", proyectoId);
        if (nombre != null && !nombre.isBlank()) description = description
            + "\n" + lang.getString("ds-embeds.project-join-request-accepted.nombre").replace("%nombre%", nombre);
        return new EmbedBuilder().setTitle(lang.getString("ds-embeds.project-join-request-accepted.title")).setDescription(description).setColor(lang.getInt("ds-embeds.project-join-request-accepted.color")).build();
    }

    public static MessageEmbed getDsMemberAddedMember(String proyectoId, String nombre, String addedMemberName) {
        String title = lang.getString("ds-embeds.project-member-added-member.title").replace("%player%", addedMemberName);
        String description = lang.getString("ds-embeds.project-member-added-member.id").replace("%proyectoId%", proyectoId);
        if (nombre != null && !nombre.isBlank()) description = description
            + "\n" + lang.getString("ds-embeds.project-member-added-member.nombre").replace("%nombre%", nombre);
        return new EmbedBuilder().setTitle(title).setDescription(description).setColor(lang.getInt("ds-embeds.project-member-added-member.color")).build();
    }

    public static MessageEmbed getDsMemberRemovedMember(String proyectoId, String nombre, String removedMemberName) {
        String title = lang.getString("ds-embeds.project-member-removed-member.title").replace("%player%", removedMemberName);
        String description = lang.getString("ds-embeds.project-member-removed-member.id").replace("%proyectoId%", proyectoId);
        if (nombre != null && !nombre.isBlank()) description = description
            + "\n" + lang.getString("ds-embeds.project-member-removed-member.nombre").replace("%nombre%", nombre);
        return new EmbedBuilder().setTitle(title).setDescription(description).setColor(lang.getInt("ds-embeds.project-member-removed-member.color")).build();
    }

    @SuppressWarnings("null")
    public static MessageEmbed getDsProjectCreated(Proyecto proyecto) {
        Player player = proyecto.getLider();
        ProyectoRegistry pr = ProyectoRegistry.getInstance();
        int[] counts = ProyectoRegistry.getInstance().getCounts(player);    
        String title = lang.getString("ds-embeds.project-created.title").replace("%player%",player.getNombre());
        Polygon polygon = proyecto.getPoligono();
        Point centroid = polygon.getCentroid();
        double[] geoCoords = TerraUtils.toGeo(centroid.getX(), centroid.getY());
        String coords = geoCoords[1] + ", " + geoCoords[0];
        EmbedBuilder eb = new EmbedBuilder().setTitle(title);
        TipoUsuarioRegistry tur = TipoUsuarioRegistry.getInstance();
        if (tur.getVisita().equals(player.getTipoUsuario())) eb.appendDescription(lang.getString("ds-embeds.project-created.player-is-visita"));
        if (tur.getPostulante().equals(player.getTipoUsuario())) eb.appendDescription(lang.getString("ds-embeds.project-created.player-is-postulante"));
        if (pr.hasCollisions(proyecto)) eb.appendDescription("\n" + lang.getString("ds-embeds.project-created.has-collisions"));

        eb.addField("Rango", player.getRangoUsuario().getNombre(), true)   
            .addField("Tipo", player.getTipoUsuario().getNombre(), true)
            .addField("Fecha de Ingreso", DateUtils.getDsTimestamp(player.getFechaIngreso()), true)
            .addField("Proyectos Completados", String.valueOf(counts[0]), true)
            .addField("Proyectos Activos", String.valueOf(counts[1]), true)
            .addField("────────────────────────────────────────", "", false)
            .addField("Tipo Proyecto", proyecto.getTipoProyecto().getNombre(), true)
            .addField("Max. Miembros", String.valueOf(proyecto.getTipoProyecto().getMaxMiembros()), true)
            .addField("Tamaño", String.valueOf(polygon.getArea()), true)
            .addField("Coordenadas", coords, false)
            .setImage("attachment://map.png")
            .setColor(lang.getInt("ds-embeds.project-created.color"))
                .setFooter("Creado el " + DateUtils.formatDateHour(proyecto.getFechaCreado()) + ".");
            if (proyecto.getNombre() != null && !proyecto.getNombre().isBlank()) eb.addField("Nombre del Proyecto", proyecto.getNombre(), false);
        if (proyecto.getDescripcion() != null && !proyecto.getDescripcion().isBlank()) eb.addField("Descripción", proyecto.getDescripcion(), false);
        eb.addField("", lang.getString("ds-embeds.project-created.polygons-colors"), false);
        return eb.build();
    } //TODO: añadir vencimiento de request.

}
