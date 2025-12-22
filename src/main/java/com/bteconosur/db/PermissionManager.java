package com.bteconosur.db;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.BTEConoSur;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.util.ConsoleLogger;
import com.bteconosur.core.util.PluginRegistry;
import com.bteconosur.db.model.NodoPermiso;
import com.bteconosur.db.model.Pais;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.Proyecto;
import com.bteconosur.db.model.RangoUsuario;
import com.bteconosur.db.model.TipoUsuario;
import com.bteconosur.db.registry.TipoUsuarioRegistry;
import com.bteconosur.db.registry.RangoUsuarioRegistry;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.group.GroupManager;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import net.luckperms.api.node.types.InheritanceNode;
import net.luckperms.api.node.types.PermissionNode;
import net.luckperms.api.node.NodeEqualityPredicate;
import net.luckperms.api.util.Tristate;

public class PermissionManager {

    private static PermissionManager instance;

    private final YamlConfiguration lang;
    private final ConsoleLogger logger;

    private final LuckPerms lpApi;
    private final TipoUsuarioRegistry tipoUsuarioRegistry;
    private final RangoUsuarioRegistry rangoUsuarioRegistry;

    public PermissionManager() {
        ConfigHandler configHandler = ConfigHandler.getInstance();
        lang = configHandler.getLang();
        logger = BTEConoSur.getConsoleLogger();

        logger.info(lang.getString("permission-manager-initializing"));

        lpApi = BTEConoSur.getLuckPermsApi();
        tipoUsuarioRegistry = TipoUsuarioRegistry.getInstance();
        rangoUsuarioRegistry = RangoUsuarioRegistry.getInstance();

        if (!checkTipoUsuario()) {
            PluginRegistry.disablePlugin("Error al verificar los Tipos de Usuario y sus permisos.");
            return;
        }

        if (!checkRangoUsuario()) {
            PluginRegistry.disablePlugin("Error al verificar los Rangos de Usuario y sus permisos.");
            return;
        }
    }

    public boolean isMiembro(Player player, Proyecto proyecto) {
        if (player == null) return false;
        if (proyecto == null) return false;
        return proyecto.getMiembros().contains(player);
    }

    public boolean isLider(Player player, Proyecto proyecto) {
        if (player == null) return false;
        if (proyecto == null) return false;
        return proyecto.getLider().equals(player);
    }

    public boolean isManager(Player player, Pais pais) {
        if (player == null) return false;
        if (pais == null) return false;
        return player.getPaisesManager().contains(pais);
    }

    public boolean isReviewer(Player player, Pais pais) {
        if (player == null) return false;
        if (pais == null) return false;
        return player.getPaisesReviewer().contains(pais);
    }

    public boolean isRangoUsuario(Player player, RangoUsuario rango) {
        if (player == null) return false;
        if (rango == null) return false;
        return player.getRangoUsuario().equals(rango);
    }

    public boolean isTipoUsuario(Player player, TipoUsuario tipo) {
        if (player == null) return false;
        if (tipo == null) return false;
        return player.getTipoUsuario().equals(tipo);
    }

    public boolean isAdmin(Player player) {
        if (player == null) return false;
        RangoUsuario rango = player.getRangoUsuario();
        if (rango == null) return false;
        return rango.getNombre().equalsIgnoreCase("Admin");
    }

