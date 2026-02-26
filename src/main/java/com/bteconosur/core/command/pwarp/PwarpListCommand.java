package com.bteconosur.core.command.pwarp;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.core.util.TagResolverUtils;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PlayerRegistry;

import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

public class PwarpListCommand extends BaseCommand {

    public PwarpListCommand() {
        super("list", "[página]", "btecs.command.pwarp", CommandMode.PLAYER_ONLY);
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player player = PlayerRegistry.getInstance().get(sender);
        Language language = player.getLanguage();
        
        List<String> pwarpNames = player.getPwarpNames();
        
        if (pwarpNames.isEmpty()) {
            PlayerLogger.info(sender, LanguageHandler.getText(language, "pwarp.empty"), (String) null);
            return true;
        }
        
        int pwarpsPerPage = config.getInt("pwarp-list-per-page", 10);
        int totalPages = (int) Math.ceil(pwarpNames.size() / (double) pwarpsPerPage);
        int page = 1;
        
        if (args.length > 0) {
            try {
                page = Integer.parseInt(args[0]);
                if (page < 1 || page > totalPages) page = 1;
            } catch (NumberFormatException e) {
                page = 1;
            }
        }
        
        String header = LanguageHandler.getText(language, "pwarp-list.header");
        String pwarpLine = LanguageHandler.getText(language, "pwarp-list.pwarp");
        String footer = LanguageHandler.getText(language, "pwarp-list.footer");
        footer = footer.replace("%currentPage%", String.valueOf(page)).replace("%totalPages%", String.valueOf(totalPages));
        
        TagResolver backResolver = TagResolverUtils.getCommandText(
            "backtext", 
            "/" + fullCommand + " " + (page - 1), 
            LanguageHandler.getText(language, "pwarp-list.backtext"), 
            LanguageHandler.getText(language, "pwarp-list.backhover")
        );
        TagResolver nextResolver = TagResolverUtils.getCommandText(
            "nexttext", 
            "/" + fullCommand + " " + (page + 1), 
            LanguageHandler.getText(language, "pwarp-list.nexttext"), 
            LanguageHandler.getText(language, "pwarp-list.nexthover")
        );
        
        if (page == 1) footer = footer.replace("<backtext>", "");
        if (page == totalPages) footer = footer.replace("<nexttext>", "");
        
        String message = header;
        
        int startIndex = (page - 1) * pwarpsPerPage;
        int endIndex = Math.min(startIndex + pwarpsPerPage, pwarpNames.size());
        List<TagResolver> tagResolvers = new ArrayList<>();
        
        for (int i = startIndex; i < endIndex; i++) {
            String pwarpName = pwarpNames.get(i);
            
            TagResolver pwarpResolver = TagResolverUtils.getCommandText(
                "pwarp-" + i, 
                "/pwarp " + pwarpName, 
                pwarpName, 
                LanguageHandler.getText(language, "pwarp-list.pwarp-hover")
            );
            tagResolvers.add(pwarpResolver);    
            message += "\n" + pwarpLine.replace("%i%", String.valueOf(i));
        }
        
        tagResolvers.add(nextResolver);
        tagResolvers.add(backResolver);
        message += "\n" + footer;
        PlayerLogger.send(sender, message, (String) null, tagResolvers.toArray(new TagResolver[0]));
        return true;
    }

    @Override
    protected boolean checkCooldown(CommandSender sender) {
        if (sender instanceof org.bukkit.entity.Player) {
            Player player = PlayerRegistry.getInstance().get(sender);
            Language language = player.getLanguage();
            UUID playerUUID = player.getUuid();
            if (timeCooldowns.containsKey(playerUUID)) {
                Long cooldown = config.getLong("pwarp-list-cooldown");
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
