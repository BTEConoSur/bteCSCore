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
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.ConsoleLogger;
import com.bteconosur.core.util.DiscordLogger;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.core.util.SatMapUtils;
import com.bteconosur.core.util.TagResolverUtils;
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
import com.bteconosur.db.util.PlaceholderUtils;
import com.bteconosur.discord.util.ProjectRequestService;

import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

//TODO: vereficar casos edgde.
public class ProjectManager {

    private static ProjectManager instance;

    private final YamlConfiguration config;
    
    public ProjectManager() {
        ConfigHandler configHandler = ConfigHandler.getInstance();
        config = configHandler.getConfig();

        ConsoleLogger.info(LanguageHandler.getText("project-manager-initializing"));
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

    public void createProject(String nombre, String descripcion, Polygon regionPolygon, Player player, Language language) {
        Interaction previousctx = InteractionRegistry.getInstance().findCreateRequest(player);
        if (previousctx != null) {
            PlayerLogger.warn(player, LanguageHandler.getText(language, "project.create.request.already"), (String) null);
            return;
        }
        Double tamaño = regionPolygon.getArea();
        TipoProyecto tipoProyecto = TipoProyectoRegistry.getInstance().get(tamaño);
        if (tipoProyecto == null) {
            PlayerLogger.error(player, LanguageHandler.getText(language, "invalid-project-size"), (String) null);
            return;
        }

        PaisRegistry paisr = PaisRegistry.getInstance();
        Division division = paisr.findDivisionByPolygon(regionPolygon, paisr.findByPolygon(regionPolygon));
        if (division == null) {
            PlayerLogger.error(player, LanguageHandler.getText(language, "invalid-project-location"), (String) null);
            return;
        }

        ProyectoRegistry pr = ProyectoRegistry.getInstance(); 
        int activeProjects = pr.getCounts(player)[1];
        int maxActiveProjects = player.getTipoUsuario().getCantProyecSim();
        if (activeProjects >= maxActiveProjects) {
            String message = LanguageHandler.getText(language, "project.leader.max-active-projects").replace("%maxProjects%", String.valueOf(maxActiveProjects)).replace("%currentProjects%", String.valueOf(activeProjects));
            PlayerLogger.error(player, message, (String) null);
            return;
        }
        
        Proyecto proyecto = new Proyecto(nombre, descripcion, Estado.EN_CREACION, regionPolygon, tamaño, tipoProyecto, player, division, Date.from(Instant.now()));
        pr.load(proyecto);

        File contextImage = SatMapUtils.downloadContext(proyecto, pr.getOverlapping(proyecto.getId(), proyecto.getPoligono()));
        if (contextImage == null) {
            PlayerLogger.error(player, LanguageHandler.getText(language, "internal-error"), (String) null);
            pr.unload(proyecto.getId());
            return;
        }
        //SatMapUtils.downloadImage(proyecto, "Image");
        Pais pais = proyecto.getPais();
        Boolean success = ProjectRequestService.sendProjectRequest(proyecto, contextImage);
        if (!success){ 
            PlayerLogger.info(player, LanguageHandler.getText(language, "project.create.request.failed"), (String) null);
            pr.unload(proyecto.getId());
            return;
        }

        PlayerLogger.info(player, LanguageHandler.replaceMC("project.create.request.success", language, proyecto), (String) null);
        
        String countryLog = LanguageHandler.replaceDS("project.create.request.log", language, player, proyecto);
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

        String countryLog = LanguageHandler.replaceDS("project.create.accept.log", Language.getDefault(), staff, lider);
        countryLog = PlaceholderUtils.replaceDS(countryLog, Language.getDefault(), proyecto);
        DiscordLogger.countryLog(countryLog, pais);

        String message = LanguageHandler.replaceMC("project.create.accept.for-leader", lider.getLanguage(), proyecto);
        PlayerLogger.info(lider, message, ChatUtil.getDsProjectAccepted(proyecto, comentario, lider.getLanguage()));
        if (comentario != null && !comentario.isBlank()) {
            String commentMessage = LanguageHandler.getText(lider.getLanguage(), "project.create.accept.comment").replace("%comentario%", comentario);
            PlayerLogger.info(lider, commentMessage, (String) null);
        }

        if (tur.getVisita().equals(lider.getTipoUsuario())) {
            TipoUsuario postulante = tur.getPostulante();
            PermissionManager.getInstance().switchTipoUsuario(lider, postulante);
            PlayerLogger.info(lider, LanguageHandler.replaceMC("tipo.switch", lider.getLanguage(), postulante), ChatUtil.getDsTipoUsuarioSwitched(postulante, lider.getLanguage()));
            String countryLog2 = LanguageHandler.replaceDS("tipo.promote-log", Language.getDefault(), postulante);
            DiscordLogger.countryLog(PlaceholderUtils.replaceDS(countryLog2, Language.getDefault(), lider), pais);
        };
    }

    public void cancelCreateRequest(String proyectoId, Player staff, Long interactionId , String comentario) {
        Proyecto proyecto = ProyectoRegistry.getInstance().get(proyectoId);
        Pais pais = proyecto.getPais();
        Player lider = getLider(proyecto);

        InteractionRegistry.getInstance().unload(interactionId);
        String message = LanguageHandler.replaceMC("project.create.reject.for-leader", lider.getLanguage(), proyecto);
        PlayerLogger.info(lider, message, ChatUtil.getDsProjectRejected(proyecto, comentario, lider.getLanguage()));
        if (comentario != null && !comentario.isBlank()) {
            String commentMessage = LanguageHandler.getText(lider.getLanguage(), "project.create.reject.comment").replace("%comentario%", comentario);
            PlayerLogger.info(lider, commentMessage, (String) null);
        }

        String countryLog = LanguageHandler.replaceDS("project.create.reject.log", Language.getDefault(), staff, lider);
        countryLog = PlaceholderUtils.replaceDS(countryLog, Language.getDefault(), proyecto);
        DiscordLogger.countryLog(countryLog, pais);
        deleteProject(proyecto, null);
    }

    public void expiredCreateRequest(String proyectoId, Long interactionId) {
        Proyecto proyecto = ProyectoRegistry.getInstance().get(proyectoId);
        Pais pais = proyecto.getPais();
        Player lider = getLider(proyecto);

        InteractionRegistry.getInstance().unload(interactionId);
        String message = LanguageHandler.replaceMC("project.create.request.expired", lider.getLanguage(), proyecto);
        PlayerLogger.info(lider, message, ChatUtil.getDsProjectRequestExpired(proyecto, lider.getLanguage()));

        String countryLog = LanguageHandler.replaceDS("project.create.request.expired-log", Language.getDefault(), lider, proyecto);
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
            for (Player member : members) PlayerLogger.info(member, LanguageHandler.replaceMC("project.delete.for-member", member.getLanguage(), proyecto),  ChatUtil.getDsProjectDeleted(proyecto, member.getLanguage()));
            String countryLog = LanguageHandler.replaceDS("project.delete.staff-log", Language.getDefault(), commandPlayer, proyecto);
            DiscordLogger.countryLog(countryLog, pais);
            return;
        }
        String countryLog = LanguageHandler.replaceDS("project.delete.log", Language.getDefault(), lider, proyecto);
        DiscordLogger.countryLog(countryLog, pais);
    }

