package com.bteconosur.core.scoreboard;

import com.bteconosur.core.config.Language;
import com.bteconosur.db.model.Player;

import net.megavex.scoreboardlibrary.api.sidebar.component.ComponentSidebarLayout;

public interface Scoreboard {
    ComponentSidebarLayout getLayout(Player player, Language language);

    default boolean isRefreshable() {
        return false;
    }

    default boolean isGlobal() {
        return false;
    }

    default long getRefreshIntervalTicks() {
        return 20L;
    }

    default void update() {}

    
}
