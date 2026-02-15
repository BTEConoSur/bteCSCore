package com.bteconosur.core;

import java.io.File;
import java.time.Instant;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.configuration.file.YamlConfiguration;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import com.bteconosur.core.chat.ChatUtil;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.util.ConsoleLogger;
import com.bteconosur.core.util.DiscordLogger;
import com.bteconosur.core.util.PlaceholderUtil;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.core.util.SatMapUtils;
import com.bteconosur.core.util.TerraUtils;
import com.bteconosur.db.PermissionManager;
import com.bteconosur.db.model.Division;
import com.bteconosur.db.model.Interaction;
import com.bteconosur.db.model.Pais;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.Proyecto;
import com.bteconosur.db.model.TipoProyecto;
import com.bteconosur.db.model.TipoUsuario;
import com.bteconosur.db.registry.InteractionRegistry;
import com.bteconosur.db.registry.PaisRegistry;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.db.registry.ProyectoRegistry;
import com.bteconosur.db.registry.TipoProyectoRegistry;
import com.bteconosur.db.registry.TipoUsuarioRegistry;
import com.bteconosur.db.util.Estado;
import com.bteconosur.db.util.InteractionKey;
import com.bteconosur.discord.util.ProjectRequestService;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

//TODO: vereficar casos edgde.
public class ProjectManager {

    private static ProjectManager instance;

    private final YamlConfiguration lang;
    private final YamlConfiguration config;
    
    public ProjectManager() {
        ConfigHandler configHandler = ConfigHandler.getInstance();
        lang = ConfigHandler.getInstance().getLang();
        config = configHandler.getConfig();

        ConsoleLogger.info(lang.getString("project-manager-initializing"));
    }

    public Set<Player> getMembers(Proyecto proyecto) {
        PlayerRegistry playerRegistry = PlayerRegistry.getInstance();
        Set<Player> detachedMembers = proyecto.getMiembros();
        Set<Player> members = new HashSet<>();
        for (Player p : detachedMembers) {
            Player fullPlayer = playerRegistry.get(p.getUuid());
            members.add(fullPlayer);
        }
        return members;
    }

    public Player getLider(Proyecto proyecto) {
        PlayerRegistry playerRegistry = PlayerRegistry.getInstance();
        Player detachedLider = proyecto.getLider();
        if (detachedLider == null) return null;
        return playerRegistry.get(detachedLider.getUuid());
    }

    public Set<Player> getJoinRequests(Proyecto proyecto) {
        PlayerRegistry playerRegistry = PlayerRegistry.getInstance();
        InteractionRegistry interactionRegistry = InteractionRegistry.getInstance();
        Set<Player> requesters = new HashSet<>();
        List<Interaction> interactions = interactionRegistry.findByInteractionKey(InteractionKey.JOIN_PROJECT);
        for (Interaction interaction : interactions) {
            if (interaction.getProjectId() != null && interaction.getProjectId().equals(proyecto.getId())) {
                Player requester = playerRegistry.get(interaction.getPlayerId());
                if (requester != null) requesters.add(requester);
            }
        }
        return requesters;
    }   

    public void createProject(String nombre, String descripcion, Polygon regionPolygon, Player player) {
        Interaction previousctx = InteractionRegistry.getInstance().findCreateRequest(player);
        if (previousctx != null) {
            PlayerLogger.warn(player, lang.getString("project-request-already"), (String) null);
            return;
        }
        Double tamaño = regionPolygon.getArea();
        TipoProyecto tipoProyecto = TipoProyectoRegistry.getInstance().get(tamaño);
        if (tipoProyecto == null) {
            PlayerLogger.error(player, lang.getString("invalid-project-size"), (String) null);
            return;
        }

        PaisRegistry paisr = PaisRegistry.getInstance();
        Division division = paisr.findDivisionByPolygon(regionPolygon, paisr.findByPolygon(regionPolygon));
        if (division == null) {
            PlayerLogger.error(player, lang.getString("invalid-project-location"), (String) null);
            return;
        }

        ProyectoRegistry pr = ProyectoRegistry.getInstance(); 
        int activeProjects = pr.getCounts(player)[1];
        int maxActiveProjects = player.getTipoUsuario().getCantProyecSim();
        if (activeProjects >= maxActiveProjects) {
            String message = lang.getString("max-active-projects").replace("%maxProjects%", String.valueOf(maxActiveProjects)).replace("%currentProjects%", String.valueOf(activeProjects));
            PlayerLogger.error(player, message, (String) null);
            return;
        }
        
        Proyecto proyecto = new Proyecto(nombre, descripcion, Estado.EN_CREACION, regionPolygon, tamaño, tipoProyecto, player, division, Date.from(Instant.now()));
        pr.load(proyecto);

        File contextImage = SatMapUtils.downloadContext(proyecto, pr.getOverlapping(proyecto.getId(), proyecto.getPoligono()));
        if (contextImage == null) {
            PlayerLogger.error(player, lang.getString("internal-error"), (String) null);
            pr.unload(proyecto.getId());
            return;
        }
        //SatMapUtils.downloadImage(proyecto, "Image");
        Pais pais = proyecto.getPais();
        Boolean success = ProjectRequestService.sendProjectRequest(proyecto, contextImage);
        if (!success){ 
            PlayerLogger.info(player, lang.getString("project-request-failed"), (String) null);
            pr.unload(proyecto.getId());
            return;
        }

        PlayerLogger.info(player, lang.getString("project-request-success").replace("%proyectoId%", proyecto.getId()), (String) null);
        
        String countryLog = lang.getString("project-create-log")
            .replace("%player%", player.getNombre())
            .replace("%proyectoId%", proyecto.getId())
            .replace("%tipoId%", tipoProyecto.getId().toString())
            .replace("%tipoNombre%", tipoProyecto.getNombre())
            .replace("%divisionId%", division.getId().toString())
            .replace("%divisionNombre%", division.getNombre())
            .replace("%proyectoNombre%", proyecto.getNombre() != null ? proyecto.getNombre() : "Sin Nombre")
            .replace("%proyectoDescripcion%", proyecto.getDescripcion() != null ? proyecto.getDescripcion() : "Sin Descripción");
        DiscordLogger.countryLog(countryLog, pais);
    }