    public void createJoinRequest(String proyectoId, UUID playerId) {
        Interaction previousctx = InteractionRegistry.getInstance().findJoinRequest(proyectoId, playerId);
        ProyectoRegistry pr = ProyectoRegistry.getInstance();
        Proyecto proyecto = pr.get(proyectoId);
        Player player = PlayerRegistry.getInstance().get(playerId);
        Pais pais = proyecto.getPais();
        if (previousctx != null) {
            PlayerLogger.warn(player, LanguageHandler.replaceMC("project.join.request.already", player.getLanguage(), proyecto), (String) null);
            return;
        }
        ProjectRequestService.sendProjectJoinRequest(proyecto, player);
        PlayerLogger.info(player, LanguageHandler.replaceMC("project.join.request.success", player.getLanguage(), proyecto), (String) null);
        String countryLog = LanguageHandler.replaceDS("project.join.request.log", Language.getDefault(), player, proyecto);
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
        String message = LanguageHandler.replaceMC("project.join.request.expired", player.getLanguage(), proyecto);
        PlayerLogger.info(player, message, ChatUtil.getDsMemberJoinRequestExpired(proyecto, player.getLanguage()));
        Player lider = getLider(proyecto);
        if (lider != null) {
            String messageLider = LanguageHandler.replaceMC("project.join.request.expired-lider", lider.getLanguage(), player, proyecto);
            PlayerLogger.info(lider, messageLider, ChatUtil.getDsMemberJoinRequestExpiredLider(proyecto, player, player.getLanguage()));
        }
        String countryLog = LanguageHandler.replaceDS("project.join.request.expired-log", Language.getDefault(), player, proyecto);
        DiscordLogger.countryLog(countryLog, pais);
    }

