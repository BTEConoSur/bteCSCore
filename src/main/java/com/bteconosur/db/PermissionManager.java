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

/**
 * Gestor de permisos del plugin.
 * Sincroniza los permisos de la base de datos con LuckPerms y proporciona métodos
 * para verificar roles, permisos y estados de jugadores en proyectos y países.
 */
public class PermissionManager {

    private static PermissionManager instance;

    private final LuckPerms lpApi;
    private final TipoUsuarioRegistry tipoUsuarioRegistry;
    private final RangoUsuarioRegistry rangoUsuarioRegistry;

    /**
     * Constructor del gestor de permisos.
     * Inicializa LuckPerms y verifica la sincronización de tipos de usuario y rangos.
     */
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

    /**
     * Verifica si alguno de los proyectos del conjunto está en estado ACTIVO o EDITANDO.
     * 
     * @param proyectos Conjunto de proyectos a verificar
     * @return true si al menos un proyecto está activo o en edición, false en caso contrario
     */
    public boolean areActiveOrEditing(Set<Proyecto> proyectos) {
        if (proyectos == null || proyectos.isEmpty()) return false;
        for (Proyecto proyecto : proyectos) {
            if (proyecto.getEstado().equals(Estado.ACTIVO) || proyecto.getEstado().equals(Estado.EDITANDO)) return true;
        }
        return false;
    }

    /**
     * Verifica si un jugador es miembro de un proyecto.
     * 
     * @param player Jugador a verificar
     * @param proyecto Proyecto a verificar
     * @return true si el jugador es miembro del proyecto, false en caso contrario
     */
    public boolean isMiembro(Player player, Proyecto proyecto) {
        if (player == null) return false;
        if (proyecto == null) return false;
        return proyecto.getMiembros().contains(player);
    }

    /**
     * Verifica si un jugador es miembro o líder de un proyecto.
     * 
     * @param player Jugador a verificar
     * @param proyecto Proyecto a verificar
     * @return true si el jugador es miembro o líder del proyecto, false en caso contrario
     */
    public boolean isMiembroOrLider(Player player, Proyecto proyecto) {
        if (player == null) return false;
        if (proyecto == null) return false;
        return isMiembro(player, proyecto) || isLider(player, proyecto);
    }

    /**
     * Verifica si un jugador es miembro de al menos uno de los proyectos del conjunto.
     * 
     * @param player Jugador a verificar
     * @param proyectos Conjunto de proyectos a verificar
     * @return true si el jugador es miembro de al menos un proyecto, false en caso contrario
     */
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

    /**
     * Verifica si un jugador es líder de un proyecto.
     * 
     * @param player Jugador a verificar
     * @param proyecto Proyecto a verificar
     * @return true si el jugador es líder del proyecto, false en caso contrario
     */
    public boolean isLider(Player player, Proyecto proyecto) {
        if (player == null) return false;
        if (proyecto == null) return false;
        return player.equals(proyecto.getLider());
    }

    /**
     * Verifica si un proyecto tiene miembros asignados.
     * 
     * @param proyecto Proyecto a verificar
     * @return true si el proyecto tiene al menos un miembro, false en caso contrario
     */
    public boolean hasMembers(Proyecto proyecto) {
        if (proyecto == null) return false;
        return !proyecto.getMiembros().isEmpty();
    }

    /**
     * Verifica si un proyecto tiene un líder asignado.
     * 
     * @param proyecto Proyecto a verificar
     * @return true si el proyecto tiene líder, false en caso contrario
     */
    public boolean hasLider(Proyecto proyecto) {
        if (proyecto == null) return false;
        return proyecto.getLider() != null;
    }

    /**
     * Verifica si un jugador es líder de al menos uno de los proyectos del conjunto.
     * 
     * @param player Jugador a verificar
     * @param proyectos Conjunto de proyectos a verificar
     * @return true si el jugador es líder de al menos un proyecto, false en caso contrario
     */
    public boolean isLider(Player player, Set<Proyecto> proyectos) {
        if (player == null) return false;
        if (proyectos == null || proyectos.isEmpty()) return false;
        for (Proyecto proyecto : proyectos) {
            if (isLider(player, proyecto)) return true;
        }
        return false;
    }

