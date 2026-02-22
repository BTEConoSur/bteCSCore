package com.bteconosur.core.command;

import java.util.UUID;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.core.util.TagResolverUtils;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PlayerRegistry;

import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

public class GenericHelpCommand extends BaseCommand {

    private final BaseCommand parentCommand;

    public GenericHelpCommand(BaseCommand parentCommand) {
        super("help", null);
        this.parentCommand = parentCommand;
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Language language = Language.getDefault();
        if (sender instanceof org.bukkit.entity.Player) language = PlayerRegistry.getInstance().get(sender).getLanguage();
        
        int subcommandsPerPage = parentCommand.config.getInt("help-command-per-page");
        
        // Count accessible subcommands
        int visibleSubcommands = 0;
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
            visibleSubcommands++;
        }
        
        int totalPages = (int) Math.ceil(visibleSubcommands / (double) subcommandsPerPage);
        int page = 1;
        if (args.length > 0) {
            try {
                page = Integer.parseInt(args[0]);
                if (page < 1 || page > totalPages) page = 1;
            } catch (NumberFormatException e) {
            }
        }
        
        String header = LanguageHandler.getText(language, "help-command.header")
            .replace("%comando%", parentCommand.getFullCommand())
            .replace("%plugin-prefix%", LanguageHandler.getText(language, "plugin-prefix"));
        String info = LanguageHandler.getText(language, "help-command.info");
        String usage = LanguageHandler.getText(language, "help-command.usage");
        String aliases = LanguageHandler.getText(language, "help-command.aliases");
        String descriptionLabel = LanguageHandler.getText(language, "help-command.description");
        String subcommandsTitle = LanguageHandler.getText(language, "help-command.subcommands-title");
        String subcommandLine1 = LanguageHandler.getText(language, "help-command.subcommand-line-1");
        String subcommandLine2 = LanguageHandler.getText(language, "help-command.subcommand-line-2");
        String subcommandLine3 = LanguageHandler.getText(language, "help-command.subcommand-line-3");
        String footer = LanguageHandler.getText(language, "help-command.footer");
        footer = footer.replace("%currentPage%", String.valueOf(page)).replace("%totalPages%", String.valueOf(totalPages));
        TagResolver backResolver = TagResolverUtils.getCommandText("backtext", "/" + fullCommand + " " + (page - 1), LanguageHandler.getText(language, "help-command.backtext"), LanguageHandler.getText(language, "help-command.backhover"));
        TagResolver nextResolver = TagResolverUtils.getCommandText("nexttext", "/" + fullCommand + " " + (page + 1), LanguageHandler.getText(language, "help-command.nexttext"), LanguageHandler.getText(language, "help-command.nexthover"));
        if (page == 1) footer = footer.replace("<backtext>", "");
        if (page == totalPages) footer = footer.replace("<nexttext>", "");
        String message = header + "\n" + info;

        if (parentCommand.getDescription(language) != null && !parentCommand.getDescription(language).isEmpty()) {
            descriptionLabel = descriptionLabel.replace("%description%", parentCommand.getDescription(language));
            message += "\n" + descriptionLabel;
        }
        
        usage = usage.replace("%comando%", parentCommand.getFullCommand()).replace("%args%", parentCommand.args != null ? parentCommand.args : (parentCommand.subcommands.isEmpty() ? "" : "<subcomando>"));
        message += "\n" + usage;
        if (!parentCommand.aliases.isEmpty()) {
            aliases = aliases.replace("%aliases%", String.join(" | ", parentCommand.aliases));
            message += "\n" + aliases;
        }
        if (!parentCommand.subcommands.isEmpty() && visibleSubcommands > 0) {
            message += "\n" + subcommandsTitle;
            
            int startIndex = (page - 1) * subcommandsPerPage;
            int endIndex = startIndex + subcommandsPerPage;
            int currentIndex = 0;
            
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
                
                if (currentIndex >= startIndex && currentIndex < endIndex) {
                    String line1 = subcommandLine1
                        .replace("%comando%", parentCommand.getFullCommand())
                        .replace("%subcomando%", sub.command)
                        .replace("%args%", (sub.args != null ? ' ' + sub.args : (sub.subcommands.isEmpty() ? "" : " <subcomando>")));
                    
                    message += "\n" + line1;

                    String description = sub.getDescription(language);
                    if (description != null && !description.isEmpty()) {
                        String line2 = subcommandLine2.replace("%description%", description);
                        message += "\n" + line2 ;
                    }

                    if (!sub.aliases.isEmpty()) {
                        String line3 = subcommandLine3.replace("%aliases%", String.join(" | ", sub.aliases));
                        message += "\n" + line3;
                    }
                }
                
                currentIndex++;
                if (currentIndex >= endIndex) break;
            }
        }

        message += "\n" + footer;
        PlayerLogger.send(sender, message, (String) null, backResolver, nextResolver);
        return true;
    }

    @Override
    protected boolean checkCooldown(CommandSender sender) {
        if (sender instanceof Player player) {
            Language language = PlayerRegistry.getInstance().get(sender).getLanguage();
            UUID playerUUID = player.getUuid();
            if (timeCooldowns.containsKey(playerUUID)) {
                Long cooldown = config.getLong("help-command-cooldown");
                Long playerCooldown = timeCooldowns.get(playerUUID);
                Long actualCooldown = System.currentTimeMillis() - playerCooldown;
                if (actualCooldown < (cooldown * 1000)) {
                    long remainingMillis = cooldown * 1000 - actualCooldown;
                    String formattedTime = formatTime(remainingMillis, language);
                    String message = LanguageHandler.getText(language, "command-on-cooldown")
                            .replace("%time%", formattedTime);
                    PlayerLogger.warn(sender, message, (String) null);
                    return false; 
                } else {
                    timeCooldowns.put(playerUUID, System.currentTimeMillis());
                }
            } else {
                timeCooldowns.put(playerUUID, System.currentTimeMillis());
            }
        }   
        return true;
    }
}

// TODO: Capaz es mejor añadir el uso como una opción.