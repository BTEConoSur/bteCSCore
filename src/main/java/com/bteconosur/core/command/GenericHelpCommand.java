package com.bteconosur.core.command;

import java.io.ObjectInputFilter.Config;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.config.ConfigHandler;
import com.fasterxml.jackson.databind.JsonSerializable.Base;

import net.kyori.adventure.text.minimessage.MiniMessage;

public class GenericHelpCommand extends BaseCommand {

    private final YamlConfiguration lang;
    private final BaseCommand parentCommand;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();


    public GenericHelpCommand(BaseCommand parentCommand) {
        super("help", "Muestra la ayuda del comando.", null);
        this.parentCommand = parentCommand;
        this.lang = ConfigHandler.getInstance().getLang();
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        String header = lang.getString("help-command.header")
            .replace("%command%", parentCommand.fullCommand)
            .replace("%plugin-prefix%", lang.getString("plugin-prefix"));
        String usage = lang.getString("help-command.usage");
        String descriptionLabel = lang.getString("help-command.description");
        String subcommandsTitle = lang.getString("help-command.subcommands-title");
        String subcommandLine = lang.getString("help-command.subcommand-line");
        String footer = lang.getString("help-command.footer");

        sender.sendMessage(miniMessage.deserialize(header));

        if (parentCommand.description != null && !parentCommand.description.isEmpty()) {
            descriptionLabel = descriptionLabel.replace("%description%", parentCommand.description);
            sender.sendMessage(miniMessage.deserialize(descriptionLabel));
        }
        
        usage = usage.replace("%command%", parentCommand.fullCommand).replace("%args%", parentCommand.args != null ? parentCommand.args : (parentCommand.subcommands.isEmpty() ? "" : "<subcomando>"));
        sender.sendMessage(miniMessage.deserialize(usage));
        
        if (!parentCommand.subcommands.isEmpty()) {
            sender.sendMessage(miniMessage.deserialize(subcommandsTitle));
            for (BaseCommand sub : parentCommand.subcommands.values()) {
                String subDesc = sub.description != null ? sub.description : "";
                
                String line = subcommandLine
                    .replace("%command%", parentCommand.fullCommand)
                    .replace("%subcommand%", sub.command)
                    .replace("%description%", subDesc);
                
                sender.sendMessage(miniMessage.deserialize(line));
            }
        }

        sender.sendMessage(miniMessage.deserialize(footer));
        return true;
    }
}

