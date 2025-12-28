package com.bteconosur.core.command.config;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.menu.config.GeneralConfigMenu;
import com.bteconosur.db.model.Player;

public class GeneralConfigCommand extends BaseCommand {

    public GeneralConfigCommand() {
        super("config", "Abrir menú de configuración general", "[subcomando]");
        this.addSubcommand(new GenericHelpCommand(this));
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player player = Player.getBTECSPlayer((org.bukkit.entity.Player) sender);
        GeneralConfigMenu menu = new GeneralConfigMenu(player);
        menu.open();
        return true;
    }

}
