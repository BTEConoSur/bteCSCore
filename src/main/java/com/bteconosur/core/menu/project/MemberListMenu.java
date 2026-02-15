package com.bteconosur.core.menu.project;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.BTEConoSur;
import com.bteconosur.core.ProjectManager;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.menu.Menu;
import com.bteconosur.core.menu.PaginatedMenu;
import com.bteconosur.core.menu.player.PlayerListMenu;
import com.bteconosur.core.util.MenuUtils;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.core.util.MenuUtils.PlayerContext;
import com.bteconosur.db.PermissionManager;
import com.bteconosur.db.model.Pais;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.Proyecto;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.db.util.Estado;

import de.rapha149.signgui.SignGUIAction;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;

public class MemberListMenu extends PaginatedMenu {

    private Proyecto proyecto;
    private Player BTECSPlayer;

    private Map<Player, GuiItem> onlinePlayerItems = new HashMap<>();
    private Map<Player, GuiItem> offlinePlayerItems = new HashMap<>();
    private Set<Player> members;
    private boolean infoMenu;

    private final ConfigHandler configHandler = ConfigHandler.getInstance();
    private final YamlConfiguration lang = configHandler.getLang();
    private final YamlConfiguration config = configHandler.getConfig();

    public MemberListMenu(Player player, Proyecto proyecto, String title, Set<Player> members, Menu previousMenu, boolean infoMenu) {
        super(title, player, previousMenu);
        this.proyecto = proyecto;
        this.members = members;
        this.BTECSPlayer = player;
        this.infoMenu = infoMenu;
    }

    @Override
    protected void populateItems() {
        PlayerRegistry pr = PlayerRegistry.getInstance();
        for (Player p : members) {
            GuiItem item = MenuUtils.getPlayerItem(p, pr.isOnline(p.getUuid()), PlayerContext.MIEMBRO);
            if (pr.isOnline(p.getUuid())) onlinePlayerItems.put(p, item);
            else offlinePlayerItems.put(p, item);
            addItem(item);
        }
       
        PaginatedGui gui = getPaginatedGui();

        ProjectManager pm = ProjectManager.getInstance();
        PermissionManager permissionManager = PermissionManager.getInstance();
        Pais pais = proyecto.getPais();
        Player lider = pm.getLider(proyecto);
        if (((lider.equals(BTECSPlayer) && (proyecto.getEstado() == Estado.ACTIVO || proyecto.getEstado() == Estado.EDITANDO)) || permissionManager.isManager(BTECSPlayer, pais)) && !infoMenu) {
            if (proyecto.checkMaxMiembros()) {
                gui.setItem(rows, 3, MenuUtils.getMemberAddItem());
                gui.addSlotAction(rows, 3, action -> {
                    Set<Player> miembros = pm.getMembers(proyecto);
                    miembros.add(lider);
                    PlayerListMenu playerListMenu = new PlayerListMenu(BTECSPlayer, lang.getString("gui-titles.select-player-add").replace("%proyectoId%", proyecto.getId()), miembros, true, (player, event) -> {
                        if (permissionManager.isMiembroOrLider(player, proyecto)) {
                            String message = lang.getString("project-already-member").replace("%player%", player.getNombre()).replace("%proyectoId%", proyecto.getId());   
                            PlayerLogger.error(BTECSPlayer, message, (String) null);
                            event.getWhoClicked().closeInventory();
                            return;
                        }

                        if (permissionManager.isPostulante(player)) {
                            PlayerLogger.error(BTECSPlayer, lang.getString("cant-add-postulante"), (String) null);
                            event.getWhoClicked().closeInventory();
                            return;
                        }

                        int maxMembers = config.getInt("max-members-for-postulantes");
                        if (permissionManager.isPostulante(BTECSPlayer) && proyecto.getCantMiembros() >= maxMembers) {
                            String message = lang.getString("postulante-cant-add-member").replace("%max%", String.valueOf(maxMembers));
                            PlayerLogger.error(BTECSPlayer, message, (String) null);
                            event.getWhoClicked().closeInventory();
                            return;
                        }
                        ProjectManager.getInstance().joinProject(proyecto.getId(), player.getUuid(), BTECSPlayer.getUuid(), true);
                        String successMessage = lang.getString("project-add-member-success").replace("%player%", player.getNombre()).replace("%proyectoId%", proyecto.getId());   
                        PlayerLogger.info(BTECSPlayer, successMessage, (String) null);
                        event.getWhoClicked().closeInventory();
                    }, this);
                    playerListMenu.open();
                });
            }
            
            gui.setItem(rows, 7, MenuUtils.getMemberRemoveItem());
            gui.addSlotAction(rows, 7, action -> {
                Set<Player> miembros = pm.getMembers(proyecto);
                PlayerListMenu playerListMenu = new PlayerListMenu(BTECSPlayer, lang.getString("gui-titles.select-member-remove").replace("%proyectoId%", proyecto.getId()), miembros, false, MenuUtils.PlayerContext.MIEMBRO, (player, event) -> {
                    if (!permissionManager.isMiembroOrLider(player, proyecto)) {
                        String message = lang.getString("project-not-member").replace("%player%", player.getNombre()).replace("%proyectoId%", proyecto.getId());   
                        PlayerLogger.error(BTECSPlayer, message, (String) null);
                        event.getWhoClicked().closeInventory();
                        return;
                    }
                    ProjectManager.getInstance().removeFromProject(proyecto.getId(), player.getUuid(), BTECSPlayer.getUuid());
                    String successMessage = lang.getString("project-remove-member-success").replace("%player%", player.getNombre()).replace("%proyectoId%", proyecto.getId());   
                    PlayerLogger.info(BTECSPlayer, successMessage, (String) null);
                    event.getWhoClicked().closeInventory();
                }, this);
                playerListMenu.open();
            });
        }

        gui.setItem(rows, 8, MenuUtils.getSearchItem("Nombre", null));
        gui.addSlotAction(rows, 8, action -> {
            searchByName();
        });
    }

