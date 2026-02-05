package com.bteconosur.core.menu.project;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.BTEConoSur;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.menu.Menu;
import com.bteconosur.core.menu.PaginatedMenu;
import com.bteconosur.core.util.MenuUtils;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.model.Interaction;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.Proyecto;
import com.bteconosur.db.registry.InteractionRegistry;
import com.bteconosur.db.registry.PlayerRegistry;

import de.rapha149.signgui.SignGUIAction;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;

public class JoinRequestListMenu extends PaginatedMenu {

    private final Proyecto proyecto;
    private Player commandPlayer;
    private Map<Player, GuiItem> requestPlayerItems = new HashMap<>(); // Capaz mapearlo por id

    public JoinRequestListMenu(Player player, String title, Proyecto proyecto, Menu previousMenu) {
        super(title, player, previousMenu);
        commandPlayer = player;
        this.proyecto = proyecto;
    }

    public JoinRequestListMenu(Player player, String title, Proyecto proyecto) {
        super(title, player);
        commandPlayer = player;
        this.proyecto = proyecto;
    }

    @Override
    protected void populateItems() {
        List<Interaction> joinRequests = InteractionRegistry.getInstance().findJoinRequest(proyecto.getId());
        PlayerRegistry playerRegistry = PlayerRegistry.getInstance();

        if (joinRequests != null && !joinRequests.isEmpty()) {
            for (Interaction interaction : joinRequests) {
                Player requestPlayer = playerRegistry.get(interaction.getPlayerId());
                if (requestPlayer == null) continue;

                GuiItem item = MenuUtils.getPlayerItem(requestPlayer, playerRegistry.isOnline(requestPlayer.getUuid()), MenuUtils.PlayerContext.DEFAULT);
                item.setAction(event -> {
                    String title = ConfigHandler.getInstance().getLang().getString("gui-titles.join-request-confirmation");
                    JoinRequestConfirmationMenu confirmationMenu = new JoinRequestConfirmationMenu(commandPlayer, title, proyecto, requestPlayer, interaction.getId(), this);
                    confirmationMenu.open();
                });
                requestPlayerItems.put(requestPlayer, item);
                addItem(item);
            }
        }

        PaginatedGui gui = getPaginatedGui();

        gui.setItem(rows, 8, MenuUtils.getSearchItem("Nombre", null));
        gui.addSlotAction(rows, 8, event -> {
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
                        requestPlayerItems.values().forEach(this::addItem);
                        setSearchItemsAndOpen(null);
                    });
                }));
            }
            
            return List.of(SignGUIAction.run(() -> {
                Bukkit.getScheduler().runTask(BTEConoSur.getInstance(), () -> {
                    removePaginatedItems();
                    
                    requestPlayerItems.entrySet().stream()
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