    /**
     * Verifica si un jugador es manager de un país específico.
     * 
     * @param player Jugador a verificar
     * @param pais País a verificar
     * @return true si el jugador es manager del país, false en caso contrario
     */
    public boolean isManager(Player player, Pais pais) {
        if (player == null) return false;
        if (pais == null) return false;
        return player.getPaisesManager().contains(pais);
    }

    /**
     * Verifica si un jugador es manager de algún país.
     * 
     * @param player Jugador a verificar
     * @return true si el jugador es manager de al menos un país, false en caso contrario
     */
    public boolean isManager(Player player) {
        if (player == null) return false;
        return player.getPaisesManager() != null && !player.getPaisesManager().isEmpty();
    }

    /**
     * Verifica si un jugador es reviewer de un país específico.
     * Un manager también se considera reviewer.
     * 
     * @param player Jugador a verificar
     * @param pais País a verificar
     * @return true si el jugador es reviewer o manager del país, false en caso contrario
     */
    public boolean isReviewer(Player player, Pais pais) {
        if (player == null) return false;
        if (pais == null) return false;
        if (player.getPaisesReviewer().contains(pais)) return true;
        return isManager(player, pais);
    }

    /**
     * Verifica si un jugador es reviewer de algún país.
     * Un manager también se considera reviewer.
     * 
     * @param player Jugador a verificar
     * @return true si el jugador es reviewer o manager de al menos un país, false en caso contrario
     */
    public boolean isReviewer(Player player) {
        if (player == null) return false;
        if (player.getPaisesReviewer() != null && !player.getPaisesReviewer().isEmpty()) return true;
        return isManager(player);
    }

    /**
     * Verifica si un jugador tiene un rango de usuario específico.
     * 
     * @param player Jugador a verificar
     * @param rango Rango a verificar
     * @return true si el jugador tiene el rango especificado, false en caso contrario
     */
    public boolean isRangoUsuario(Player player, RangoUsuario rango) {
        if (player == null) return false;
        if (rango == null) return false;
        return player.getRangoUsuario().equals(rango);
    }

    /**
     * Verifica si un jugador tiene un tipo de usuario específico.
     * 
     * @param player Jugador a verificar
     * @param tipo Tipo de usuario a verificar
     * @return true si el jugador tiene el tipo especificado, false en caso contrario
     */
    public boolean isTipoUsuario(Player player, TipoUsuario tipo) {
        if (player == null) return false;
        if (tipo == null) return false;
        return player.getTipoUsuario().equals(tipo);
    }

    /**
     * Verifica si un jugador tiene el tipo "Postulante".
     * 
     * @param player Jugador a verificar
     * @return true si el jugador es postulante, false en caso contrario
     */
    public boolean isPostulante(Player player) {
        if (player == null) return false;
        TipoUsuario tipoPostulante = tipoUsuarioRegistry.getPostulante();
        return isTipoUsuario(player, tipoPostulante);
    }

    /**
     * Verifica si un jugador tiene el rango "Normal".
     * 
     * @param player Jugador a verificar
     * @return true si el jugador tiene el rango normal, false en caso contrario
     */
    public boolean isNormal(Player player) {
        if (player == null) return false;
        RangoUsuario rango = rangoUsuarioRegistry.getNormal();
        return isRangoUsuario(player, rango);
    }

    /**
     * Verifica si un jugador tiene el rango "Admin".
     * 
     * @param player Jugador a verificar
     * @return true si el jugador es administrador, false en caso contrario
     */
    public boolean isAdmin(Player player) {
        if (player == null) return false;
        RangoUsuario rango = player.getRangoUsuario();
        if (rango == null) return false;
        return rango.getNombre().equalsIgnoreCase("Admin");
    }

