package com.bteconosur.core.command.chat;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.menu.chat.ChatSelectMenu;
import com.bteconosur.db.model.Player;

public class ChatCommand extends BaseCommand {

    public ChatCommand() {
        super("chat", "Para cambiar el chat.", "[subcomando]");
        this.addSubcommand(new ChatSetCommand());
        this.addSubcommand(new GenericHelpCommand(this));
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player player = Player.getBTECSPlayer((org.bukkit.entity.Player) sender);
        ChatSelectMenu menu = new ChatSelectMenu(player);
        menu.open();
        return false;
    }

}