    public void acceptCreateRequest(String proyectoId, Player staff, Long interactionId, String comentario) {
        Proyecto proyecto = ProyectoRegistry.getInstance().get(proyectoId);
        proyecto.setEstado(Estado.ACTIVO);
        ProyectoRegistry.getInstance().merge(proyecto.getId());
        InteractionRegistry.getInstance().unload(interactionId);
        TipoUsuarioRegistry tur = TipoUsuarioRegistry.getInstance();
        Player lider = getLider(proyecto);
        Pais pais = proyecto.getPais();

        String countryLog = lang.getString("project-create-request-accepted-log")
            .replace("%staff%", staff.getNombre())
            .replace("%lider%", lider.getNombre()).replace("%proyectoId%", proyecto.getId());
        DiscordLogger.countryLog(countryLog, pais);

        String message = lang.getString("project-create-request-accepted").replace("%proyectoId%", proyecto.getId());
        PlayerLogger.info(lider, message, ChatUtil.getDsProjectAccepted(proyecto.getId(), comentario, proyecto.getNombre()));
        if (comentario != null && !comentario.isBlank()) {
            String commentMessage = lang.getString("project-create-request-comment-accepted").replace("%comentario%", comentario);
            PlayerLogger.info(lider, commentMessage, (String) null);
        }

        if (tur.getVisita().equals(lider.getTipoUsuario())) {
            TipoUsuario postulante = tur.getPostulante();
            PermissionManager.getInstance().switchTipoUsuario(lider, postulante);
            PlayerLogger.info(lider, lang.getString("tipo-switched").replace("%tipo%", postulante.getNombre()), ChatUtil.getDsTipoUsuarioSwitched(postulante));
            DiscordLogger.countryLog(lang.getString("tipo-promote-log").replace("%player%", lider.getNombre()).replace("%tipo%", postulante.getNombre()), pais);
        };
    }

    public void cancelCreateRequest(String proyectoId, Player staff, Long interactionId , String comentario) {
        Proyecto proyecto = ProyectoRegistry.getInstance().get(proyectoId);
        Pais pais = proyecto.getPais();
        String proyectoName = proyecto.getNombre();
        Player lider = getLider(proyecto);

        InteractionRegistry.getInstance().unload(interactionId);
        String message = lang.getString("project-create-request-rejected").replace("%proyectoId%", proyectoId);
        PlayerLogger.info(lider, message, ChatUtil.getDsProjectRejected(proyecto.getId(), comentario, proyectoName));
        if (comentario != null && !comentario.isBlank()) {
            String commentMessage = lang.getString("project-create-request-comment-rejected").replace("%comentario%", comentario);
            PlayerLogger.info(lider, commentMessage, (String) null);
        }

        String countryLog = lang.getString("project-create-request-rejected-log").replace("%staff%", staff.getNombre()).replace("%lider%", lider.getNombre()).replace("%proyectoId%", proyectoId);
        DiscordLogger.countryLog(countryLog, pais);
        deleteProject(proyecto, null);
    }

    public void expiredCreateRequest(String proyectoId, Long interactionId) {
        Proyecto proyecto = ProyectoRegistry.getInstance().get(proyectoId);
        Pais pais = proyecto.getPais();
        String proyectoName = proyecto.getNombre();
        Player lider = getLider(proyecto);

        InteractionRegistry.getInstance().unload(interactionId);
        String message = lang.getString("project-create-request-expired").replace("%proyectoId%", proyectoId);
        PlayerLogger.info(lider, message, ChatUtil.getDsProjectRequestExpired(proyectoId, proyectoName));

        String countryLog = lang.getString("project-create-request-expired-log").replace("%lider%", lider.getNombre()).replace("%proyectoId%", proyectoId);
        DiscordLogger.countryLog(countryLog, pais);
        deleteProject(proyecto, null);
    }

    public void deleteProject(Proyecto proyecto, UUID commandId) {
        Pais pais = proyecto.getPais();
        Player lider = getLider(proyecto);
        String proyectoId = proyecto.getId();
        ProyectoRegistry.getInstance().unload(proyectoId);
        PlayerRegistry playerRegistry = PlayerRegistry.getInstance();
        File folder = new File(BTEConoSur.getInstance().getDataFolder(), "images/projects");
        File contextFile = new File(folder, proyectoId + "_context.png");
        if (contextFile.exists()) {
            contextFile.delete();
        }
        File imageFile = new File(folder, proyectoId + ".png");
        if (imageFile.exists()) {
            imageFile.delete();
        }
        if (commandId != null) {
            Player commandPlayer = playerRegistry.get(commandId);
            Set<Player> members = getMembers(proyecto);
            members.add(lider);
            String memberNotification = lang.getString("project-deleted-member").replace("%proyectoId%", proyectoId);
            MessageEmbed dsMemberNotification = ChatUtil.getDsProjectDeleted(proyectoId, proyecto.getNombre());
            for (Player member : members) PlayerLogger.info(member, memberNotification, dsMemberNotification);
            String countryLog = lang.getString("project-delete-staff-log").replace("%staff%", commandPlayer.getNombre()).replace("%lider%", lider.getNombre()).replace("%proyectoId%", proyectoId);
            DiscordLogger.countryLog(countryLog, pais);
            return;
        }
        String countryLog = lang.getString("project-delete-log").replace("%lider%", lider.getNombre()).replace("%proyectoId%", proyectoId);
        DiscordLogger.countryLog(countryLog, pais);
    }

