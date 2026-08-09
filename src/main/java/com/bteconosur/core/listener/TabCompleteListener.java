package com.bteconosur.core.listener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.db.model.Preset;
import com.bteconosur.db.registry.PlayerRegistry;
import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;

public class TabCompleteListener implements Listener {

    @EventHandler
    public void onAsyncTabComplete(AsyncTabCompleteEvent event) {
        if (!(event.getSender() instanceof Player bukkitPlayer)) return;

        String buffer = event.getBuffer();
        
        if (!buffer.startsWith("/")) return;

        String[] args = buffer.split(" ", -1);
        String currentArg = args[args.length - 1];

        String presetSymbol = ConfigHandler.getInstance().getConfig().getString("preset.symbol", "$");

        int lastCommaIndex = currentArg.lastIndexOf(",");
        String prefixBeforeComma = lastCommaIndex == -1 ? "" : currentArg.substring(0, lastCommaIndex + 1);
        String currentlyTyping = currentArg.substring(lastCommaIndex + 1);

        if (currentlyTyping.startsWith(presetSymbol)) {
            if (currentlyTyping.length() > presetSymbol.length() && currentlyTyping.endsWith(presetSymbol)) return;

            String typedName = currentlyTyping.substring(presetSymbol.length()).toLowerCase();

            com.bteconosur.db.model.Player commandPlayer = PlayerRegistry.getInstance().get(bukkitPlayer.getUniqueId());
            if (commandPlayer == null) return;

            List<String> suggestions = new ArrayList<>();

            for (Preset preset : commandPlayer.getPresets()) {
                String presetName = preset.getId().getNombre();
                
                if (presetName.toLowerCase().startsWith(typedName)) {
                    suggestions.add(prefixBeforeComma + presetSymbol + presetName + presetSymbol);
                }
            }

            if (!suggestions.isEmpty()) {
                Collections.sort(suggestions);
                event.setCompletions(suggestions);
                event.setHandled(true); 
            }
        }
    }

}