    private void searchByName() {  
        Boolean opened = MenuUtils.createSignGUI(player, (p, result) -> {
            String line1 = result.getLine(0);
            String line2 = result.getLine(1);
            String search = line1 + line2;
            
            if (search.isBlank()) {
                return List.of(SignGUIAction.run(() -> {
                    Bukkit.getScheduler().runTask(BTEConoSur.getInstance(), () -> {
                        removePaginatedItems();
                        onlinePlayerItems.values().forEach(this::addItem);
                        offlinePlayerItems.values().forEach(this::addItem);
                        setSearchItemsAndOpen(null);
                    });
                }));
            }
            return List.of(SignGUIAction.run(() -> {
                Bukkit.getScheduler().runTask(BTEConoSur.getInstance(), () -> {
                    removePaginatedItems();
                    
                    onlinePlayerItems.entrySet().stream()
                        .filter(entry -> entry.getKey().getNombrePublico().toLowerCase().contains(search.toLowerCase()))
                        .forEach(entry -> addItem(entry.getValue()));
                    
                    offlinePlayerItems.entrySet().stream()
                        .filter(entry -> entry.getKey().getNombrePublico().toLowerCase().contains(search.toLowerCase()))
                        .forEach(entry -> addItem(entry.getValue()));
                    
                    setSearchItemsAndOpen(search);
                });
            }));
        });
        
        YamlConfiguration lang = ConfigHandler.getInstance().getLang();
        if (!opened) PlayerLogger.error(Player.getBTECSPlayer(player), lang.getString("internal-error"), (String) null);
    }


    private void setSearchItemsAndOpen(String nombreSearch) {
        PaginatedGui gui = getPaginatedGui();
        gui.updateItem(rows, 8, MenuUtils.getSearchItem("Nombre", nombreSearch));
        gui.update();
        gui.open(player);
    }

}