    public void acceptJoinRequest(String proyectoId, UUID playerId, Long interactionId, UUID commandId) {
        Proyecto proyecto = ProyectoRegistry.getInstance().get(proyectoId);
        cancelJoinRequest(proyectoId, playerId);
        Pais pais = proyecto.getPais();
        Player player = PlayerRegistry.getInstance().get(playerId);
        Player commandPlayer = PlayerRegistry.getInstance().get(commandId);
        String message = LanguageHandler.replaceMC("project.join.accept.for-member", player.getLanguage(), proyecto);
        if (!commandPlayer.equals(player)) {
            PlayerLogger.info(player, message, ChatUtil.getDsMemberJoinRequestAccepted(proyecto, player.getLanguage()));
        }
        String countryLog = LanguageHandler.replaceDS("project.join.accept.log", Language.getDefault(), commandPlayer, player);
        countryLog = PlaceholderUtils.replaceDS(countryLog, Language.getDefault(), proyecto);
        DiscordLogger.countryLog(countryLog, pais);
        joinProject(proyectoId, playerId, commandId, false);
    }

    public void rejectJoinRequest(String proyectoId, UUID playerId, Long interactionId, UUID commandId) {
        Proyecto proyecto = ProyectoRegistry.getInstance().get(proyectoId);
        cancelJoinRequest(proyectoId, playerId);
        Pais pais = proyecto.getPais();
        Player player = PlayerRegistry.getInstance().get(playerId);
        Player commandPlayer = PlayerRegistry.getInstance().get(commandId);
        String message = LanguageHandler.replaceMC("project.join.reject.for-member", player.getLanguage(), proyecto);
        if (!commandPlayer.equals(player)) {
            PlayerLogger.info(player, message, ChatUtil.getDsMemberJoinRequestRejected(proyecto, player.getLanguage()));
        }
        String countryLog = LanguageHandler.replaceDS("project.join.reject.log", Language.getDefault(), commandPlayer, player);
        countryLog = PlaceholderUtils.replaceDS(countryLog, Language.getDefault(), proyecto);
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
        String memberNotification = LanguageHandler.replaceMC("project.member.add.added", player.getLanguage(), proyecto);
        PlayerLogger.info(player, memberNotification, ChatUtil.getDsMemberAdded(proyecto, player.getLanguage()));

        Set<Player> members = getMembers(proyecto);
        if (!commandPlayer.equals(lider)) members.add(lider);
        for (Player member : members) {
            if (!member.equals(player)) {
                PlayerLogger.info(member, LanguageHandler.replaceMC("project.member.add.for-member", member.getLanguage(), proyecto), ChatUtil.getDsMemberAddedMember(proyecto, player, member.getLanguage()));
            }
        }
        if (isAdded) {
            String countryLog = LanguageHandler.replaceDS("project.member.add.log", Language.getDefault(), commandPlayer, player);
            countryLog = PlaceholderUtils.replaceDS(countryLog, Language.getDefault(), proyecto);
            DiscordLogger.countryLog(countryLog, pais);
            return;
        }
        String countryLog = LanguageHandler.replaceDS("project.join.log", Language.getDefault(), player, proyecto);
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
            String countryLog = LanguageHandler.replaceDS("project.leader.leave.log", Language.getDefault(), player, proyecto);
            DiscordLogger.countryLog(countryLog, pais);
        } else {
            proyecto.removeMiembro(player);
            pr.merge(proyecto.getId());
            Set<Player> members = getMembers(proyecto);
            members.add(lider);
            for (Player member : members) {
                if (!member.equals(player)) {
                    PlayerLogger.info(member, LanguageHandler.replaceMC("project.member.leave.for-member", member.getLanguage(), proyecto), ChatUtil.getDsMemberLeftMember(proyecto, player, member.getLanguage()));
                }
            }
            String countryLog = LanguageHandler.replaceDS("project.member.leave.log", Language.getDefault(), player, proyecto);
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
            String leaderNotification = LanguageHandler.replaceMC("project.leader.remove.for-leader", player.getLanguage(), proyecto);
            PlayerLogger.info(player, leaderNotification, ChatUtil.getDsLeaderRemoved(proyecto, player.getLanguage()));
            String countryLog = LanguageHandler.replaceDS("project.leader.remove.log", Language.getDefault(), commandPlayer, player);
            countryLog = PlaceholderUtils.replaceDS(countryLog, Language.getDefault(), proyecto);
            DiscordLogger.countryLog(countryLog, proyecto.getPais());
            return;
        }
        proyecto.removeMiembro(player);
        ProyectoRegistry.getInstance().merge(proyecto.getId());
        Pais pais = proyecto.getPais();
        String memberNotification = LanguageHandler.replaceMC("project.member.remove.removed", player.getLanguage(), proyecto);
        PlayerLogger.info(player, memberNotification, ChatUtil.getDsMemberRemoved(proyecto, player.getLanguage()));