    private boolean checkTipoUsuario() {
        logger.info("Verificando Tipos de Usuario..."); 
        GroupManager groupManager = lpApi.getGroupManager();
        List<TipoUsuario> tipos = tipoUsuarioRegistry.getList();
        if (tipos == null || tipos.isEmpty()) {
            logger.warn("No se encontraron TiposUsuario en la base de datos.");
            return false;
        }

        for (TipoUsuario tipo : tipos) {
            String groupName = "tipo_" + tipo.getNombre().toLowerCase();
            Group group = groupManager.getGroup(groupName);
            if (group == null) {
                logger.warn("El grupo de LuckPerms para TipoUsuario '" + tipo.getNombre() + "' no existe. Creándolo...");
                group = groupManager.createAndLoadGroup(groupName).join();
            }
            final Group groupRef = group;

            boolean modified = false;
            Set<NodoPermiso> permisos = tipo.getPermisos();
            if (permisos == null || permisos.isEmpty()) {
                logger.warn("El TipoUsuario '" + tipo.getNombre() + "' no tiene permisos asignados en la base de datos.");
                continue;
            }

            Set<String> permisosEsperados = permisos.stream()
                .filter(p -> p != null && p.getNombre() != null)
                .map(NodoPermiso::getNombre)
                .collect(Collectors.toSet());

            List<PermissionNode> toRemove = groupRef.data().toCollection().stream()
                .filter(node -> node instanceof PermissionNode)
                .map(node -> (PermissionNode) node)
                .filter(node -> !permisosEsperados.contains(node.getPermission()))
                .toList();

            for (PermissionNode node : toRemove) {
                group.data().remove(node);
                modified = true;
                logger.warn("Desincronizacón detectada: Se removió el permiso '" + node.getPermission() + "' del grupo '" + groupName + "'.");
            }
            
            List<String> toAdd = permisosEsperados.stream()
                .filter(permisoNombre -> {
                    PermissionNode node = PermissionNode.builder(permisoNombre).build();
                    return groupRef.data().contains(node, NodeEqualityPredicate.EXACT) == Tristate.UNDEFINED;
                })
                .toList();

            for (String permisoNombre : toAdd) {
                PermissionNode node = PermissionNode.builder(permisoNombre).build();
                groupRef.data().add(node);
                modified = true;
                logger.warn("Se añadió automáticamente el permiso '" + permisoNombre + "' al grupo '" + groupName + "'.");
            }
            if (modified) groupManager.saveGroup(groupRef).join();
        }

        return true;
    }

    private boolean checkRangoUsuario() {
        logger.info("Verificando Rangos de Usuario...");
        GroupManager groupManager = lpApi.getGroupManager();
        List<RangoUsuario> rangos = rangoUsuarioRegistry.getList();
        if (rangos == null || rangos.isEmpty()) {
            logger.warn("No se encontraron RangosUsuario en la base de datos.");
            return false;
        }

        for (RangoUsuario rango : rangos) {
            String groupName = "rango_" + rango.getNombre().toLowerCase();
            Group group = groupManager.getGroup(groupName);
            if (group == null) {
                logger.warn("El grupo de LuckPerms para RangoUsuario '" + rango.getNombre() + "' no existe. Creándolo...");
                group = groupManager.createAndLoadGroup(groupName).join();
            }
            final Group groupRef = group;

            boolean modified = false;
            Set<NodoPermiso> permisos = rango.getPermisos();
            if (permisos == null || permisos.isEmpty()) {
                logger.warn("El RangoUsuario '" + rango.getNombre() + "' no tiene permisos asignados en la base de datos.");
                continue;
            }

            Set<String> permisosEsperados = permisos.stream()
                .filter(p -> p != null && p.getNombre() != null)
                .map(NodoPermiso::getNombre)
                .collect(Collectors.toSet());

            // Remover nodos de permisos que no están en la BD
            List<PermissionNode> toRemove = groupRef.data().toCollection().stream()
                .filter(node -> node instanceof PermissionNode)
                .map(node -> (PermissionNode) node)
                .filter(node -> !permisosEsperados.contains(node.getPermission()))
                .toList();

            for (PermissionNode node : toRemove) {
                group.data().remove(node);
                modified = true;
                logger.warn("Desincronizacón detectada: Se removió el permiso '" + node.getPermission() + "' del grupo '" + groupName + "'.");
            }

            // Añadir permisos faltantes
            List<String> toAdd = permisosEsperados.stream()
                .filter(permisoNombre -> {
                    PermissionNode node = PermissionNode.builder(permisoNombre).build();
                    return groupRef.data().contains(node, NodeEqualityPredicate.EXACT) == Tristate.UNDEFINED;
                })
                .toList();

            for (String permisoNombre : toAdd) {
                PermissionNode node = PermissionNode.builder(permisoNombre).build();
                groupRef.data().add(node);
                modified = true;
                logger.warn("Se añadió automáticamente el permiso '" + permisoNombre + "' al grupo '" + groupName + "'.");
            }
            if (modified) groupManager.saveGroup(groupRef).join();
        }

        return true;
    }