    /**
     * Sincroniza los grupos de tipos de usuario de la base de datos con LuckPerms.
     * Crea grupos faltantes y actualiza permisos según la configuración de la base de datos.
     * 
     * @return true si la sincronización fue exitosa, false si hubo errores
     */
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

    /**
     * Sincroniza los grupos de rangos de usuario de la base de datos con LuckPerms.
     * Crea grupos faltantes y actualiza permisos según la configuración de la base de datos.
     * 
     * @return true si la sincronización fue exitosa, false si hubo errores
     */
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

    /**
     * Verifica y sincroniza el tipo de usuario de un jugador con LuckPerms.
     * Asegura que el jugador tenga el grupo correcto de tipo de usuario asignado.
     * 
     * @param player Jugador a sincronizar
     */
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

    /**
     * Verifica y sincroniza el rango de usuario de un jugador con LuckPerms.
     * Asegura que el jugador tenga el grupo correcto de rango de usuario asignado.
     * 
     * @param player Jugador a sincronizar
     */
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

    /**
     * Cierra el gestor de permisos.
     */
    public void shutdown() {
        ConsoleLogger.info(LanguageHandler.getText("permission-manager-shutting-down"));
    }

    /**
     * Cambia el tipo de usuario de un jugador y sincroniza con LuckPerms.
     * Actualiza la base de datos y los grupos en LuckPerms.
     * 
     * @param player Jugador a modificar
     * @param tipo Nuevo tipo de usuario
     * @return El jugador actualizado con el nuevo tipo de usuario
     */
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

    /**
     * Cambia el rango de usuario de un jugador y sincroniza con LuckPerms.
     * Actualiza la base de datos y los grupos en LuckPerms.
     * 
     * @param player Jugador a modificar
     * @param rango Nuevo rango de usuario
     * @return El jugador actualizado con el nuevo rango
     */
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

    /**
     * Añade a un jugador como manager de un país.
     * 
     * @param player Jugador a añadir como manager
     * @param pais País del cual será manager
     * @return El jugador actualizado
     */
    public Player addManager(Player player, Pais pais) {
        if (player == null || pais == null) return player;
        if (isManager(player, pais)) return player;

        player.addPaisManager(pais);
        return PlayerRegistry.getInstance().merge(player.getUuid());
    }

    /**
     * Remueve a un jugador como manager de un país.
     * 
     * @param player Jugador a remover como manager
     * @param pais País del cual dejará de ser manager
     * @return El jugador actualizado
     */
    public Player removeManager(Player player, Pais pais) {
        if (player == null || pais == null) return player;
        if (!isManager(player, pais)) return player;
        player.removePaisManager(pais);
        return PlayerRegistry.getInstance().merge(player.getUuid());
    }

    /**
     * Añade a un jugador como reviewer de un país.
     * 
     * @param player Jugador a añadir como reviewer
     * @param pais País del cual será reviewer
     * @return El jugador actualizado
     */
    public Player addReviewer(Player player, Pais pais) {
        if (player == null || pais == null) return player;
        if (isReviewer(player, pais)) return player;

        player.addPaisReviewer(pais);
        return PlayerRegistry.getInstance().merge(player.getUuid());
    }

    /**
     * Remueve a un jugador como reviewer de un país.
     * 
     * @param player Jugador a remover como reviewer
     * @param pais País del cual dejará de ser reviewer
     * @return El jugador actualizado
     */
    public Player removeReviewer(Player player, Pais pais) {
        if (player == null || pais == null) return player;
        if (!isReviewer(player, pais)) return player;

        player.removePaisReviewer(pais);
        return PlayerRegistry.getInstance().merge(player.getUuid());
    }

    /**
     * Obtiene la instancia singleton del gestor de permisos.
     * 
     * @return La instancia única de PermissionManager
     */
    public static PermissionManager getInstance() {
        if (instance == null) {
            instance = new PermissionManager();
        }
        return instance;
    }
}
