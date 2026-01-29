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
import com.bteconosur.db.util.Estado;
import com.bteconosur.db.registry.RangoUsuarioRegistry;
import com.bteconosur.db.registry.PlayerRegistry;

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

    private final LuckPerms lpApi;
    private final TipoUsuarioRegistry tipoUsuarioRegistry;
    private final RangoUsuarioRegistry rangoUsuarioRegistry;

    public PermissionManager() {
        ConfigHandler configHandler = ConfigHandler.getInstance();
        lang = configHandler.getLang();

        ConsoleLogger.info(lang.getString("permission-manager-initializing"));

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

    public boolean areActiveOrEditing(Set<Proyecto> proyectos) {
        if (proyectos == null || proyectos.isEmpty()) return false;
        for (Proyecto proyecto : proyectos) {
            if (proyecto.getEstado().equals(Estado.ACTIVO) || proyecto.getEstado().equals(Estado.EDITANDO)) return true;
        }
        return false;
    }

    public boolean isMiembro(Player player, Proyecto proyecto) {
        if (player == null) return false;
        if (proyecto == null) return false;
        return proyecto.getMiembros().contains(player);
    }

    public boolean isMiembro(Player player, Set<Proyecto> proyectos) {
        if (player == null) return false;
        if (proyectos == null || proyectos.isEmpty()) return false;
        for (Proyecto proyecto : proyectos) {
            if (proyecto.getMiembros().contains(player)) {
                return true;
            }
        }
        return false;
    }

    public boolean isLider(Player player, Proyecto proyecto) {
        if (player == null) return false;
        if (proyecto == null) return false;
        return proyecto.getLider().equals(player);
    }

    public boolean isLider(Player player, Set<Proyecto> proyectos) {
        if (player == null) return false;
        if (proyectos == null || proyectos.isEmpty()) return false;
        for (Proyecto proyecto : proyectos) {
            if (proyecto.getLider().equals(player)) {
                return true;
            }
        }
        return false;
    }

    public boolean isManager(Player player, Pais pais) {
        if (player == null) return false;
        if (pais == null) return false;
        return player.getPaisesManager().contains(pais);
    }

    public boolean isManager(Player player) {
        if (player == null) return false;
        return player.getPaisesManager() != null && !player.getPaisesManager().isEmpty();
    }

    public boolean isReviewer(Player player, Pais pais) {
        if (player == null) return false;
        if (pais == null) return false;
        if (player.getPaisesReviewer().contains(pais)) return true;
        return isManager(player, pais);
    }

    public boolean isReviewer(Player player) {
        if (player == null) return false;
        if (player.getPaisesReviewer() != null && !player.getPaisesReviewer().isEmpty()) return true;
        return isManager(player);
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
        ConsoleLogger.info("Verificando Tipos de Usuario..."); 
        GroupManager groupManager = lpApi.getGroupManager();
        List<TipoUsuario> tipos = tipoUsuarioRegistry.getList();
        if (tipos == null || tipos.isEmpty()) {
            ConsoleLogger.info("No se encontraron TiposUsuario en la base de datos.");
            return false;
        }

        for (TipoUsuario tipo : tipos) {
            String groupName = "tipo_" + tipo.getNombre().toLowerCase();
            Group group = groupManager.getGroup(groupName);
            if (group == null) {
                ConsoleLogger.info("El grupo de LuckPerms para TipoUsuario '" + tipo.getNombre() + "' no existe. Creándolo...");
                group = groupManager.createAndLoadGroup(groupName).join();
            }
            final Group groupRef = group;

            boolean modified = false;
            Set<NodoPermiso> permisos = tipo.getPermisos();
            if (permisos == null || permisos.isEmpty()) {
                List<PermissionNode> existingPerms = groupRef.data().toCollection().stream()
                    .filter(node -> node instanceof PermissionNode)
                    .map(node -> (PermissionNode) node)
                    .toList();

                if (!existingPerms.isEmpty()) {
                    for (PermissionNode node : existingPerms) {
                        group.data().remove(node);
                        modified = true;
                        ConsoleLogger.info("Desincronizacón detectada: Se removió el permiso '" + node.getPermission() + "' del grupo '" + groupName + "'.");
                    }
                }

                if (modified) groupManager.saveGroup(groupRef).join();
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
                ConsoleLogger.info("Desincronizacón detectada: Se removió el permiso '" + node.getPermission() + "' del grupo '" + groupName + "'.");
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
                ConsoleLogger.info("Se añadió automáticamente el permiso '" + permisoNombre + "' al grupo '" + groupName + "'.");
            }
            if (modified) groupManager.saveGroup(groupRef).join();
        }

        return true;
    }

    private boolean checkRangoUsuario() {
        ConsoleLogger.info("Verificando Rangos de Usuario...");
        GroupManager groupManager = lpApi.getGroupManager();
        List<RangoUsuario> rangos = rangoUsuarioRegistry.getList();
        if (rangos == null || rangos.isEmpty()) {
            ConsoleLogger.info("No se encontraron RangosUsuario en la base de datos.");
            return false;
        }

        for (RangoUsuario rango : rangos) {
            String groupName = "rango_" + rango.getNombre().toLowerCase();
            Group group = groupManager.getGroup(groupName);
            if (group == null) {
                ConsoleLogger.info("El grupo de LuckPerms para RangoUsuario '" + rango.getNombre() + "' no existe. Creándolo...");
                group = groupManager.createAndLoadGroup(groupName).join();
            }
            final Group groupRef = group;

            boolean modified = false;
            Set<NodoPermiso> permisos = rango.getPermisos();
            if (permisos == null || permisos.isEmpty()) {
                List<PermissionNode> existingPerms = groupRef.data().toCollection().stream()
                    .filter(node -> node instanceof PermissionNode)
                    .map(node -> (PermissionNode) node)
                    .toList();

                if (!existingPerms.isEmpty()) {
                    for (PermissionNode node : existingPerms) {
                        group.data().remove(node);
                        modified = true;
                        ConsoleLogger.info("Desincronizacón detectada: Se removió el permiso '" + node.getPermission() + "' del grupo '" + groupName + "'.");
                    }
                }

                if (modified) groupManager.saveGroup(groupRef).join();
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
                ConsoleLogger.info("Desincronizacón detectada: Se removió el permiso '" + node.getPermission() + "' del grupo '" + groupName + "'.");
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
                ConsoleLogger.info("Se añadió automáticamente el permiso '" + permisoNombre + "' al grupo '" + groupName + "'.");
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
            ConsoleLogger.info("No se pudo obtener el usuario de LuckPerms para " + player.getNombre());
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
            ConsoleLogger.info("Desincronizacón detectada: Se removió el grupo '" + node.getGroupName() + "' del jugador " + player.getNombre());
        }

        Tristate state = user.data().contains(targetNode, NodeEqualityPredicate.EXACT);
        if (state == Tristate.UNDEFINED) {
            user.data().add(targetNode);
            modified = true;
            ConsoleLogger.info("Desincronizacón detectada: Se añadió el grupo '" + targetGroupName + "' al jugador " + player.getNombre());
        }

        if (modified) userManager.saveUser(user).join();
    }

    public void checkRangoUsuario(Player player) {
        if (player == null || player.getRangoUsuario() == null) return;
        
        UserManager userManager = lpApi.getUserManager();
        User user = userManager.getUser(player.getUuid());
        if (user == null) {
            ConsoleLogger.info("No se pudo obtener el usuario de LuckPerms para " + player.getNombre());
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
            ConsoleLogger.info("Desincronizacón detectada: Se removió el grupo '" + node.getGroupName() + "' del jugador " + player.getNombre());
        }

        Tristate state = user.data().contains(targetNode, NodeEqualityPredicate.EXACT);
        if (state == Tristate.UNDEFINED) {
            user.data().add(targetNode);
            modified = true;
            ConsoleLogger.info("Desincronizacón detectada: Se añadió el grupo '" + targetGroupName + "' al jugador " + player.getNombre());
        }

        if (modified) userManager.saveUser(user).join();
    }

    public void shutdown() {
        ConsoleLogger.info(lang.getString("permission-manager-shutting-down"));
    }

    public Player switchTipoUsuario(Player player, TipoUsuario tipo) {
        player.setTipoUsuario(tipo);
        player = PlayerRegistry.getInstance().merge(player.getUuid());
        
        UserManager userManager = lpApi.getUserManager();
        User user = userManager.getUser(player.getUuid());
        if (user == null) { 
            ConsoleLogger.info("No se pudo obtener el usuario de LuckPerms para " + player.getNombre());
            return player;
        }
        
        String targetGroupName = "tipo_" + tipo.getNombre().toLowerCase();
        InheritanceNode targetNode = InheritanceNode.builder(targetGroupName).build();
        
        user.getNodes().stream()
            .filter(node -> node instanceof InheritanceNode)
            .map(node -> (InheritanceNode) node)
            .filter(node -> node.getGroupName().toLowerCase().startsWith("tipo_") && !node.getGroupName().equalsIgnoreCase(targetGroupName))
            .forEach(node -> user.data().remove(node));
        
        if (user.data().contains(targetNode, NodeEqualityPredicate.EXACT) == Tristate.UNDEFINED) {
            user.data().add(targetNode);
        }
        
        userManager.saveUser(user).join();
        return player;  
    }

    public Player switchRangoUsuario(Player player, RangoUsuario rango) {
        if (player == null || rango == null) return player;
        if (rango.equals(player.getRangoUsuario())) return player;
        
        player.setRangoUsuario(rango);
        player = PlayerRegistry.getInstance().merge(player.getUuid());
        
        UserManager userManager = lpApi.getUserManager();
        User user = userManager.getUser(player.getUuid());
        if (user == null) { 
            ConsoleLogger.info("No se pudo obtener el usuario de LuckPerms para " + player.getNombre());
            return player;
        }
        
        String targetGroupName = "rango_" + rango.getNombre().toLowerCase();
        InheritanceNode targetNode = InheritanceNode.builder(targetGroupName).build();
        
        user.getNodes().stream()
            .filter(node -> node instanceof InheritanceNode)
            .map(node -> (InheritanceNode) node)
            .filter(node -> node.getGroupName().toLowerCase().startsWith("rango_") && !node.getGroupName().equalsIgnoreCase(targetGroupName))
            .forEach(node -> user.data().remove(node));
        
        if (user.data().contains(targetNode, NodeEqualityPredicate.EXACT) == Tristate.UNDEFINED) {
            user.data().add(targetNode);
        }
        
        userManager.saveUser(user).join();
        return player;
    }

    public Player addManager(Player player, Pais pais) {
        if (player == null || pais == null) return player;
        if (isManager(player, pais)) return player;

        player.addPaisManager(pais);
        return PlayerRegistry.getInstance().merge(player.getUuid());
    }

    public Player removeManager(Player player, Pais pais) {
        if (player == null || pais == null) return player;
        if (!isManager(player, pais)) return player;
        player.removePaisManager(pais);
        return PlayerRegistry.getInstance().merge(player.getUuid());
    }

    public Player addReviewer(Player player, Pais pais) {
        if (player == null || pais == null) return player;
        if (isReviewer(player, pais)) return player;

        player.addPaisReviewer(pais);
        return PlayerRegistry.getInstance().merge(player.getUuid());
    }

    public Player removeReviewer(Player player, Pais pais) {
        if (player == null || pais == null) return player;
        if (!isReviewer(player, pais)) return player;

        player.removePaisReviewer(pais);
        return PlayerRegistry.getInstance().merge(player.getUuid());
    }

    public static PermissionManager getInstance() {
        if (instance == null) {
            instance = new PermissionManager();
        }
        return instance;
    }
//TODO: Display de managers y reviewers.
}
