package com.bteconosur.core.command;

import java.util.List;

import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.menu.TpdirMenu;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.core.util.json.JsonUtils;
import com.bteconosur.core.util.json.RealLocation;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PlayerRegistry;

public class TpdirCommand extends BaseCommand {

    public TpdirCommand() {
        super("tpdir", "<búsqueda>", "btecs.command.tpdir", CommandMode.PLAYER_ONLY);
        this.addSubcommand(new GenericHelpCommand(this));
    }

    @Override
    protected boolean onCommand(org.bukkit.command.CommandSender sender, String[] args) {
        org.bukkit.entity.Player bukkitPlayer = (org.bukkit.entity.Player) sender;
        Player player = PlayerRegistry.getInstance().get(sender);
        if (args.length < 1) {
            String message = LanguageHandler.getText(player.getLanguage(), "help-command-usage").replace("%comando%", getFullCommand());
            PlayerLogger.info(sender, message, (String) null);
            return true;
        }

        String query = String.join("+", args);
        List<RealLocation> locations = JsonUtils.buscar(query);
        if (locations.isEmpty()) {
            PlayerLogger.error(sender, LanguageHandler.getText(player.getLanguage(), "tpdir.no-results").replace("%query%", query), (String) null);
            return true;
        }
        if (locations.size() == 1) {
            RealLocation loc = locations.get(0);
            PlayerLogger.info(player, LanguageHandler.getText(player.getLanguage(), "tpdir.tped").replace("%destino%", loc.displayName), (String) null);
            bukkitPlayer.performCommand("terraplusminus:tpll " + loc.lat + " " + loc.lon);
            return true;
        } else {
            TpdirMenu menu = new TpdirMenu(player, LanguageHandler.getText(player.getLanguage(), "gui-titles.tpdir-selection"), locations);
            menu.open();
        }
        
        return true;
    }

}
