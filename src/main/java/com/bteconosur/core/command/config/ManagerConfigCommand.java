package com.bteconosur.core.command.config;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.menu.config.ManagerConfigMenu;
import com.bteconosur.db.model.Player;

public class ManagerConfigCommand extends BaseCommand {

    public ManagerConfigCommand() {
        super("configmanager", "Abrir menú de configuración del manager", null);
        this.addSubcommand(new GenericHelpCommand(this));
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player player = Player.getBTECSPlayer((org.bukkit.entity.Player) sender);
        ManagerConfigMenu menu = new ManagerConfigMenu(player);
        menu.open();
        return true;
    }

}
