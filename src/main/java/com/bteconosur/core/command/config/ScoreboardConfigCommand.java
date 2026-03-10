package com.bteconosur.core.command.config;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.menu.config.ScoreboardConfigMenu;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PlayerRegistry;

public class ScoreboardConfigCommand extends BaseCommand {

    public ScoreboardConfigCommand() {
        super("scoreboard", "", "btecs.command.scoreboard", CommandMode.PLAYER_ONLY);
        this.addSubcommand(new GenericHelpCommand(this));
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player player = PlayerRegistry.getInstance().get(((org.bukkit.entity.Player) sender).getUniqueId());
        ScoreboardConfigMenu menu = new ScoreboardConfigMenu(player);
        menu.open();
        return false;
    }

}