    public void createJoinRequest(String proyectoId, UUID playerId) {
        Interaction previousctx = InteractionRegistry.getInstance().findJoinRequest(proyectoId, playerId);
        ProyectoRegistry pr = ProyectoRegistry.getInstance();
        Proyecto proyecto = pr.get(proyectoId);
        Player player = PlayerRegistry.getInstance().get(playerId);
        Pais pais = proyecto.getPais();
        if (previousctx != null) {
            PlayerLogger.warn(player, lang.getString("project-join-already-requested").replace("%proyectoId%", proyecto.getId()), (String) null);
            return;
        }
        ProjectRequestService.sendProjectJoinRequest(proyecto, player);
        PlayerLogger.info(player, lang.getString("project-join-success").replace("%proyectoId%", proyecto.getId()), (String) null);
        String countryLog = lang.getString("project-join-request-log").replace("%player%", player.getNombre()).replace("%proyectoId%", proyecto.getId());
        DiscordLogger.countryLog(countryLog, pais);
    }

    public void cancelJoinRequest(String proyectoId, UUID playerId) {
        InteractionRegistry interactionRegistry = InteractionRegistry.getInstance();
        Interaction interaction = interactionRegistry.findJoinRequest(proyectoId, playerId);
        if (interaction == null) return;
        interactionRegistry.unload(interaction.getId());    
    }

    public void expiredJoinRequest(String proyectoId, UUID playerId) {
        Proyecto proyecto = ProyectoRegistry.getInstance().get(proyectoId);
        Player player = PlayerRegistry.getInstance().get(playerId);
        cancelJoinRequest(proyectoId, playerId);
        Pais pais = proyecto.getPais();
        String message = lang.getString("project-join-request-expired").replace("%proyectoId%", proyecto.getId());
        String messageLider = lang.getString("project-join-request-expired-lider").replace("%proyectoId%", proyecto.getId()).replace("%player%", player.getNombre());
        PlayerLogger.info(player, message, ChatUtil.getDsMemberJoinRequestExpired(proyecto.getId(), proyecto.getNombre()));
        PlayerLogger.info(getLider(proyecto), messageLider, ChatUtil.getDsMemberJoinRequestExpiredLider(proyecto.getId(), proyecto.getNombre(), player.getNombre()));
        String countryLog = lang.getString("project-join-request-expired-log").replace("%player%", player.getNombre()).replace("%proyectoId%", proyecto.getId());
        DiscordLogger.countryLog(countryLog, pais);
    }

    public void acceptJoinRequest(String proyectoId, UUID playerId, Long interactionId, UUID commandId) {
        Proyecto proyecto = ProyectoRegistry.getInstance().get(proyectoId);
        cancelJoinRequest(proyectoId, playerId);
        Pais pais = proyecto.getPais();
        Player player = PlayerRegistry.getInstance().get(playerId);
        Player commandPlayer = PlayerRegistry.getInstance().get(commandId);
        String message = lang.getString("project-join-accepted").replace("%proyectoId%", proyecto.getId());
        PlayerLogger.info(player, message, ChatUtil.getDsMemberJoinRequestAccepted(proyecto.getId(), proyecto.getNombre()));
        String countryLog = lang.getString("project-join-accepted-log").replace("%lider%", commandPlayer.getNombre()).replace("%player%", player.getNombre()).replace("%proyectoId%", proyecto.getId());
        DiscordLogger.countryLog(countryLog, pais);
        joinProject(proyectoId, playerId, commandId, false);
    }

    public void rejectJoinRequest(String proyectoId, UUID playerId, Long interactionId, UUID commandId) {
        Proyecto proyecto = ProyectoRegistry.getInstance().get(proyectoId);
        cancelJoinRequest(proyectoId, playerId);
        Pais pais = proyecto.getPais();
        Player player = PlayerRegistry.getInstance().get(playerId);
        Player commandPlayer = PlayerRegistry.getInstance().get(commandId);
        String message = lang.getString("project-join-rejected").replace("%proyectoId%", proyecto.getId());
        PlayerLogger.info(player, message, ChatUtil.getDsMemberJoinRequestRejected(proyecto.getId(), proyecto.getNombre()));
        String countryLog = lang.getString("project-join-rejected-log").replace("%lider%", commandPlayer.getNombre()).replace("%player%", player.getNombre()).replace("%proyectoId%", proyecto.getId());
        DiscordLogger.countryLog(countryLog, pais);
    }

    public void joinProject(String proyectoId, UUID playerId, UUID commandId, Boolean isAdded) {
        Proyecto proyecto = ProyectoRegistry.getInstance().get(proyectoId);
        Player player = PlayerRegistry.getInstance().get(playerId);
        Player commandPlayer = PlayerRegistry.getInstance().get(commandId);
        Player lider = getLider(proyecto);
        proyecto.addMiembro(player);
        ProyectoRegistry.getInstance().merge(proyecto.getId());
        Pais pais = proyecto.getPais();
        String memberNotification = lang.getString("project-member-added").replace("%proyectoId%", proyecto.getId());
        PlayerLogger.info(player, memberNotification, ChatUtil.getDsMemberAdded(proyecto.getId(), proyecto.getNombre()));

        String memberNotificationPlayer = lang.getString("project-member-added-member").replace("%player%", player.getNombre()).replace("%proyectoId%", proyecto.getId());
        Set<Player> members = getMembers(proyecto);
        if (!commandPlayer.equals(lider)) members.add(lider);
        for (Player member : members) {
            if (!member.equals(player   )) {
                PlayerLogger.info(member, memberNotificationPlayer, ChatUtil.getDsMemberAddedMember(proyecto.getId(), proyecto.getNombre(), player.getNombre()));
            }
        }

        if (isAdded) {
            String countryLog = lang.getString("project-added-member-log").replace("%lider%", commandPlayer.getNombre()).replace("%player%", player.getNombre()).replace("%proyectoId%", proyecto.getId());
            DiscordLogger.countryLog(countryLog, pais);
            return;
        }
        String countryLog = lang.getString("project-join-log").replace("%player%", player.getNombre()).replace("%proyectoId%", proyecto.getId());
        DiscordLogger.countryLog(countryLog, pais);
    }

