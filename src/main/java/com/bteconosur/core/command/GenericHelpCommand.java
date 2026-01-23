package com.bteconosur.core.command;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.util.PlayerLogger;

public class GenericHelpCommand extends BaseCommand {

    private final YamlConfiguration lang;
    private final BaseCommand parentCommand;


    public GenericHelpCommand(BaseCommand parentCommand) {
        super("help", "Muestra la ayuda del comando.", null);
        this.parentCommand = parentCommand;
        this.lang = ConfigHandler.getInstance().getLang();
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        String header = lang.getString("help-command.header")
            .replace("%command%", parentCommand.getFullCommand())
            .replace("%plugin-prefix%", lang.getString("plugin-prefix"));
        String usage = lang.getString("help-command.usage");
        String descriptionLabel = lang.getString("help-command.description");
        String subcommandsTitle = lang.getString("help-command.subcommands-title");
        String subcommandLine1 = lang.getString("help-command.subcommand-line-1");
        String subcommandLine2 = lang.getString("help-command.subcommand-line-2");
        String footer = lang.getString("help-command.footer");

        String message = header;

        if (parentCommand.description != null && !parentCommand.description.isEmpty()) {
            descriptionLabel = descriptionLabel.replace("%description%", parentCommand.description);
            message += "\n" + descriptionLabel;
        }
        
        usage = usage.replace("%command%", parentCommand.getFullCommand()).replace("%args%", parentCommand.args != null ? parentCommand.args : (parentCommand.subcommands.isEmpty() ? "" : "<subcomando>"));
        message += "\n" + usage;
        
        if (!parentCommand.subcommands.isEmpty()) {
            message += "\n" + subcommandsTitle;
            for (BaseCommand sub : parentCommand.subcommands.values()) {
                if (sub.getPermission() != null && !sender.hasPermission(sub.getPermission())) {
                    continue;
                }

                if (!sub.customPermissionCheck(sender)) {
                    continue;
                }
                
                if (!sub.isAllowedSender(sender)) {
                    continue;
                }
                
                String subDesc = sub.description != null ? sub.description : "";
                
                String line1 = subcommandLine1
                    .replace("%command%", parentCommand.getFullCommand())
                    .replace("%subcommand%", sub.command)
                    .replace("%args%", ' ' + (sub.args != null ? sub.args : (sub.subcommands.isEmpty() ? "" : " <subcomando>")));
                
                String line2 = subcommandLine2.replace("%description%", subDesc);
                
                message += "\n" + line1;
                message += "\n" + line2;
            }
        }

        message += "\n" + footer;
        PlayerLogger.send(sender, message, (String) null);
        return true;
    }
}

// TODO: Capaz es mejor añadir el uso como una opción.