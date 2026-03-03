package com.bteconosur.core.scoreboard;

import java.util.List;

import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PaisRegistry;
import com.bteconosur.db.registry.PlayerRegistry;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.megavex.scoreboardlibrary.api.sidebar.component.ComponentSidebarLayout;
import net.megavex.scoreboardlibrary.api.sidebar.component.SidebarComponent;

public class OnlineScoreboard implements Scoreboard {

    private int onlinePlayers = 0;
    private int argentinaPlayers = 0;
    private int boliviaPlayers = 0;
    private int chilePlayers = 0;
    private int paraguayPlayers = 0;
    private int peruPlayers = 0;
    private int uruguayPlayers = 0;
    private int antartidaPlayers = 0;

    @Override
    public ComponentSidebarLayout getLayout(Player context, Language language) {
        String pluginPrefix = LanguageHandler.getText(language, "plugin-prefix");
        SidebarComponent title = SidebarComponent.staticLine(MiniMessage.miniMessage().deserialize(LanguageHandler.replaceMC("scoreboard-online.title", language, context).replace("%pluginPrefix%", pluginPrefix   )));
        SidebarComponent.Builder builder = SidebarComponent.builder();
        List<String> lines = LanguageHandler.getTextList(language,"scoreboard-online.lines");
        for (String line : lines) {
            builder.addStaticLine(MiniMessage.miniMessage().deserialize(line.replace("%onlinePlayers%", String.valueOf(onlinePlayers))
                .replace("%argentinaPlayers%", String.valueOf(argentinaPlayers))
                .replace("%boliviaPlayers%", String.valueOf(boliviaPlayers))
                .replace("%chilePlayers%", String.valueOf(chilePlayers))
                .replace("%paraguayPlayers%", String.valueOf(paraguayPlayers))
                .replace("%peruPlayers%", String.valueOf(peruPlayers))
                .replace("%uruguayPlayers%", String.valueOf(uruguayPlayers))
                .replace("%antartidaPlayers%", String.valueOf(antartidaPlayers))
            ));
        }
        return new ComponentSidebarLayout(title, builder.build());
    }

    @Override
    public void update() {
        PaisRegistry pr = PaisRegistry.getInstance();
        PlayerRegistry playerRegistry = PlayerRegistry.getInstance();
        onlinePlayers = playerRegistry.getOnlinePlayersCount();
        argentinaPlayers = playerRegistry.getOnlinePlayersCount(pr.getArgentina());
        boliviaPlayers = playerRegistry.getOnlinePlayersCount(pr.getBolivia());
        chilePlayers = playerRegistry.getOnlinePlayersCount(pr.getChile());
        paraguayPlayers = playerRegistry.getOnlinePlayersCount(pr.getParaguay());
        peruPlayers = playerRegistry.getOnlinePlayersCount(pr.getPeru());
        uruguayPlayers = playerRegistry.getOnlinePlayersCount(pr.getUruguay());
        antartidaPlayers = playerRegistry.getOnlinePlayersCount(pr.getAntartida());
    }

    @Override
    public boolean isGlobal() {
        return true;
    }

}
