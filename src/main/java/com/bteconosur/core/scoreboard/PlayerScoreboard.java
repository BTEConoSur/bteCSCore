package com.bteconosur.core.scoreboard;

import java.util.List;

import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.db.model.Configuration;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.util.PlaceholderUtils;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.megavex.scoreboardlibrary.api.sidebar.component.ComponentSidebarLayout;
import net.megavex.scoreboardlibrary.api.sidebar.component.SidebarComponent;

public class PlayerScoreboard implements Scoreboard {

    @Override
    public ComponentSidebarLayout getLayout(Player context, Language language) {
        SidebarComponent title = SidebarComponent.staticLine(MiniMessage.miniMessage().deserialize(LanguageHandler.replaceMC("scoreboard-player.title", language, context)));
        SidebarComponent.Builder builder = SidebarComponent.builder();
        List<String> lines = LanguageHandler.getTextList(language,"scoreboard-player.lines");
        for (String line : lines) {
            builder.addStaticLine(MiniMessage.miniMessage().deserialize(PlaceholderUtils.replaceMC(line, language, context)));
        }
        return new ComponentSidebarLayout(title, builder.build());
    }


    @Override
    public boolean isEnabledFor(Configuration configuration) {
        return configuration.getScoreboardPlayer();
    }

}
