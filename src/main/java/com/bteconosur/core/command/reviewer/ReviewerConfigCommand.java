package com.bteconosur.core.command.reviewer;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.menu.config.ReviewerConfigMenu;
import com.bteconosur.db.PermissionManager;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PlayerRegistry;

public class ReviewerConfigCommand extends BaseCommand {
    
    public ReviewerConfigCommand() {
        super("config", "Abrir menú de configuración general", null, "btecs.command.reviewer.config", CommandMode.PLAYER_ONLY);
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

    @Override
    protected boolean customPermissionCheck(CommandSender sender) {
        Player commandPlayer = PlayerRegistry.getInstance().get(((org.bukkit.entity.Player) sender).getUniqueId());
        return PermissionManager.getInstance().isReviewer(commandPlayer);
    }

}
