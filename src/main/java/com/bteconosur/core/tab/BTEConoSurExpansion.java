package com.bteconosur.core.tab;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import com.bteconosur.core.BTEConoSur;
import com.bteconosur.db.registry.PlayerRegistry;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;

public class BTEConoSurExpansion extends PlaceholderExpansion {

    private final BTEConoSur plugin;

    public BTEConoSurExpansion(BTEConoSur plugin) {
        this.plugin = plugin;
    }

    @Override
    @NotNull
    public String getIdentifier() {
        return "btecs"; // %btecs_currentTip%
    }

    @Override
    @NotNull
    public String getAuthor() {
        return "BTEConoSur";
    }

    @Override
    @NotNull
    public String getVersion() {
        return plugin.getPluginMeta().getVersion();
    }

    @Override
    @NotNull
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        com.bteconosur.db.model.Player dbPlayer = PlayerRegistry.getInstance().get(player.getUniqueId());
        if (params.equalsIgnoreCase("currentTip")) {
            return TabManager.getInstance().getCurrentTip(dbPlayer.getLanguage());
        }

        return null;
    }

}