    public void leaveProject(String proyectoId, UUID playerId) {
        ProyectoRegistry pr = ProyectoRegistry.getInstance();
        PlayerRegistry playerRegistry = PlayerRegistry.getInstance();
        Proyecto proyecto = pr.get(proyectoId);
        Player player = playerRegistry.get(playerId);
        Player lider = getLider(proyecto);
        Pais pais = proyecto.getPais();
        if (player.equals(lider)) {
            proyecto.setLider(null);
            proyecto.setEstado(Estado.ABANDONADO);
            pr.merge(proyecto.getId());
            String countryLog = lang.getString("project-leader-left-log").replace("%lider%", player.getNombre()).replace("%proyectoId%", proyecto.getId());
            DiscordLogger.countryLog(countryLog, pais);
        } else {
            proyecto.removeMiembro(player);
            pr.merge(proyecto.getId());
            String memberNotificationPlayer = lang.getString("project-member-left-member").replace("%player%", player.getNombre()).replace("%proyectoId%", proyecto.getId());
            Set<Player> members = getMembers(proyecto);
            members.add(lider);
            for (Player member : members) {
                if (!member.equals(player)) {
                    PlayerLogger.info(member, memberNotificationPlayer, ChatUtil.getDsMemberLeftMember(proyecto.getId(), proyecto.getNombre(), player.getNombre()));
                }
            }
            String countryLog = lang.getString("project-left-log").replace("%player%", player.getNombre()).replace("%proyectoId%", proyecto.getId());
            DiscordLogger.countryLog(countryLog, pais);
        }
    }

    public void removeFromProject(String proyectoId, UUID playerId, UUID commanUuid) {
        Proyecto proyecto = ProyectoRegistry.getInstance().get(proyectoId);
        PlayerRegistry playerRegistry = PlayerRegistry.getInstance();
        Player player = playerRegistry.get(playerId);
        Player lider = getLider(proyecto);
        Player commandPlayer = playerRegistry.get(commanUuid);
        if (player.equals(lider)) {
            proyecto.setLider(null);
            proyecto.setEstado(Estado.ABANDONADO);
            ProyectoRegistry.getInstance().merge(proyecto.getId());
            String leaderNotification = lang.getString("project-leader-removed").replace("%proyectoId%", proyecto.getId());
            PlayerLogger.info(player, leaderNotification, ChatUtil.getDsLeaderRemoved(proyecto.getId(), proyecto.getNombre()));
            String countryLog = lang.getString("project-leader-removed-log").replace("%staff%", commandPlayer.getNombre()).replace("%lider%", player.getNombre()).replace("%proyectoId%", proyecto.getId());
            DiscordLogger.countryLog(countryLog, proyecto.getPais());
            return;
        }
        proyecto.removeMiembro(player);
        ProyectoRegistry.getInstance().merge(proyecto.getId());
        Pais pais = proyecto.getPais();
        String memberNotification = lang.getString("project-member-removed").replace("%proyectoId%", proyecto.getId());
        PlayerLogger.info(player, memberNotification, ChatUtil.getDsMemberRemoved(proyecto.getId(), proyecto.getNombre()));

        String memberNotificationPlayer = lang.getString("project-member-removed-member").replace("%player%", player.getNombre()).replace("%proyectoId%", proyecto.getId());
        Set<Player> members = getMembers(proyecto);
        if (!commandPlayer.equals(lider)) members.add(lider);
        for (Player member : members) {
            if (!member.equals(player)) {
                PlayerLogger.info(member, memberNotificationPlayer, ChatUtil.getDsMemberRemovedMember(proyecto.getId(), proyecto.getNombre(), player.getNombre()));
            }
        }

        String countryLog = lang.getString("project-removed-log").replace("%player%", player.getNombre()).replace("%proyectoId%", proyecto.getId()).replace("%lider%", commandPlayer.getNombre());
        DiscordLogger.countryLog(countryLog, pais);
    }

    public void createFinishRequest(String proyectoId, UUID requesterId) {
        Proyecto proyecto = ProyectoRegistry.getInstance().get(proyectoId);
        proyecto.setEstado(Estado.EN_FINALIZACION);
        Player requester = PlayerRegistry.getInstance().get(requesterId);
        ProyectoRegistry.getInstance().merge(proyecto.getId());
        Interaction interaction = new Interaction(
            requester.getUuid(),
            proyecto.getId(),
            InteractionKey.FINISH_PROJECT,
            Instant.now(),
            Instant.now().plusSeconds(config.getLong("interaction-expirations.finish-project") * 60)
        );
        InteractionRegistry.getInstance().load(interaction);
        Player lider = getLider(proyecto);
    
        Point centroid = proyecto.getPoligono().getCentroid();
        double[] geoCoords = TerraUtils.toGeo(centroid.getX(), centroid.getY());
        String coords = geoCoords[1] + ", " + geoCoords[0];
        Pais pais = proyecto.getPais();

        String countryLog = lang.getString("project-finish-request-log").replace("%requester%", requester.getNombre()).replace("%proyectoId%", proyecto.getId());
        DiscordLogger.countryLog(countryLog, pais);
        
        String dsNotification = lang.getString("ds-reviewer-notification-finish-project").replace("%pais%", pais.getNombrePublico())
            .replace("%id%", proyecto.getId()).replace("%coords%", coords);
        TagResolver tagResolver1 = PlaceholderUtil.getCopyableText("id", proyecto.getId(), proyecto.getId());
        TagResolver tagResolver2 = PlaceholderUtil.getCopyableText("coords", coords, coords);
        String mcNotification = lang.getString("reviewer-notification-finish-project").replace("%pais%", pais.getNombrePublico());
        DiscordLogger.notifyReviewers(mcNotification, dsNotification, pais, tagResolver1, tagResolver2);

        Set<Player> members = getMembers(proyecto);
        String mcMemberNotification = lang.getString("member-notification-finish-project").replace("%requester%", requester.getNombre()).replace("%proyectoId%", proyecto.getId());
        MessageEmbed dsMemberNotification = ChatUtil.getDsProjectFinishRequested(proyecto.getId(), proyecto.getNombre(), requester.getNombre());
        if (!requester.equals(lider)) PlayerLogger.info(lider, mcMemberNotification, dsMemberNotification);
        for (Player member : members) {
            if (member.equals(requester)) continue;
            PlayerLogger.info(member, mcMemberNotification, dsMemberNotification);
        }
    }