    public void checkTipoUsuario(Player player) {
        if (player == null || player.getTipoUsuario() == null) return;
        
        UserManager userManager = lpApi.getUserManager();
        User user = userManager.getUser(player.getUuid());
        if (user == null) {
            logger.warn("No se pudo obtener el usuario de LuckPerms para " + player.getNombre());
            return;
        }

        TipoUsuario tipo = player.getTipoUsuario();
        String targetGroupName = "tipo_" + tipo.getNombre().toLowerCase();
        InheritanceNode targetNode = InheritanceNode.builder(targetGroupName).build();

        boolean modified = false;
        
        List<InheritanceNode> toRemove = user.getNodes().stream()
            .filter(node -> node instanceof InheritanceNode)
            .map(node -> (InheritanceNode) node)
            .filter(node -> node.getGroupName().toLowerCase().startsWith("tipo_") && !node.getGroupName().equalsIgnoreCase(targetGroupName))
            .toList();
        
        for (InheritanceNode node : toRemove) {
            user.data().remove(node);
            modified = true;
            logger.warn("Desincronizacón detectada: Se removió el grupo '" + node.getGroupName() + "' del jugador " + player.getNombre());
        }

        Tristate state = user.data().contains(targetNode, NodeEqualityPredicate.EXACT);
        if (state == Tristate.UNDEFINED) {
            user.data().add(targetNode);
            modified = true;
            logger.warn("Desincronizacón detectada: Se añadió el grupo '" + targetGroupName + "' al jugador " + player.getNombre());
        }

        if (modified) userManager.saveUser(user).join();
    }

    public void checkRangoUsuario(Player player) {
        if (player == null || player.getRangoUsuario() == null) return;
        
        UserManager userManager = lpApi.getUserManager();
        User user = userManager.getUser(player.getUuid());
        if (user == null) {
            logger.warn("No se pudo obtener el usuario de LuckPerms para " + player.getNombre());
            return;
        }

        RangoUsuario rango = player.getRangoUsuario();
        String targetGroupName = "rango_" + rango.getNombre().toLowerCase();
        InheritanceNode targetNode = InheritanceNode.builder(targetGroupName).build();

        boolean modified = false;
        
        List<InheritanceNode> toRemove = user.getNodes().stream()
            .filter(node -> node instanceof InheritanceNode)
            .map(node -> (InheritanceNode) node)
            .filter(node -> node.getGroupName().toLowerCase().startsWith("rango_") && !node.getGroupName().equalsIgnoreCase(targetGroupName))
            .toList();
        
        for (InheritanceNode node : toRemove) {
            user.data().remove(node);
            modified = true;
            logger.warn("Desincronizacón detectada: Se removió el grupo '" + node.getGroupName() + "' del jugador " + player.getNombre());
        }

        Tristate state = user.data().contains(targetNode, NodeEqualityPredicate.EXACT);
        if (state == Tristate.UNDEFINED) {
            user.data().add(targetNode);
            modified = true;
            logger.warn("Desincronizacón detectada: Se añadió el grupo '" + targetGroupName + "' al jugador " + player.getNombre());
        }

        if (modified) userManager.saveUser(user).join();
    }

    public void shutdown() {
        logger.info(lang.getString("permission-manager-shutting-down"));
    }

    public static PermissionManager getInstance() {
        if (instance == null) {
            instance = new PermissionManager();
        }
        return instance;
    }

}