        Set<Player> members = getMembers(proyecto);
        if (!commandPlayer.equals(lider)) members.add(lider);
        for (Player member : members) {
            if (!member.equals(player)) {
                PlayerLogger.info(member, LanguageHandler.replaceMC("project.member.remove.for-member", member.getLanguage(), player, proyecto), ChatUtil.getDsMemberRemovedMember(proyecto, player, member.getLanguage()));
            }
        }

        String countryLog = LanguageHandler.replaceDS("project.member.remove.log", Language.getDefault(), commandPlayer, player);
        countryLog = PlaceholderUtils.replaceDS(countryLog, Language.getDefault(), proyecto);
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

        String countryLog = LanguageHandler.replaceDS("project.finish.request.log", Language.getDefault(), requester, proyecto);
        DiscordLogger.countryLog(countryLog, pais);
        
        String dsNotification = LanguageHandler.replaceDS("project.finish.request.ds-for-reviewer", Language.getDefault(), proyecto);
        dsNotification = PlaceholderUtils.replaceDS(dsNotification, Language.getDefault(), pais);
        TagResolver tagResolver1 = TagResolverUtils.getCopyableText("id", proyecto.getId(), proyecto.getId(), Language.getDefault());
        TagResolver tagResolver2 = TagResolverUtils.getCopyableText("coords", coords, coords, Language.getDefault());
        String mcNotification = LanguageHandler.replaceMC("project.finish.request.for-reviewer", Language.getDefault(), pais);
        mcNotification = PlaceholderUtils.replaceMC(mcNotification, Language.getDefault(), proyecto);
        DiscordLogger.notifyReviewers(mcNotification, dsNotification, pais, tagResolver1, tagResolver2);

        Set<Player> members = getMembers(proyecto);
        if (!requester.equals(lider)) members.add(lider);
        for (Player member : members) {
            if (member.equals(requester)) continue;
            PlayerLogger.info(member, LanguageHandler.replaceMC("project.finish.request.for-member", member.getLanguage(), requester, proyecto), ChatUtil.getDsProjectFinishRequested(proyecto, requester, member.getLanguage()));
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

        TipoUsuarioRegistry tur = TipoUsuarioRegistry.getInstance();
        TipoUsuario constructor = tur.getConstructor();
        Pais pais = proyecto.getPais();
        Player lider = getLider(proyecto);
        String countryLog = LanguageHandler.replaceDS("project.finish.accept.log", Language.getDefault(), staff, lider);
        countryLog = PlaceholderUtils.replaceDS(countryLog, Language.getDefault(), proyecto);
        DiscordLogger.countryLog(countryLog, pais);

        Set<Player> members = getMembers(proyecto);
        if (!staff.equals(lider)) members.add(lider);
        for (Player member : members) {
            PlayerLogger.info(member, LanguageHandler.replaceMC("project.finish.accept.for-member", member.getLanguage(), proyecto), ChatUtil.getDsProjectFinishAccepted(proyecto, comentario, member.getLanguage()));
            if (comentario != null && !comentario.isBlank()) PlayerLogger.info(member, LanguageHandler.replaceMC("project.finish.accept.comment", member.getLanguage(), proyecto).replace("%comentario%", comentario), (String) null);
        }
        if (promote) {
            PermissionManager.getInstance().switchTipoUsuario(lider, constructor);
            PlayerLogger.info(lider, LanguageHandler.replaceMC("tipo.switch", lider.getLanguage(), constructor), ChatUtil.getDsTipoUsuarioSwitched(constructor, lider.getLanguage()));
            String promoteLog = LanguageHandler.replaceDS("tipo.promote-log", Language.getDefault(), constructor);
            promoteLog = PlaceholderUtils.replaceDS(promoteLog, Language.getDefault(), lider);
            DiscordLogger.countryLog(promoteLog, pais);
        }
    
    }

