package com.bteconosur.core;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.util.ConsoleLogger;
import com.bteconosur.core.util.DiscordLogger;
import com.bteconosur.db.model.Interaction;
import com.bteconosur.db.model.Pais;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.Proyecto;
import com.bteconosur.db.registry.InteractionRegistry;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.db.registry.ProyectoRegistry;
import com.bteconosur.db.util.Estado;
import com.bteconosur.db.util.InteractionKey;

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

    public void createProject(Proyecto proyecto) {
        ProyectoRegistry.getInstance().load(proyecto);
        Pais pais = proyecto.getPais();
        String countryLog = lang.getString("project-create-log")
            .replace("%lider%", getLider(proyecto).getNombre()).replace("%proyectoId%", proyecto.getId())
            .replace("%tipoId%", proyecto.getTipoProyecto().getId().toString()).replace("%tipoNombre%", proyecto.getTipoProyecto().getNombre())
            .replace("%ciudadId%", proyecto.getCiudad().getId().toString()).replace("%ciudadNombre%", proyecto.getCiudad().getNombre());
        DiscordLogger.countryLog(countryLog, pais);
        DiscordLogger.staffLog(countryLog, pais);
    }

    public void deleteProject(Proyecto proyecto) {
        ProyectoRegistry.getInstance().unload(proyecto.getId());
        Pais pais = proyecto.getPais();
        String countryLog = lang.getString("project-delete-log").replace("%lider%", getLider(proyecto).getNombre()).replace("%proyectoId%", proyecto.getId());
        DiscordLogger.countryLog(countryLog, pais);
        DiscordLogger.staffLog(countryLog, pais);
    }

    public void createJoinRequest(Proyecto proyecto, Player player) {
        Interaction interaction = new Interaction(
            player.getUuid(),
            proyecto.getId(),
            InteractionKey.JOIN_PROJECT,
            Instant.now(),
            Instant.now().plusSeconds(config.getLong("interaction-expirations.join-project") * 60)
        );
        InteractionRegistry.getInstance().load(interaction);
        Pais pais = proyecto.getPais();
        String countryLog = lang.getString("project-join-request-log").replace("%player%", player.getNombre()).replace("%proyectoId%", proyecto.getId());
        DiscordLogger.countryLog(countryLog, pais);
        DiscordLogger.staffLog(countryLog, pais);
    }

    public void cancelJoinRequest(Proyecto proyecto, Player player) {
        InteractionRegistry interactionRegistry = InteractionRegistry.getInstance();
        List<Interaction> interactions = interactionRegistry.findJoinRequest(proyecto);
        for (Interaction interaction : interactions) {
            if (interaction.getPlayerId() != null && interaction.getPlayerId().equals(player.getUuid())) {
                interactionRegistry.unload(interaction.getId());
                break;
            }
        }
    }

    public void expireJoinRequest(Proyecto proyecto, Player player) {
        cancelJoinRequest(proyecto, player);
        Pais pais = proyecto.getPais();
        String countryLog = lang.getString("project-join-request-expired-log").replace("%player%", player.getNombre()).replace("%proyectoId%", proyecto.getId());
        DiscordLogger.countryLog(countryLog, pais);
        DiscordLogger.staffLog(countryLog, pais);
    }

    public void acceptJoinRequest(Proyecto proyecto, Player player) {
        joinProject(proyecto, player);
        cancelJoinRequest(proyecto, player);
        Pais pais = proyecto.getPais();
        String countryLog = lang.getString("project-join-accepted-log").replace("%lider%", getLider(proyecto).getNombre()).replace("%player%", player.getNombre()).replace("%proyectoId%", proyecto.getId());
        DiscordLogger.countryLog(countryLog, pais);
        DiscordLogger.staffLog(countryLog, pais);
    }

    public void joinProject(Proyecto proyecto, Player player) {
        proyecto.addMiembro(player);
        ProyectoRegistry.getInstance().merge(proyecto.getId());
        Pais pais = proyecto.getPais();
        String countryLog = lang.getString("project-join-log").replace("%player%", player.getNombre()).replace("%proyectoId%", proyecto.getId());
        DiscordLogger.countryLog(countryLog, pais);
        DiscordLogger.staffLog(countryLog, pais);
    }

    public void leaveProject(Proyecto proyecto, Player player) {
        // TODO: Casos del lider abandonando el proyecto
        proyecto.removeMiembro(player);
        ProyectoRegistry.getInstance().merge(proyecto.getId());
        Pais pais = proyecto.getPais();
        String countryLog = lang.getString("project-leave-log").replace("%player%", player.getNombre()).replace("%proyectoId%", proyecto.getId());
        DiscordLogger.countryLog(countryLog, pais);
        DiscordLogger.staffLog(countryLog, pais);
    }

    public void createFinishProjectRequest(Proyecto proyecto) {
        proyecto.setEstado(Estado.EN_FINALIZACION);
        ProyectoRegistry.getInstance().merge(proyecto.getId());
        Interaction interaction = new Interaction(
            proyecto.getLider().getUuid(),
            proyecto.getId(),
            InteractionKey.FINISH_PROJECT,
            Instant.now(),
            Instant.now().plusSeconds(config.getLong("interaction-expirations.finish-project") * 60)
        );
        InteractionRegistry.getInstance().load(interaction);
        Pais pais = proyecto.getPais();
        String countryLog = lang.getString("project-finish-request-log").replace("%lider%", getLider(proyecto).getNombre()).replace("%proyectoId%", proyecto.getId());
        DiscordLogger.countryLog(countryLog, pais);
        DiscordLogger.staffLog(countryLog, pais);
    }

    public void cancelFinishProjectRequest(Proyecto proyecto) {
        proyecto.setEstado(Estado.RECLAMADO);
        ProyectoRegistry.getInstance().merge(proyecto.getId());
        InteractionRegistry interactionRegistry = InteractionRegistry.getInstance();
        Interaction interaction = interactionRegistry.findFinishRequest(proyecto);
        interactionRegistry.unload(interaction.getId());
    }

    public void acceptFinishProjectRequest(Proyecto proyecto, Player staff) {
        cancelFinishProjectRequest(proyecto);
        Pais pais = proyecto.getPais();
        String countryLog = lang.getString("project-finish-accepted-log").replace("%staff%", staff.getNombre()).replace("%proyectoId%", proyecto.getId());
        DiscordLogger.countryLog(countryLog, pais);
        DiscordLogger.staffLog(countryLog, pais);
    }

    public void deniedFinishProjectRequest(Proyecto proyecto, Player staff) {
        cancelFinishProjectRequest(proyecto);
        Pais pais = proyecto.getPais();
        String countryLog = lang.getString("project-finish-denied-log").replace("%staff%", staff.getNombre()).replace("%proyectoId%", proyecto.getId());
        DiscordLogger.countryLog(countryLog, pais);
        DiscordLogger.staffLog(countryLog, pais);
    }

    public void expireFinishProjectRequest(Proyecto proyecto) {
        cancelFinishProjectRequest(proyecto);
        Pais pais = proyecto.getPais();
        String countryLog = lang.getString("project-finish-request-expired-log").replace("%proyectoId%", proyecto.getId());
        DiscordLogger.countryLog(countryLog, pais);
        DiscordLogger.staffLog(countryLog, pais);
    }

    public void createRedefineProjectRequest(Proyecto proyecto) {
        proyecto.setEstado(Estado.REDEFINIENDO);
        Interaction interaction = new Interaction(
            proyecto.getLider().getUuid(),
            proyecto.getId(),
            InteractionKey.REDEFINE_PROJECT,
            Instant.now(),
            Instant.now().plusSeconds(config.getLong("interaction-expirations.redefine-project") * 60)
        );
        InteractionRegistry.getInstance().load(interaction);
        Pais pais = proyecto.getPais();
        String countryLog = lang.getString("project-redefine-request-log").replace("%lider%", getLider(proyecto).getNombre()).replace("%proyectoId%", proyecto.getId());
        DiscordLogger.countryLog(countryLog, pais);
        DiscordLogger.staffLog(countryLog, pais);
    }

    public void cancelRedefineProjectRequest(Proyecto proyecto) {
        proyecto.setEstado(Estado.RECLAMADO);
        ProyectoRegistry.getInstance().merge(proyecto.getId());
        InteractionRegistry interactionRegistry = InteractionRegistry.getInstance();
        Interaction interaction = interactionRegistry.findRedefineRequest(proyecto);
        interactionRegistry.unload(interaction.getId());
    }

    public void expireRedefineProjectRequest(Proyecto proyecto) {
        cancelRedefineProjectRequest(proyecto);
        Pais pais = proyecto.getPais();
        String countryLog = lang.getString("project-redefine-request-expired-log").replace("%proyectoId%", proyecto.getId());
        DiscordLogger.countryLog(countryLog, pais);
        DiscordLogger.staffLog(countryLog, pais);
    }

    public void acceptRedefineProjectRequest(Proyecto proyecto, Player staff) {
        cancelRedefineProjectRequest(proyecto);
        Pais pais = proyecto.getPais();
        String countryLog = lang.getString("project-redefine-accepted-log").replace("%staff%", staff.getNombre()).replace("%proyectoId%", proyecto.getId());
        DiscordLogger.countryLog(countryLog, pais);
        DiscordLogger.staffLog(countryLog, pais);
    }

    public void deniedRedefineProjectRequest(Proyecto proyecto, Player staff) {
        cancelRedefineProjectRequest(proyecto);
        Pais pais = proyecto.getPais();
        String countryLog = lang.getString("project-redefine-denied-log").replace("%staff%", staff.getNombre()).replace("%proyectoId%", proyecto.getId());
        DiscordLogger.countryLog(countryLog, pais);
        DiscordLogger.staffLog(countryLog, pais);
    }

    public void createEditProjectRequest(Proyecto proyecto) {
        proyecto.setEstado(Estado.EDITANDO);
        ProyectoRegistry.getInstance().merge(proyecto.getId());
        Interaction interaction = new Interaction(
            proyecto.getLider().getUuid(),
            proyecto.getId(),
            InteractionKey.EDIT_PROJECT,
            Instant.now(),
            Instant.now().plusSeconds(config.getLong("interaction-expirations.edit-project") * 60)
        );
        InteractionRegistry.getInstance().load(interaction);
        Pais pais = proyecto.getPais();
        String countryLog = lang.getString("project-edit-request-log").replace("%lider%", getLider(proyecto).getNombre()).replace("%proyectoId%", proyecto.getId());
        DiscordLogger.countryLog(countryLog, pais);
        DiscordLogger.staffLog(countryLog, pais);
    }

    public void cancelEditProjectRequest(Proyecto proyecto) {
        proyecto.setEstado(Estado.COMPLETADO);
        ProyectoRegistry.getInstance().merge(proyecto.getId());
        InteractionRegistry interactionRegistry = InteractionRegistry.getInstance();
        Interaction interaction = interactionRegistry.findEditRequest(proyecto);
        interactionRegistry.unload(interaction.getId());
    }

    public void expireEditProjectRequest(Proyecto proyecto) {
        cancelEditProjectRequest(proyecto);
        Pais pais = proyecto.getPais();
        String countryLog = lang.getString("project-edit-request-expired-log").replace("%proyectoId%", proyecto.getId());
        DiscordLogger.countryLog(countryLog, pais);
        DiscordLogger.staffLog(countryLog, pais);
    }

    public void acceptEditProjectRequest(Proyecto proyecto, Player staff) {
        cancelEditProjectRequest(proyecto);
        Pais pais = proyecto.getPais();
        String countryLog = lang.getString("project-edit-accepted-log").replace("%staff%", staff.getNombre()).replace("%proyectoId%", proyecto.getId());
        DiscordLogger.countryLog(countryLog, pais);
        DiscordLogger.staffLog(countryLog, pais);
    }

    public void deniedEditProjectRequest(Proyecto proyecto, Player staff) {
        cancelEditProjectRequest(proyecto);
        Pais pais = proyecto.getPais();
        String countryLog = lang.getString("project-edit-denied-log").replace("%staff%", staff.getNombre()).replace("%proyectoId%", proyecto.getId());
        DiscordLogger.countryLog(countryLog, pais);
        DiscordLogger.staffLog(countryLog, pais);
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
