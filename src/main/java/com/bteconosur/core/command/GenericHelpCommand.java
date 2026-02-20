package com.bteconosur.core.command;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PlayerRegistry;

public class GenericHelpCommand extends BaseCommand {

    private final BaseCommand parentCommand;


    public GenericHelpCommand(BaseCommand parentCommand) {
        super("help", "Muestra la ayuda del comando.", null);
        this.parentCommand = parentCommand;
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Language language = Language.getDefault();
        if (sender instanceof Player) language = PlayerRegistry.getInstance().get(sender).getLanguage();
        String header = LanguageHandler.getText(language, "help-command.header")
            .replace("%comando%", parentCommand.getFullCommand())
            .replace("%plugin-prefix%", LanguageHandler.getText(language, "plugin-prefix"));
        String usage = LanguageHandler.getText(language, "help-command.usage");
        String descriptionLabel = LanguageHandler.getText(language, "help-command.description");
        String subcommandsTitle = LanguageHandler.getText(language, "help-command.subcommands-title");
        String subcommandLine1 = LanguageHandler.getText(language, "help-command.subcommand-line-1");
        String subcommandLine2 = LanguageHandler.getText(language, "help-command.subcommand-line-2");
        String footer = LanguageHandler.getText(language, "help-command.footer");

        String message = header;

        if (parentCommand.description != null && !parentCommand.description.isEmpty()) {
            descriptionLabel = descriptionLabel.replace("%description%", parentCommand.description);
            message += "\n" + descriptionLabel;
        }
        
        usage = usage.replace("%comando%", parentCommand.getFullCommand()).replace("%args%", parentCommand.args != null ? parentCommand.args : (parentCommand.subcommands.isEmpty() ? "" : "<subcomando>"));
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
                    .replace("%comando%", parentCommand.getFullCommand())
                    .replace("%subcomando%", sub.command)
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