    public void rejectFinishRequest(String proyectoId, Player staff, String comentario) {
        Proyecto proyecto = ProyectoRegistry.getInstance().get(proyectoId);
        cancelFinishRequest(proyecto, Estado.ACTIVO);

        Player lider = getLider(proyecto);
        Set<Player> members = getMembers(proyecto);
        if (!staff.equals(lider)) members.add(lider);
        for (Player member : members) {
            PlayerLogger.info(member, LanguageHandler.replaceMC("project.finish.reject.for-member", member.getLanguage(), proyecto), ChatUtil.getDsProjectFinishRejected(proyecto, comentario, member.getLanguage()));
            if (comentario != null && !comentario.isBlank()) PlayerLogger.info(member, LanguageHandler.replaceMC("project.finish.reject.comment", member.getLanguage(), proyecto).replace("%comentario%", comentario), (String) null);
        }

        Pais pais = proyecto.getPais();
        String countryLog = LanguageHandler.replaceDS("project.finish.reject.log", Language.getDefault(), staff, lider);
        countryLog = PlaceholderUtils.replaceDS(countryLog, Language.getDefault(), proyecto);
        DiscordLogger.countryLog(countryLog, pais);
    }

    public void expiredFinishRequest(String proyectoId) {
        Proyecto proyecto = ProyectoRegistry.getInstance().get(proyectoId);
        cancelFinishRequest(proyecto, Estado.ACTIVO);

        Player lider = getLider(proyecto);
        Set<Player> members = getMembers(proyecto);
        members.add(lider);
        for (Player member : members) PlayerLogger.info(member, LanguageHandler.replaceMC("project.finish.request.expired", member.getLanguage(), proyecto), ChatUtil.getDsProjectFinishRequestExpired(proyecto, member.getLanguage()));
        
        Pais pais = proyecto.getPais();
        String countryLog = LanguageHandler.replaceDS("project.finish.request.expired-log", Language.getDefault(), proyecto);
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
        String newLiderNotification = LanguageHandler.replaceMC("project.leader.switch.for-new-leader", newLider.getLanguage(), proyecto);
        if (!commandPlayer.equals(oldLider) ) {
            String countryLog;
            if (oldLider == null) {
                countryLog = LanguageHandler.replaceDS("project.leader.switch.staff-no-old-leader-log", Language.getDefault(), commandPlayer, newLider);
                countryLog = PlaceholderUtils.replaceDS(countryLog, Language.getDefault(), proyecto);
            } else {
                String leaderNotification = LanguageHandler.replaceMC("project.leader.switch.for-old-leader", oldLider.getLanguage(), newLider, proyecto);
                PlayerLogger.info(oldLider, leaderNotification, ChatUtil.getDsLeaderSwitchedLeader(proyecto, newLider, oldLider.getLanguage()));
                countryLog = LanguageHandler.replaceDS("project.leader.switch.staff-log", Language.getDefault(), commandPlayer, oldLider, newLider);
                countryLog = PlaceholderUtils.replaceDS(countryLog, Language.getDefault(), proyecto);
            }
            DiscordLogger.countryLog(countryLog, pais);
            PlayerLogger.info(newLider, newLiderNotification, ChatUtil.getDsLeaderSwitched(proyecto, newLider.getLanguage()));
            return;
        } else {
            if (oldLider == null) return;
            String countryLog = LanguageHandler.replaceDS("project.leader.switch.log", Language.getDefault(), oldLider, newLider);
            countryLog = PlaceholderUtils.replaceDS(countryLog, Language.getDefault(), proyecto);
            DiscordLogger.countryLog(countryLog, pais);
        }
        Set<Player> members = getMembers(proyecto);
        for (Player member : members) {
            if (!member.equals(oldLider)) {
                PlayerLogger.info(member, LanguageHandler.replaceMC("project.leader.switch.for-member", member.getLanguage(), newLider, proyecto), ChatUtil.getDsLeaderSwitchedMember(proyecto, newLider, member.getLanguage()));
            }
        }
        PlayerLogger.info(newLider, newLiderNotification, ChatUtil.getDsLeaderSwitched(proyecto, newLider.getLanguage()));
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
        Boolean success = ProjectRequestService.sendProjectRedefineRequest(proyecto, newPolygon, tipoProyecto.getId(), division.getId(), contextImage, commandPlayer);
        if (!success) return false;
        
        String countryLog = LanguageHandler.replaceDS("project.redefine.request.log", Language.getDefault(), commandPlayer, proyecto);
        DiscordLogger.countryLog(countryLog, pais);
        
        Set<Player> members = getMembers(proyecto);
        if (!commandPlayer.equals(lider)) members.add(lider);
        for (Player member : members) {
            PlayerLogger.info(member, LanguageHandler.replaceMC("project.redefine.request.for-member", member.getLanguage(), commandPlayer, proyecto), ChatUtil.getDsProjectRedefineRequestedMember(proyecto, commandPlayer, member.getLanguage()));
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
        Player lider = getLider(proyecto);
        Set<Player> members = getMembers(proyecto);
        members.add(lider);
        for (Player member : members) PlayerLogger.info(member, LanguageHandler.replaceMC("project.redefine.request.expired", member.getLanguage(), proyecto), ChatUtil.getDsProjectRedefineExpired(proyecto, member.getLanguage()));
        Pais pais = proyecto.getPais();
        String countryLog = LanguageHandler.replaceDS("project.redefine.request.expired-log", Language.getDefault(), proyecto);
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
        if (!staff.equals(lider)) members.add(lider);
        Pais pais = proyecto.getPais();

        String countryLog = LanguageHandler.replaceDS("project.redefine.accept.log", Language.getDefault(), staff, lider);
        countryLog = PlaceholderUtils.replaceDS(countryLog, Language.getDefault(), proyecto);
        DiscordLogger.countryLog(countryLog, pais);

        for (Player member : members) {
            PlayerLogger.info(member, LanguageHandler.replaceMC("project.redefine.accept.for-member", member.getLanguage(), proyecto), ChatUtil.getDsProjectRedefineAccepted(proyecto, comentario, member.getLanguage()));
            if (comentario != null && !comentario.isBlank()) {
                PlayerLogger.info(member, LanguageHandler.getText(member.getLanguage(), "project.redefine.accept.comment").replace("%comentario%", comentario), (String) null);
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
        if (!staff.equals(lider)) members.add(lider);
        Pais pais = proyecto.getPais();

        String countryLog = LanguageHandler.replaceDS("project.redefine.reject.log", Language.getDefault(), staff, lider);
        countryLog = PlaceholderUtils.replaceDS(countryLog, Language.getDefault(), proyecto);
        DiscordLogger.countryLog(countryLog, pais);

        for (Player member : members) {
            PlayerLogger.info(member, LanguageHandler.replaceMC("project.redefine.reject.for-member", member.getLanguage(), proyecto), ChatUtil.getDsProjectRedefineRejected(proyecto, comentario, member.getLanguage()));
            if (comentario != null && !comentario.isBlank()) {
                PlayerLogger.info(member, LanguageHandler.getText(member.getLanguage(), "project.redefine.reject.comment").replace("%comentario%", comentario), (String) null);
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

        Set<Player> members = getMembers(proyecto);
        if (!commandPlayer.equals(lider)) members.add(lider);
        for (Player member : members) {
            PlayerLogger.info(member, LanguageHandler.replaceMC("project.edit.activate.for-member", member.getLanguage(), commandPlayer, proyecto), ChatUtil.getDsProjectEditActiveMember(proyecto, commandPlayer, member.getLanguage()));
        }
        Pais pais = proyecto.getPais();
        String countryLog = LanguageHandler.replaceDS("project.edit.activate.log", Language.getDefault(), commandPlayer, proyecto);
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

        String countryLog = LanguageHandler.replaceDS("project.edit.finish.request.log", Language.getDefault(), commandPlayer, proyecto);
        DiscordLogger.countryLog(countryLog, pais);
        
        String dsNotification = LanguageHandler.replaceDS("project.edit.finish.request.ds-for-reviewer", Language.getDefault(), proyecto);
        dsNotification = PlaceholderUtils.replaceDS(dsNotification, Language.getDefault(), pais);
        TagResolver tagResolver1 = TagResolverUtils.getCopyableText("id", proyecto.getId(), proyecto.getId(), Language.getDefault());
        TagResolver tagResolver2 = TagResolverUtils.getCopyableText("coords", coords, coords, Language.getDefault());
        String mcNotification = LanguageHandler.replaceMC("project.edit.finish.request.for-reviewer", Language.getDefault(), proyecto);
        mcNotification = PlaceholderUtils.replaceMC(mcNotification, Language.getDefault(), pais);
        DiscordLogger.notifyReviewers(mcNotification, dsNotification, pais, tagResolver1, tagResolver2);

        Set<Player> members = getMembers(proyecto);
        if (!commandPlayer.equals(lider)) members.add(lider);
        for (Player member : members) {
            PlayerLogger.info(member, LanguageHandler.replaceMC("project.edit.finish.request.for-member", member.getLanguage(), commandPlayer, proyecto), ChatUtil.getDsProjectFinishEditRequested(proyecto, commandPlayer, member.getLanguage()));
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
        cancelFinishEditRequest(proyecto, Estado.EDITANDO);

        Player lider = getLider(proyecto);
        Set<Player> members = getMembers(proyecto);
        members.add(lider);
        for (Player member : members) PlayerLogger.info(member, LanguageHandler.replaceMC("project.edit.finish.request.expired", member.getLanguage(), proyecto), ChatUtil.getDsProjectFinishEditRequestExpired(proyecto, member.getLanguage()));
        
        Pais pais = proyecto.getPais();
        String countryLog = LanguageHandler.replaceDS("project.edit.finish.request.expired-log", Language.getDefault(), proyecto);
        DiscordLogger.countryLog(countryLog, pais);
    }

    public void acceptEditRequest(String proyectoId, Player staff, String comentario) {
        Proyecto proyecto = ProyectoRegistry.getInstance().get(proyectoId);
        cancelFinishEditRequest(proyecto, Estado.COMPLETADO);

        Pais pais = proyecto.getPais();

        String countryLog = LanguageHandler.replaceDS("project.edit.finish.accept.log", Language.getDefault(), staff, proyecto);
        DiscordLogger.countryLog(countryLog, pais);

        Player lider = getLider(proyecto);
        Set<Player> members = getMembers(proyecto);
        if (!staff.equals(lider)) members.add(lider);
        
        for (Player member : members) {
            PlayerLogger.info(member, LanguageHandler.replaceMC("project.edit.finish.accept.for-member", member.getLanguage(), proyecto), ChatUtil.getDsProjectFinishEditAccepted(proyecto, comentario, member.getLanguage()));
            if (comentario != null && !comentario.isBlank()) PlayerLogger.info(member, LanguageHandler.getText(member.getLanguage(), "project.edit.finish.accept.comment").replace("%comentario%", comentario), (String) null);
        }
    }

    public void rejectedEditRequest(String proyectoId, Player staff, String comentario) {
        Proyecto proyecto = ProyectoRegistry.getInstance().get(proyectoId);
        cancelFinishEditRequest(proyecto, Estado.EDITANDO);

        Pais pais = proyecto.getPais();
        String countryLog = LanguageHandler.replaceDS("project.edit.finish.reject.log", Language.getDefault(), staff, proyecto);
        DiscordLogger.countryLog(countryLog, pais);

        Player lider = getLider(proyecto);
        Set<Player> members = getMembers(proyecto);
        if (!staff.equals(lider)) members.add(lider);
        for (Player member : members) {
            PlayerLogger.info(member, LanguageHandler.replaceMC("project.edit.finish.reject.for-member", member.getLanguage(), proyecto), ChatUtil.getDsProjectFinishEditRejected(proyecto, comentario, member.getLanguage()));
            if (comentario != null && !comentario.isBlank()) PlayerLogger.info(member, LanguageHandler.getText(member.getLanguage(), "project.edit.finish.reject.comment").replace("%comentario%", comentario), (String) null);
        }
    }

    public void claim(String proyectoId, UUID playerId) {
        Proyecto proyecto = ProyectoRegistry.getInstance().get(proyectoId);
        Player player = PlayerRegistry.getInstance().get(playerId);
        proyecto.setLider(player);
        proyecto.setEstado(Estado.ACTIVO);
        ProyectoRegistry.getInstance().merge(proyecto.getId());
        Pais pais = proyecto.getPais();
        DiscordLogger.countryLog(LanguageHandler.replaceMC("project.claim.log", Language.getDefault(), player, proyecto), pais);
    }
    
    public void shutdown() {
        ConsoleLogger.info(LanguageHandler.getText("project-manager-shutting-down"));
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