package com.bteconosur.core.scoreboard;

import java.util.List;
import java.util.Set;

import org.bukkit.Location;

import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.db.model.Configuration;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.Proyecto;
import com.bteconosur.db.registry.ProyectoRegistry;
import com.bteconosur.db.util.PlaceholderUtils;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.megavex.scoreboardlibrary.api.sidebar.component.ComponentSidebarLayout;
import net.megavex.scoreboardlibrary.api.sidebar.component.SidebarComponent;

public class ProyectoScoreboard implements Scoreboard {

    @Override
    public ComponentSidebarLayout getLayout(Player player, Language language) {
        org.bukkit.entity.Player bukkitPlayer = player.getBukkitPlayer();
        Location location = bukkitPlayer.getLocation();
        Set<Proyecto> proyectos = ProyectoRegistry.getInstance().getByLocation(location.blockX(), location.blockZ());
        if (proyectos.size() > 1) {
            String line = LanguageHandler.getText(language, "scoreboard-multiple-proyectos.lines");
            SidebarComponent title = SidebarComponent.staticLine(MiniMessage.miniMessage().deserialize(LanguageHandler.getText(language, "scoreboard-multiple-proyectos.title")));
            SidebarComponent.Builder builder = SidebarComponent.builder();
            for (Proyecto proyecto : proyectos) {
                builder.addStaticLine(MiniMessage.miniMessage().deserialize(PlaceholderUtils.replaceMC(line, language, proyecto)));
            }
            return new ComponentSidebarLayout(title, builder.build());
        }
        Proyecto proyecto = proyectos.isEmpty() ? null : proyectos.iterator().next();
        String path = proyecto == null ? "scoreboard-no-proyecto." : "scoreboard-proyecto.";
        SidebarComponent title = SidebarComponent.staticLine(MiniMessage.miniMessage().deserialize(LanguageHandler.replaceMC(path + "title", language, proyecto)));
        SidebarComponent.Builder builder = SidebarComponent.builder();
        List<String> lines = LanguageHandler.getTextList(language,path + "lines");
        for (String line : lines) {
            builder.addStaticLine(MiniMessage.miniMessage().deserialize(PlaceholderUtils.replaceMC(line, language, proyecto)));
        }
        return new ComponentSidebarLayout(title, builder.build());
    }

    @Override
    public boolean isRefreshable() {
        return true;
    }

    @Override
    public long getRefreshIntervalTicks() {
        return ConfigHandler.getInstance().getConfig().getInt("proyecto-scoreboard-refresh") * 20L;
    }

    @Override
    public boolean isEnabledFor(Configuration configuration) {
        return configuration.getScoreboardProyecto();
    }
}