    public void cancelFinishRequest(Proyecto proyecto, Estado newEstado) {
        proyecto.setEstado(newEstado);
        ProyectoRegistry.getInstance().merge(proyecto.getId());
        InteractionRegistry interactionRegistry = InteractionRegistry.getInstance();
        Interaction interaction = interactionRegistry.findFinishRequest(proyecto.getId());
        interactionRegistry.unload(interaction.getId());
    }

    public void acceptFinishRequest(String proyectoId, Player staff, String comentario, Boolean promote) {
        Proyecto proyecto = ProyectoRegistry.getInstance().get(proyectoId);
        proyecto.setFechaTerminado(Date.from(Instant.now()));
        cancelFinishRequest(proyecto, Estado.COMPLETADO);
        String message = lang.getString("project-finish-accepted").replace("%proyectoId%", proyectoId);
        String commentMessage = comentario != null ? lang.getString("project-finish-comment-accepted").replace("%comentario%", comentario) : null;
        MessageEmbed dsMessage = ChatUtil.getDsProjectFinishAccepted(proyecto.getId(), comentario, proyecto.getNombre());

        TipoUsuarioRegistry tur = TipoUsuarioRegistry.getInstance();
        TipoUsuario constructor = tur.getConstructor();
        Pais pais = proyecto.getPais();
        String tipoSwitchedMessage = lang.getString("tipo-switched").replace("%tipo%", constructor.getNombre());
        MessageEmbed dsTipoSwitchedMessage = ChatUtil.getDsTipoUsuarioSwitched(constructor);
        String tipoPromoteLog = lang.getString("tipo-promote-log").replace("%tipo%", constructor.getNombre());

        String countryLog = lang.getString("project-finish-accepted-log").replace("%staff%", staff.getNombre()).replace("%proyectoId%", proyecto.getId());
        DiscordLogger.countryLog(countryLog, pais);

        Player lider = getLider(proyecto);
        Set<Player> members = getMembers(proyecto);
        
        PlayerLogger.info(lider, message, dsMessage);
        if (comentario != null && !comentario.isBlank()) PlayerLogger.info(lider, commentMessage, (String) null);
        if (promote) {
            PermissionManager.getInstance().switchTipoUsuario(lider, constructor);
            PlayerLogger.info(lider, tipoSwitchedMessage, dsTipoSwitchedMessage);
            DiscordLogger.countryLog(tipoPromoteLog.replace("%player%", lider.getNombre()), pais);
        }
        for (Player member : members) {
            PlayerLogger.info(member, message, dsMessage);
            if (comentario != null && !comentario.isBlank()) PlayerLogger.info(member, commentMessage, (String) null);
        }
    }

    public void rejectFinishRequest(String proyectoId, Player staff, String comentario) {
        Proyecto proyecto = ProyectoRegistry.getInstance().get(proyectoId);
        cancelFinishRequest(proyecto, Estado.ACTIVO);

        String message = lang.getString("project-finish-rejected").replace("%proyectoId%", proyectoId);
        String commentMessage = comentario != null ? lang.getString("project-finish-comment-rejected").replace("%comentario%", comentario) : null;
        MessageEmbed dsMessage = ChatUtil.getDsProjectFinishRejected(proyecto.getId(), comentario, proyecto.getNombre());
        Player lider = getLider(proyecto);
        Set<Player> members = getMembers(proyecto);
        PlayerLogger.info(lider, message, dsMessage);
        if (comentario != null && !comentario.isBlank()) PlayerLogger.info(lider, commentMessage, (String) null);
        for (Player member : members) {
            PlayerLogger.info(member, message, dsMessage);
            if (comentario != null && !comentario.isBlank()) PlayerLogger.info(member, commentMessage, (String) null);
        }

        Pais pais = proyecto.getPais();
        String countryLog = lang.getString("project-finish-rejected-log").replace("%staff%", staff.getNombre()).replace("%proyectoId%", proyecto.getId());
        DiscordLogger.countryLog(countryLog, pais);
    }

    public void expiredFinishRequest(String proyectoId) {
        Proyecto proyecto = ProyectoRegistry.getInstance().get(proyectoId);
        cancelFinishRequest(proyecto, Estado.ACTIVO);

        Player lider = getLider(proyecto);
        Set<Player> members = getMembers(proyecto);
        members.add(lider);
        String message = lang.getString("project-finish-request-expired").replace("%proyectoId%", proyectoId);
        MessageEmbed dsMessage = ChatUtil.getDsProjectFinishRequestExpired(proyectoId, proyecto.getNombre());
        for (Player member : members) PlayerLogger.info(member, message, dsMessage);
        
        Pais pais = proyecto.getPais();
        String countryLog = lang.getString("project-finish-request-expired-log").replace("%proyectoId%", proyecto.getId());
        DiscordLogger.countryLog(countryLog, pais);
    }

