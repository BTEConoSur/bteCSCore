package com.bteconosur.core.command.pais;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.menu.config.PaisPrefixSelectMenu;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PlayerRegistry;

public class PaisPrefixCommand extends BaseCommand {

    public PaisPrefixCommand() {
        super("pais", "Para cambiar el prefijo del país.", "[subcomando]");
        this.addSubcommand(new PaisPrefixSetCommand());
        this.addSubcommand(new GenericHelpCommand(this));
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player player = PlayerRegistry.getInstance().get(((org.bukkit.entity.Player) sender).getUniqueId());
        PaisPrefixSelectMenu menu = new PaisPrefixSelectMenu(player);
        menu.open();
        return false;
    }

}
