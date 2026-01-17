package com.bteconosur.core.command.chat;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.menu.chat.ChatSelectMenu;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PlayerRegistry;

public class ChatCommand extends BaseCommand {

    public ChatCommand() {
        super("chat", "Para cambiar el chat.", "[subcomando]");
        this.addSubcommand(new ChatSetCommand());
        this.addSubcommand(new GenericHelpCommand(this));
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player player = PlayerRegistry.getInstance().get(((org.bukkit.entity.Player) sender).getUniqueId());
        ChatSelectMenu menu = new ChatSelectMenu(player);
        menu.open();
        return false;
    }

}