    public void switchLeader(String proyectoId, UUID newLiderId, UUID commandId) {
        PlayerRegistry playerRegistry = PlayerRegistry.getInstance();
        Proyecto proyecto = ProyectoRegistry.getInstance().get(proyectoId);
        Player newLider = playerRegistry.get(newLiderId);
        Player oldLider = getLider(proyecto);
        Player commandPlayer = playerRegistry.get(commandId);
        if (oldLider != null) proyecto.addMiembro(oldLider);
        if (oldLider == null) proyecto.setEstado(Estado.ACTIVO);
        proyecto.removeMiembro(newLider);
        proyecto.setLider(newLider);
        ProyectoRegistry.getInstance().merge(proyecto.getId());
        Pais pais = proyecto.getPais();
        String newLiderNotification = lang.getString("project-leader-switched").replace("%proyectoId%", proyecto.getId());
        if (!commandPlayer.equals(oldLider) ) {
            String leaderNotification = lang.getString("project-leader-switched-leader").replace("%proyectoId%", proyecto.getId()).replace("%player%", newLider.getNombre());
            PlayerLogger.info(oldLider, leaderNotification, ChatUtil.getDsLeaderSwitchedLeader(proyecto.getId(), proyecto.getNombre(), newLider.getNombre()));
            String countryLog;
            if (oldLider == null) countryLog = lang.getString("project-leader-switch-no-old-leader-log").replace("%newLider%", newLider.getNombre()).replace("%proyectoId%", proyecto.getId()).replace("%staff%", commandPlayer.getNombre());
            else countryLog = lang.getString("project-leader-switch-staff-log").replace("%oldLider%", oldLider.getNombre()).replace("%newLider%", newLider.getNombre()).replace("%proyectoId%", proyecto.getId()).replace("%staff%", commandPlayer.getNombre());
            DiscordLogger.countryLog(countryLog, pais);
            PlayerLogger.info(newLider, newLiderNotification, ChatUtil.getDsLeaderSwitched(proyecto.getId(), proyecto.getNombre()));
            return;
        } else {
            if (oldLider == null) return;
            String countryLog = lang.getString("project-leader-switch-log").replace("%oldLider%", oldLider.getNombre()).replace("%newLider%", newLider.getNombre()).replace("%proyectoId%", proyecto.getId());
            DiscordLogger.countryLog(countryLog, pais);
        }
        String memberNotification = lang.getString("project-leader-switched-member").replace("%proyectoId%", proyecto.getId()).replace("%player%", newLider.getNombre());
        Set<Player> members = getMembers(proyecto);
        for (Player member : members) {
            if (!member.equals(oldLider)) {
                PlayerLogger.info(member, memberNotification, ChatUtil.getDsLeaderSwitchedMember(proyecto.getId(), proyecto.getNombre(), newLider.getNombre()));
            }
        }
        PlayerLogger.info(newLider, newLiderNotification, ChatUtil.getDsLeaderSwitched(proyecto.getId(), proyecto.getNombre()));
    }

    public boolean createRedefineRequest(String proyectoId, Polygon newPolygon, UUID commandUuid, TipoProyecto tipoProyecto, Division division) {
        ProyectoRegistry pr = ProyectoRegistry.getInstance();
        Proyecto proyecto = pr.get(proyectoId);
        Player commandPlayer = PlayerRegistry.getInstance().get(commandUuid);
        Player lider = getLider(proyecto);
        File contextImage = SatMapUtils.downloadRedefineContext(proyecto.getId(), proyecto.getPoligono(), newPolygon,
            pr.getOverlapping(proyectoId, newPolygon).stream().map(Proyecto::getPoligono).collect(Collectors.toSet())
        );

        if (contextImage == null) return false;
        
        Pais pais = proyecto.getPais();
        Boolean success = ProjectRequestService.sendProjectRedefineRequest(proyecto, newPolygon, tipoProyecto.getId(), division.getId(), contextImage, commandPlayer.getNombre());
        if (!success) return false;
        
        String countryLog = lang.getString("project-redefine-request-log")
            .replace("%lider%", commandPlayer.getNombre())
            .replace("%proyectoId%", proyecto.getId())
            .replace("%tipoId%", tipoProyecto.getId().toString())
            .replace("%tipoNombre%", tipoProyecto.getNombre())
            .replace("%divisionId%", division.getId().toString())
            .replace("%divisionNombre%", division.getNombre());
        DiscordLogger.countryLog(countryLog, pais);
        
        String memberNotification = lang.getString("project-redefine-request-member").replace("%proyectoId%", proyecto.getId()).replace("%player%", commandPlayer.getNombre());
        MessageEmbed dsMemberNotification = ChatUtil.getDsProjectRedefineRequestedMember(proyecto.getId(), proyecto.getNombre(), commandPlayer.getNombre());
        Set<Player> members = getMembers(proyecto);
        if (!commandPlayer.equals(lider)) members.add(lider);
        for (Player member : members) {
            PlayerLogger.info(member, memberNotification, dsMemberNotification);
        }
        return true;
    }

    public void cancelRedefineRequest(Proyecto proyecto, Estado newEstado) {
        proyecto.setEstado(newEstado);
        ProyectoRegistry.getInstance().merge(proyecto.getId());
        InteractionRegistry interactionRegistry = InteractionRegistry.getInstance();
        Interaction interaction = interactionRegistry.findRedefineRequest(proyecto.getId());
        interactionRegistry.unload(interaction.getId());
        SatMapUtils.deleteRedefineImage(proyecto);
    }

    public void expiredRedefineRequest(String proyectoId, Long interactionId) {
        Proyecto proyecto = ProyectoRegistry.getInstance().get(proyectoId);
        Interaction interaction = InteractionRegistry.getInstance().get(interactionId);
        Estado previousEstado = Estado.valueOf((String) interaction.getPayloadValue("previousEstado"));
        cancelRedefineRequest(proyecto, previousEstado);
        String message = lang.getString("project-redefine-request-expired").replace("%proyectoId%", proyectoId);
        MessageEmbed dsMessage = ChatUtil.getDsProjectRedefineExpired(proyecto.getId(), proyecto.getNombre());
        Player lider = getLider(proyecto);
        Set<Player> members = getMembers(proyecto);
        members.add(lider);
        for (Player member : members) PlayerLogger.info(member, message, dsMessage);
        Pais pais = proyecto.getPais();
        String countryLog = lang.getString("project-redefine-request-expired-log").replace("%proyectoId%", proyecto.getId());
        DiscordLogger.countryLog(countryLog, pais);
    }

