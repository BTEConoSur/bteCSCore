package com.bteconosur.core.menu.player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import com.bteconosur.core.BTEConoSur;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.menu.Menu;
import com.bteconosur.core.menu.PaginatedMenu;
import com.bteconosur.core.util.MenuUtils;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PlayerRegistry;

import de.rapha149.signgui.SignGUIAction;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;

public class PlayerListMenu extends PaginatedMenu {

    private Map<Player, GuiItem> onlinePlayerItems = new HashMap<>();
    private Map<Player, GuiItem> offlinePlayerItems = new HashMap<>();
    private Set<Player> searchPlayers;
    private Set<Player> excludedPlayers;
    private final boolean excludePlayers;
    private final BiConsumer<Player, InventoryClickEvent> onClick;
    private final MenuUtils.PlayerContext context;
    private Language language;

    public PlayerListMenu(Player player, String title, Set<Player> players, boolean excludePlayers, MenuUtils.PlayerContext context, @NotNull BiConsumer<Player, InventoryClickEvent> onClick, Menu previousMenu) {
        super(title, player, previousMenu);
        this.onClick = onClick;
        this.excludedPlayers = excludePlayers ? players : Set.of();
        this.searchPlayers = excludePlayers ? Set.of() : players;
        this.excludePlayers = excludePlayers;
        this.context = context;
    }

    public PlayerListMenu(Player player, String title, Set<Player> players, boolean excludePlayers, MenuUtils.PlayerContext context, @NotNull BiConsumer<Player, InventoryClickEvent> onClick) {
        super(title, player);
        this.onClick = onClick;
        this.excludedPlayers = excludePlayers ? players : Set.of();
        this.searchPlayers = excludePlayers ? Set.of() : players;
        this.excludePlayers = excludePlayers;
        this.context = context;
    }

    public PlayerListMenu(Player player, String title, Set<Player> players, boolean excludePlayers, @NotNull BiConsumer<Player, InventoryClickEvent> onClick, Menu previousMenu) {
        super(title, player, previousMenu);
        this.onClick = onClick;
        this.excludedPlayers = excludePlayers ? players : Set.of();
        this.searchPlayers = excludePlayers ? Set.of() : players;
        this.excludePlayers = excludePlayers;
        this.context = MenuUtils.PlayerContext.DEFAULT;
    }

    public PlayerListMenu(Player player, String title, Set<Player> players, boolean excludePlayers, @NotNull BiConsumer<Player, InventoryClickEvent> onClick) {
        super(title, player);
        this.onClick = onClick;
        this.excludedPlayers = excludePlayers ? players : Set.of();
        this.searchPlayers = excludePlayers ? Set.of() : players;
        this.excludePlayers = excludePlayers;
        this.context = MenuUtils.PlayerContext.DEFAULT;
    }

    public PlayerListMenu(Player player, String title, @NotNull BiConsumer<Player, InventoryClickEvent> onClick, Menu previousMenu) {
        super(title, player, previousMenu);
        this.onClick = onClick;
        this.excludedPlayers = Set.of();
        this.searchPlayers = Set.of();
        this.excludePlayers = true;
        this.context = MenuUtils.PlayerContext.DEFAULT;
    }

    public PlayerListMenu(Player player, String title, @NotNull BiConsumer<Player, InventoryClickEvent> onClick) {
        super(title, player);
        this.onClick = onClick;
        this.excludedPlayers = Set.of();
        this.searchPlayers = Set.of();
        this.excludePlayers = true;
        this.context = MenuUtils.PlayerContext.DEFAULT;
    }

//TODO: verificar con muchos jugadores;
    @Override
    protected void populateItems() {
        PlayerRegistry pr = PlayerRegistry.getInstance();
        language = Player.getBTECSPlayer(player).getLanguage();
        if (!excludePlayers) {
            for (Player p : searchPlayers) {
                GuiItem item = MenuUtils.getPlayerItem(p, pr.isOnline(p.getUuid()), context, language);
                item.setAction(event -> onClick.accept(p, event));
                if (pr.isOnline(p.getUuid())) onlinePlayerItems.put(p, item);
                else offlinePlayerItems.put(p, item);
                addItem(item);
            }
        } else {
            List<Player> onlinePlayers = pr.getOnlinePlayers();
            List<Player> offlinePlayers = pr.getOfflinePlayers();

            for (Player p : onlinePlayers) {
                if (excludedPlayers.contains(p)) continue;
                GuiItem item = MenuUtils.getPlayerItem(p, true, context, language);
                item.setAction(event -> onClick.accept(p, event));
                onlinePlayerItems.put(p, item);
                addItem(item);
            }

            for (Player p : offlinePlayers) {
                if (excludedPlayers.contains(p)) continue;
                GuiItem item = MenuUtils.getPlayerItem(p, false, context, language);
                item.setAction(event -> onClick.accept(p, event));
                offlinePlayerItems.put(p, item);
                addItem(item);
            }
        }
        PaginatedGui gui = getPaginatedGui();

        gui.setItem(rows, 8, MenuUtils.getSearchItem(LanguageHandler.getText(language, "placeholder.item-mc.search-term.nombre"), null, language));
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
