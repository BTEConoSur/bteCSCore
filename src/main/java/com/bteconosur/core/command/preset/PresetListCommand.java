package com.bteconosur.core.command.preset;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.core.menu.preset.PresetListMenu;

public class PresetListCommand extends BaseCommand {

    public PresetListCommand() {
        super("list", "[página]", "btecs.command.preset", CommandMode.PLAYER_ONLY);
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player player = PlayerRegistry.getInstance().get(sender);
        if (player == null) return true;
        PresetListMenu menu = new PresetListMenu(player);
        menu.open();
        return true;
    }

}