    public void acceptRedefineRequest(String proyectoId, Player staff, Long interactionId, String comentario) {
        Proyecto proyecto = ProyectoRegistry.getInstance().get(proyectoId);
        InteractionRegistry ir = InteractionRegistry.getInstance();
        Interaction interaction = ir.get(interactionId);
        Estado previousEstado = Estado.valueOf((String) interaction.getPayloadValue("previousEstado"));
        Long tipoId = ((Number) interaction.getPayloadValue("tipoId")).longValue();
        Long divisionId = ((Number) interaction.getPayloadValue("divisionId")).longValue();
        TipoProyecto tipoProyecto = TipoProyectoRegistry.getInstance().get(tipoId);
        Division division = PaisRegistry.getInstance().findDivisionById(divisionId);
        proyecto.setEstado(previousEstado);
        proyecto.setPoligono(interaction.getPoligono());
        proyecto.updateTamaño();
        proyecto.setTipoProyecto(tipoProyecto);
        proyecto.setDivision(division);
        ProyectoRegistry.getInstance().merge(proyecto.getId());
        ir.unload(interactionId);
        SatMapUtils.switchRedefineImage(proyecto);
        Player lider = getLider(proyecto);
        Set<Player> members = getMembers(proyecto);
        members.add(lider);
        Pais pais = proyecto.getPais();

        String countryLog = lang.getString("project-redefine-accepted-log").replace("%staff%", staff.getNombre()).replace("%lider%", lider.getNombre()).replace("%proyectoId%", proyecto.getId());
        DiscordLogger.countryLog(countryLog, pais);

        String message = lang.getString("project-redefine-request-accepted").replace("%proyectoId%", proyecto.getId());
        String commentMessage = lang.getString("project-redefine-request-comment-accepted").replace("%comentario%", comentario);
        MessageEmbed dsMessage = ChatUtil.getDsProjectRedefineAccepted(proyecto.getId(), comentario, proyecto.getNombre());
        for (Player member : members) {
            PlayerLogger.info(member, message, dsMessage);
            if (comentario != null && !comentario.isBlank()) {
                PlayerLogger.info(member, commentMessage, (String) null);
            }
        }
    }

    public void rejectRedefineRequest(String proyectoId, Player staff, Long interactionId, String comentario) {
        Proyecto proyecto = ProyectoRegistry.getInstance().get(proyectoId);
        InteractionRegistry ir = InteractionRegistry.getInstance();
        Interaction interaction = ir.get(interactionId);
        Estado previousEstado = Estado.valueOf((String) interaction.getPayloadValue("previousEstado"));
        cancelRedefineRequest(proyecto, previousEstado);
        Player lider = getLider(proyecto);
        Set<Player> members = getMembers(proyecto);
        members.add(lider);
        Pais pais = proyecto.getPais();

        String countryLog = lang.getString("project-redefine-rejected-log").replace("%staff%", staff.getNombre()).replace("%lider%", lider.getNombre()).replace("%proyectoId%", proyecto.getId());
        DiscordLogger.countryLog(countryLog, pais);

        String message = lang.getString("project-redefine-request-rejected").replace("%proyectoId%", proyecto.getId());
        String commentMessage = lang.getString("project-redefine-request-comment-rejected").replace("%comentario%", comentario);
        MessageEmbed dsMessage = ChatUtil.getDsProjectRedefineRejected(proyecto.getId(), comentario, proyecto.getNombre());
        for (Player member : members) {
            PlayerLogger.info(member, message, dsMessage);
            if (comentario != null && !comentario.isBlank()) {
                PlayerLogger.info(member, commentMessage, (String) null);
            }
        }
    }

    public void activateEdit(String proyectoId, UUID comanUuid) {
        ProyectoRegistry pr = ProyectoRegistry.getInstance();
        Proyecto proyecto = pr.get(proyectoId);
        Player commandPlayer = PlayerRegistry.getInstance().get(comanUuid);
        Player lider = getLider(proyecto);
        proyecto.setEstado(Estado.EDITANDO);
        ProyectoRegistry.getInstance().merge(proyecto.getId());

        String message = lang.getString("project-edit-active-member").replace("%proyectoId%", proyecto.getId()).replace("%player%", commandPlayer.getNombre());
        MessageEmbed dsMessage = ChatUtil.getDsProjectEditActiveMember(proyecto.getId(), proyecto.getNombre(), commandPlayer.getNombre());
        Set<Player> members = getMembers(proyecto);
        if (!commandPlayer.equals(lider)) members.add(lider);
        for (Player member : members) {
            PlayerLogger.info(member, message, dsMessage);
        }
        Pais pais = proyecto.getPais();
        String countryLog = lang.getString("project-edit-active-log").replace("%lider%", commandPlayer.getNombre()).replace("%proyectoId%", proyecto.getId());
        DiscordLogger.countryLog(countryLog, pais);
    }

    public void createFinishEditRequest(String proyectoId, UUID commandUuid) {
        Proyecto proyecto = ProyectoRegistry.getInstance().get(proyectoId);
        Player commandPlayer = PlayerRegistry.getInstance().get(commandUuid);
        Player lider = getLider(proyecto);
        
        proyecto.setEstado(Estado.EN_FINALIZACION_EDICION);
        ProyectoRegistry.getInstance().merge(proyecto.getId());
        Interaction interaction = new Interaction(
            commandPlayer.getUuid(),
            proyecto.getId(),
            InteractionKey.FINISH_EDIT_PROJECT,
            Instant.now(),
            Instant.now().plusSeconds(config.getLong("interaction-expirations.finish-edit-project") * 60)
        );
        InteractionRegistry.getInstance().load(interaction);
    
        Point centroid = proyecto.getPoligono().getCentroid();
        double[] geoCoords = TerraUtils.toGeo(centroid.getX(), centroid.getY());
        String coords = geoCoords[1] + ", " + geoCoords[0];
        Pais pais = proyecto.getPais();

        String countryLog = lang.getString("project-finish-edit-request-log").replace("%lider%", commandPlayer.getNombre()).replace("%proyectoId%", proyecto.getId());
        DiscordLogger.countryLog(countryLog, pais);
        
        String dsNotification = lang.getString("ds-reviewer-notification-finish-edit-project").replace("%pais%", pais.getNombrePublico())
            .replace("%id%", proyecto.getId()).replace("%coords%", coords);
        TagResolver tagResolver1 = PlaceholderUtil.getCopyableText("id", proyecto.getId(), proyecto.getId());
        TagResolver tagResolver2 = PlaceholderUtil.getCopyableText("coords", coords, coords);
        String mcNotification = lang.getString("reviewer-notification-finish-edit-project").replace("%pais%", pais.getNombrePublico());
        DiscordLogger.notifyReviewers(mcNotification, dsNotification, pais, tagResolver1, tagResolver2);

        Set<Player> members = getMembers(proyecto);
        String mcMemberNotification = lang.getString("project-finish-edit-request-member").replace("%player%", commandPlayer.getNombre()).replace("%proyectoId%", proyecto.getId());
        MessageEmbed dsMemberNotification = ChatUtil.getDsProjectFinishEditRequested(proyecto.getId(), proyecto.getNombre(), commandPlayer.getNombre());
        if (!commandPlayer.equals(lider)) members.add(lider);
        for (Player member : members) {
            PlayerLogger.info(member, mcMemberNotification, dsMemberNotification);
        }
    }

