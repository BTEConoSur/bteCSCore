package com.bteconosur.db;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.bteconosur.core.BTEConoSur;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
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

    private final LuckPerms lpApi;
    private final TipoUsuarioRegistry tipoUsuarioRegistry;
    private final RangoUsuarioRegistry rangoUsuarioRegistry;

    public PermissionManager() {
        ConsoleLogger.info(LanguageHandler.getText("permission-manager-initializing"));

        lpApi = BTEConoSur.getLuckPermsApi();
        tipoUsuarioRegistry = TipoUsuarioRegistry.getInstance();
        rangoUsuarioRegistry = RangoUsuarioRegistry.getInstance();

        if (!checkTipoUsuario()) {
            PluginRegistry.disablePlugin("CHECK_TIPO_USUARIO_ERROR");
            return;
        }

        if (!checkRangoUsuario()) {
            PluginRegistry.disablePlugin("CHECK_RANGO_USUARIO_ERROR");
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

    public boolean isMiembroOrLider(Player player, Proyecto proyecto) {
        if (player == null) return false;
        if (proyecto == null) return false;
        return isMiembro(player, proyecto) || isLider(player, proyecto);
    }

    public boolean isMiembro(Player player, Set<Proyecto> proyectos) {
        if (player == null) return false;
        if (proyectos == null || proyectos.isEmpty()) return false;
        for (Proyecto proyecto : proyectos) {
            if (isMiembro(player, proyecto)) {
                return true;
            }
        }
        return false;
    }

    public boolean isLider(Player player, Proyecto proyecto) {
        if (player == null) return false;
        if (proyecto == null) return false;
        return player.equals(proyecto.getLider());
    }

    public boolean hasMembers(Proyecto proyecto) {
        if (proyecto == null) return false;
        return !proyecto.getMiembros().isEmpty();
    }

    public boolean hasLider(Proyecto proyecto) {
        if (proyecto == null) return false;
        return proyecto.getLider() != null;
    }

    public boolean isLider(Player player, Set<Proyecto> proyectos) {
        if (player == null) return false;
        if (proyectos == null || proyectos.isEmpty()) return false;
        for (Proyecto proyecto : proyectos) {
            if (isLider(player, proyecto)) return true;
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

    public boolean isPostulante(Player player) {
        if (player == null) return false;
        TipoUsuario tipoPostulante = tipoUsuarioRegistry.getPostulante();
        return isTipoUsuario(player, tipoPostulante);
    }

    public boolean isAdmin(Player player) {
        if (player == null) return false;
        RangoUsuario rango = player.getRangoUsuario();
        if (rango == null) return false;
        return rango.getNombre().equalsIgnoreCase("Admin");
    }

    private boolean checkTipoUsuario() {
        ConsoleLogger.info(LanguageHandler.getText("checking-tipos-usuario")); 
        GroupManager groupManager = lpApi.getGroupManager();
        List<TipoUsuario> tipos = tipoUsuarioRegistry.getList();
        if (tipos == null || tipos.isEmpty()) {
            ConsoleLogger.info(LanguageHandler.getText("tipos-usuarios-not-found"));
            return false;
        }

        for (TipoUsuario tipo : tipos) {
            String groupName = "tipo_" + tipo.getNombre().toLowerCase();
            Group group = groupManager.getGroup(groupName);
            if (group == null) {
                ConsoleLogger.info(LanguageHandler.replaceMC("lp-group-tipo-usuario-missing", Language.getDefault(), tipo));
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
                        ConsoleLogger.info(LanguageHandler.getText("lp-permission-desync").replace("%permiso%", node.getPermission()).replace("%grupo%", groupName));
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
                ConsoleLogger.info(LanguageHandler.getText("lp-permission-desync").replace("%permiso%", node.getPermission()).replace("%grupo%", groupName));
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
                ConsoleLogger.info(LanguageHandler.getText("lp-permission-added").replace("%permiso%", permisoNombre).replace("%grupo%", groupName));
            }
            if (modified) groupManager.saveGroup(groupRef).join();
        }

        return true;
    }

    private boolean checkRangoUsuario() {
        ConsoleLogger.info(LanguageHandler.getText("checking-rangos-usuario"));
        GroupManager groupManager = lpApi.getGroupManager();
        List<RangoUsuario> rangos = rangoUsuarioRegistry.getList();
        if (rangos == null || rangos.isEmpty()) {
            ConsoleLogger.info(LanguageHandler.getText("rangos-usuarios-not-found"));
            return false;
        }

        for (RangoUsuario rango : rangos) {
            String groupName = "rango_" + rango.getNombre().toLowerCase();
            Group group = groupManager.getGroup(groupName);
            if (group == null) {
                ConsoleLogger.info(LanguageHandler.replaceMC("lp-group-rango-usuario-missing", Language.getDefault(), rango));
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
                        ConsoleLogger.info(LanguageHandler.getText("lp-permission-desync").replace("%permiso%", node.getPermission()).replace("%grupo%", groupName));
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
                ConsoleLogger.info(LanguageHandler.getText("lp-permission-desync").replace("%permiso%", node.getPermission()).replace("%grupo%", groupName));
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
                ConsoleLogger.info(LanguageHandler.getText("lp-permission-added").replace("%permiso%", permisoNombre).replace("%grupo%", groupName));
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
            ConsoleLogger.info(LanguageHandler.getText("lp-user-not-found").replace("%player%", player.getNombre()));
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
            ConsoleLogger.info(LanguageHandler.getText("lp-group-removed").replace("%grupo%", targetGroupName).replace("%player%", player.getNombre()));
        }

        Tristate state = user.data().contains(targetNode, NodeEqualityPredicate.EXACT);
        if (state == Tristate.UNDEFINED) {
            user.data().add(targetNode);
            modified = true;
            ConsoleLogger.info(LanguageHandler.getText("lp-group-added").replace("%grupo%", targetGroupName).replace("%player%", player.getNombre()));
        }

        if (modified) userManager.saveUser(user).join();
    }

    public void checkRangoUsuario(Player player) {
        if (player == null || player.getRangoUsuario() == null) return;
        
        UserManager userManager = lpApi.getUserManager();
        User user = userManager.getUser(player.getUuid());
        if (user == null) {
            ConsoleLogger.info(LanguageHandler.getText("lp-user-not-found").replace("%player%", player.getNombre()));
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
            ConsoleLogger.info(LanguageHandler.getText("lp-group-removed").replace("%grupo%", node.getGroupName()).replace("%player%", player.getNombre()));
        }

        Tristate state = user.data().contains(targetNode, NodeEqualityPredicate.EXACT);
        if (state == Tristate.UNDEFINED) {
            user.data().add(targetNode);
            modified = true;
            ConsoleLogger.info(LanguageHandler.getText("lp-group-added").replace("%grupo%", targetGroupName).replace("%player%", player.getNombre()));
        }

        if (modified) userManager.saveUser(user).join();
    }

    public void shutdown() {
        ConsoleLogger.info(LanguageHandler.getText("permission-manager-shutting-down"));
    }

    public Player switchTipoUsuario(Player player, TipoUsuario tipo) {
        player.setTipoUsuario(tipo);
        player = PlayerRegistry.getInstance().merge(player.getUuid());
        
        UserManager userManager = lpApi.getUserManager();
        User user = userManager.getUser(player.getUuid());
        if (user == null) { 
            ConsoleLogger.info(LanguageHandler.getText("lp-user-not-found").replace("%player%", player.getNombre()));
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
            ConsoleLogger.info(LanguageHandler.getText("lp-user-not-found").replace("%player%", player.getNombre()));
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
