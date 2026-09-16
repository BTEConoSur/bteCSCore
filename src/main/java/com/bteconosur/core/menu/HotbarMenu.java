package com.bteconosur.core.menu;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import dev.triumphteam.gui.guis.BaseGui;

public abstract class HotbarMenu extends Menu {

    private static final Map<UUID, HotbarMenu> ACTIVE = new HashMap<>();

    protected ItemStack[] backup = new ItemStack[9];
    protected boolean open = false;
    protected final Map<Integer, Consumer<Player>> actions = new HashMap<>();

    public HotbarMenu(@NotNull com.bteconosur.db.model.Player player, @Nullable Menu previousMenu) {
        super("", 1, player, previousMenu);
    }

    public HotbarMenu(@NotNull com.bteconosur.db.model.Player player) {
        super("", 1, player);
    }

    public HotbarMenu(@NotNull Player player, @Nullable Menu previousMenu) {
        super("", 1, player, previousMenu);
    }

    public HotbarMenu(@NotNull Player player) {
        super("", 1, player);
    }

    public static HotbarMenu getActive(@NotNull Player player) {
        return ACTIVE.get(player.getUniqueId());
    }

    public static HotbarMenu getActive(@NotNull UUID playerUuid) {
        return ACTIVE.get(playerUuid);
    }
//TODO: CLick del medio
    protected abstract void setupItems();

    protected void setItem(int slot, @NotNull ItemStack item, @Nullable Consumer<Player> action) {
        player.getInventory().setItem(slot, item);
        if (action != null) actions.put(slot, action);
    }

    public void handleAction(int slot) {
        Consumer<Player> action = actions.get(slot);
        if (action != null) action.accept(player);
    }

    @Override
    protected BaseGui createGui() {
        return null;
    }

    @Override
    public void open() {
        if (player == null || !player.isOnline()) return;

        for (int i = 0; i < 9; i++) {
            backup[i] = player.getInventory().getItem(i);
            player.getInventory().setItem(i, null);
        }

        actions.clear();
        setupItems();
        open = true;
        ACTIVE.put(player.getUniqueId(), this);
    }

    public static void closeActive(@NotNull UUID playerUuid) {
        HotbarMenu activeMenu = ACTIVE.get(playerUuid);
        if (activeMenu != null) {
            activeMenu.close();
        }
    }

    public static boolean hasActive(@NotNull UUID playerUuid) {
        return ACTIVE.containsKey(playerUuid);
    }

    public void close() {
        if (!open || player == null) return;
        open = false;

        for (int i = 0; i < 9; i++) {
            player.getInventory().setItem(i, backup[i]);
        }

        actions.clear();
        ACTIVE.remove(player.getUniqueId());
    }
}