    public void cancelFinishEditRequest(Proyecto proyecto, Estado newEstado) {
        proyecto.setEstado(newEstado);
        ProyectoRegistry.getInstance().merge(proyecto.getId());
        InteractionRegistry interactionRegistry = InteractionRegistry.getInstance();
        Interaction interaction = interactionRegistry.findFinishEditRequest(proyecto.getId());
        interactionRegistry.unload(interaction.getId());
    }

    public void expiredFinishEditRequest(String proyectoId) {
        Proyecto proyecto = ProyectoRegistry.getInstance().get(proyectoId);
        cancelFinishEditRequest(proyecto, Estado.COMPLETADO);

        Player lider = getLider(proyecto);
        Set<Player> members = getMembers(proyecto);
        members.add(lider);
        String message = lang.getString("project-finish-edit-request-expired").replace("%proyectoId%", proyectoId);
        MessageEmbed dsMessage = ChatUtil.getDsProjectFinishEditRequestExpired(proyectoId, proyecto.getNombre());
        for (Player member : members) PlayerLogger.info(member, message, dsMessage);
        
        Pais pais = proyecto.getPais();
        String countryLog = lang.getString("project-finish-edit-request-expired-log").replace("%proyectoId%", proyecto.getId());
        DiscordLogger.countryLog(countryLog, pais);
    }

    public void acceptEditRequest(String proyectoId, Player staff, String comentario) {
        Proyecto proyecto = ProyectoRegistry.getInstance().get(proyectoId);
        cancelFinishEditRequest(proyecto, Estado.COMPLETADO);
        String message = lang.getString("project-finish-edit-accepted").replace("%proyectoId%", proyectoId);
        String commentMessage = comentario != null ? lang.getString("project-finish-edit-comment-accepted").replace("%comentario%", comentario) : null;
        MessageEmbed dsMessage = ChatUtil.getDsProjectFinishEditAccepted(proyecto.getId(), comentario, proyecto.getNombre());

        Pais pais = proyecto.getPais();

        String countryLog = lang.getString("project-finish-edit-accepted-log").replace("%staff%", staff.getNombre()).replace("%proyectoId%", proyecto.getId());
        DiscordLogger.countryLog(countryLog, pais);

        Player lider = getLider(proyecto);
        Set<Player> members = getMembers(proyecto);
        members.add(lider);
        
        for (Player member : members) {
            PlayerLogger.info(member, message, dsMessage);
            if (comentario != null && !comentario.isBlank()) PlayerLogger.info(member, commentMessage, (String) null);
        }
    }

    public void rejectedEditRequest(String proyectoId, Player staff, String comentario) {
        Proyecto proyecto = ProyectoRegistry.getInstance().get(proyectoId);
        cancelFinishEditRequest(proyecto, Estado.EDITANDO);

        String message = lang.getString("project-finish-edit-rejected").replace("%proyectoId%", proyectoId);
        String commentMessage = comentario != null ? lang.getString("project-finish-edit-comment-rejected").replace("%comentario%", comentario) : null;
        MessageEmbed dsMessage = ChatUtil.getDsProjectFinishEditRejected(proyecto.getId(), comentario, proyecto.getNombre());
        Player lider = getLider(proyecto);
        Set<Player> members = getMembers(proyecto);
        members.add(lider);
        for (Player member : members) {
            PlayerLogger.info(member, message, dsMessage);
            if (comentario != null && !comentario.isBlank()) PlayerLogger.info(member, commentMessage, (String) null);
        }

        Pais pais = proyecto.getPais();
        String countryLog = lang.getString("project-finish-edit-rejected-log").replace("%staff%", staff.getNombre()).replace("%proyectoId%", proyecto.getId());
        DiscordLogger.countryLog(countryLog, pais);
    }

    public void claim(String proyectoId, UUID playerId) {
        Proyecto proyecto = ProyectoRegistry.getInstance().get(proyectoId);
        Player player = PlayerRegistry.getInstance().get(playerId);
        proyecto.setLider(player);
        proyecto.setEstado(Estado.ACTIVO);
        ProyectoRegistry.getInstance().merge(proyecto.getId());
        Pais pais = proyecto.getPais();
        String countryLogTemplate = lang.getString("project-claimed-log");
        if (countryLogTemplate != null) {
            String countryLog = countryLogTemplate.replace("%player%", player.getNombre()).replace("%proyectoId%", proyecto.getId());
            DiscordLogger.countryLog(countryLog, pais);
        }
    }
    
    public void shutdown() {
        ConsoleLogger.info(lang.getString("project-manager-shutting-down"));
        if (instance != null) {
            instance = null;
        }
    }

    public static ProjectManager getInstance() {
        if (instance == null) {
            instance = new ProjectManager();
        }
        return instance;
    }
    
}