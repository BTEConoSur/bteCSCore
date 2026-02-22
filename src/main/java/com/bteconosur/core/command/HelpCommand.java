package com.bteconosur.core.command;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.core.util.TagResolverUtils;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PlayerRegistry;

import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

public class HelpCommand extends BaseCommand {

    private static List<BaseCommand> commands = new ArrayList<>();

    public HelpCommand() {
        super("help", null);
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Language language = Language.getDefault();
        if (sender instanceof org.bukkit.entity.Player) language = PlayerRegistry.getInstance().get(sender).getLanguage();
        
        int commandsPerPage = config.getInt("help-command-per-page");
        
        int visibleCommands = 0;
        for (BaseCommand cmd : commands) {
            if (cmd.getPermission() != null && !sender.hasPermission(cmd.getPermission())) {
                continue;
            }
            if (!cmd.customPermissionCheck(sender)) {
                continue;
            }
            if (!cmd.isAllowedSender(sender)) {
                continue;
            }
            visibleCommands++;
        }
        
        int totalPages = (int) Math.ceil(visibleCommands / (double) commandsPerPage);
        int page = 1;
        if (args.length > 0) {
            try {
                page = Integer.parseInt(args[0]);
                if (page < 1 || page > totalPages) page = 1;
            } catch (NumberFormatException e) {
            }
        }
        
        String pluginPrefix = LanguageHandler.getText(language, "plugin-prefix");
        String header = LanguageHandler.getText(language, "global-help-command.header")
            .replace("%plugin-prefix%", pluginPrefix);
        String info = LanguageHandler.getText(language, "global-help-command.info");
        String commandsTitle = LanguageHandler.getText(language, "global-help-command.commands-title");
        String commandLine1 = LanguageHandler.getText(language, "global-help-command.command-line-1");
        String commandLine2 = LanguageHandler.getText(language, "global-help-command.command-line-2");
        String commandLine3 = LanguageHandler.getText(language, "global-help-command.command-line-3");
        String footer = LanguageHandler.getText(language, "global-help-command.footer");
        footer = footer.replace("%currentPage%", String.valueOf(page)).replace("%totalPages%", String.valueOf(totalPages));
        TagResolver backResolver = TagResolverUtils.getCommandText("backtext", "/" + fullCommand + " " + (page - 1), LanguageHandler.getText(language, "global-help-command.backtext"), LanguageHandler.getText(language, "global-help-command.backhover"));
        TagResolver nextResolver = TagResolverUtils.getCommandText("nexttext", "/" + fullCommand + " " + (page + 1), LanguageHandler.getText(language, "global-help-command.nexttext"), LanguageHandler.getText(language, "global-help-command.nexthover"));
        if (page == 1) footer = footer.replace("<backtext>", "");
        if (page == totalPages) footer = footer.replace("<nexttext>", "");
        String message = header + "\n" + info;

        if (visibleCommands > 0) {
            message += "\n" + commandsTitle;
            
            int startIndex = (page - 1) * commandsPerPage;
            int endIndex = startIndex + commandsPerPage;
            int currentIndex = 0;
            
            for (BaseCommand cmd : commands) {
                if (cmd.getPermission() != null && !sender.hasPermission(cmd.getPermission())) {
                    continue;
                }

                if (!cmd.customPermissionCheck(sender)) {
                    continue;
                }
                
                if (!cmd.isAllowedSender(sender)) {
                    continue;
                }
                
                if (currentIndex >= startIndex && currentIndex < endIndex) {
                    String line1 = commandLine1
                        .replace("%comando%", cmd.command)
                        .replace("%args%", (cmd.args != null ? ' ' + cmd.args : (cmd.subcommands.isEmpty() ? "" : " <subcomando>")));
                    
                    message += "\n" + line1;

                    String description = cmd.getDescription(language);
                    if (description != null && !description.isEmpty()) {
                        String line2 = commandLine2.replace("%description%", description);
                        message += "\n" + line2 ;
                    }

                    if (!cmd.aliases.isEmpty()) {
                        String line3 = commandLine3.replace("%aliases%", String.join(" | ", cmd.aliases));
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

    public static void addCommand(BaseCommand command) {
        commands.add(command);
    }

}
