package com.bteconosur.core.menu.project;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.BTEConoSur;
import com.bteconosur.core.ProjectManager;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
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

    private Map<Player, GuiItem> onlinePlayerItems = new LinkedHashMap<>();
    private Map<Player, GuiItem> offlinePlayerItems = new LinkedHashMap<>();
    private Set<Player> members;
    private boolean infoMenu;
    private Language language;

    private final ConfigHandler configHandler = ConfigHandler.getInstance();
    private final YamlConfiguration config = configHandler.getConfig();

    public MemberListMenu(Player player, Proyecto proyecto, String title, Set<Player> members, Menu previousMenu, boolean infoMenu) {
        super(title, player, previousMenu);
        this.proyecto = proyecto;
        this.members = members;
        this.BTECSPlayer = player;
        this.infoMenu = infoMenu;
        this.language = player.getLanguage();
    }

    @Override
    protected void populateItems() {
        PlayerRegistry pr = PlayerRegistry.getInstance();
        for (Player p : members) {
            GuiItem item = MenuUtils.getPlayerItem(p, pr.isOnline(p.getUuid()), PlayerContext.MIEMBRO, language);
            if (pr.isOnline(p.getUuid())) onlinePlayerItems.put(p, item);
            else offlinePlayerItems.put(p, item);
            addItem(item);
        }
       
        PaginatedGui gui = getPaginatedGui();

        ProjectManager pm = ProjectManager.getInstance();
        PermissionManager permissionManager = PermissionManager.getInstance();
        Pais pais = proyecto.getPais();
        Player lider = pm.getLider(proyecto);
        if (((BTECSPlayer.equals(lider) && (proyecto.getEstado() == Estado.ACTIVO || proyecto.getEstado() == Estado.EDITANDO)) || permissionManager.isManager(BTECSPlayer, pais)) && !infoMenu) {
            if (proyecto.checkMaxMiembros()) {
                gui.setItem(rows, 3, MenuUtils.getMemberAddItem(language));
                gui.addSlotAction(rows, 3, action -> {
                    Set<Player> miembros = pm.getMembers(proyecto);
                    if (lider != null) miembros.add(lider);
                    String title = LanguageHandler.replaceMC("gui-titles.select-player-add", language, proyecto);
                    PlayerListMenu playerListMenu = new PlayerListMenu(BTECSPlayer, title, miembros, true, (player, event) -> {
                        event.getWhoClicked().closeInventory();
                        if (permissionManager.isMiembroOrLider(player, proyecto)) {
                            String message = LanguageHandler.replaceMC("project.member.already", language, player, proyecto);   
                            PlayerLogger.error(BTECSPlayer, message, (String) null);
                            return;
                        }

                        if (permissionManager.isPostulante(player)) {
                            String message = LanguageHandler.getText(language, "project.member.add.cant-add-postulante");
                            PlayerLogger.error(BTECSPlayer, message, (String) null);
                            return;
                        }

                        int maxMembers = config.getInt("max-members-for-postulantes");
                        if (permissionManager.isPostulante(BTECSPlayer) && proyecto.getCantMiembros() >= maxMembers) {
                            String message = LanguageHandler.getText(language, "project.member.add.postulante-max-reached").replace("%max%", String.valueOf(maxMembers));
                            PlayerLogger.error(BTECSPlayer, message, (String) null);
                            return;
                        }
                        ProjectManager.getInstance().joinProject(proyecto.getId(), player.getUuid(), BTECSPlayer.getUuid(), true);
                        String successMessage = LanguageHandler.replaceMC("project.member.add.success", language, player, proyecto);   
                        PlayerLogger.info(BTECSPlayer, successMessage, (String) null);
                    }, this);
                    playerListMenu.open();
                });
            }
            
            gui.setItem(rows, 7, MenuUtils.getMemberRemoveItem(language));
            gui.addSlotAction(rows, 7, action -> {
                Set<Player> miembros = pm.getMembers(proyecto);
                String title = LanguageHandler.replaceMC("gui-titles.select-member-remove", language, proyecto);
                PlayerListMenu playerListMenu = new PlayerListMenu(BTECSPlayer, title, miembros, false, MenuUtils.PlayerContext.MIEMBRO, (player, event) -> {
                    event.getWhoClicked().closeInventory();
                    if (!permissionManager.isMiembroOrLider(player, proyecto)) {
                        String message = LanguageHandler.replaceMC("project.member.not-member", language, player, proyecto);   
                        PlayerLogger.error(BTECSPlayer, message, (String) null);
                        return;
                    }
                    ProjectManager.getInstance().removeFromProject(proyecto.getId(), player.getUuid(), BTECSPlayer.getUuid());
                    String successMessage = LanguageHandler.replaceMC("project.member.remove.success", language, player, proyecto);   
                    PlayerLogger.info(BTECSPlayer, successMessage, (String) null);
                }, this);
                playerListMenu.open();
            });
        }

        gui.setItem(rows, 8, MenuUtils.getSearchItem(LanguageHandler.getText(language, "placeholder.item-mc.search-term.nombre"), null, language));
        gui.addSlotAction(rows, 8, action -> {
            searchByName();
        });

        gui.setItem(rows, 5, MenuUtils.getPlayerSearchInfo(language));
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
                        .filter(entry -> entry.getKey().getNombre().toLowerCase().contains(search.toLowerCase()))
                        .forEach(entry -> addItem(entry.getValue()));
                    
                    offlinePlayerItems.entrySet().stream()
                        .filter(entry -> entry.getKey().getNombre().toLowerCase().contains(search.toLowerCase()))
                        .forEach(entry -> addItem(entry.getValue()));
                    
                    setSearchItemsAndOpen(search);
                });
            }));
        }, language);

        if (!opened) PlayerLogger.error(Player.getBTECSPlayer(player), LanguageHandler.getText(language, "internal-error"), (String) null);
    }


    private void setSearchItemsAndOpen(String nombreSearch) {
        PaginatedGui gui = getPaginatedGui();
        gui.updateItem(rows, 8, MenuUtils.getSearchItem(LanguageHandler.getText(language, "placeholder.item-mc.search-term.nombre"), nombreSearch, language));
        gui.update();
        gui.open(player);
    }

}
