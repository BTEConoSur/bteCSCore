package com.bteconosur.core.command.config;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.menu.config.ReviewerConfigMenu;
import com.bteconosur.db.model.Player;

public class ReviewerConfigCommand extends BaseCommand {
    
    public ReviewerConfigCommand() {
        super("configreviewer", "Abrir menú de configuración general", null);
        this.addSubcommand(new GenericHelpCommand(this));
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player player = Player.getBTECSPlayer((org.bukkit.entity.Player) sender);
        ReviewerConfigMenu menu = new ReviewerConfigMenu(player);
        menu.open();
        //TODO: (Futuro) Crear comando general config
        return true;
    }
}
