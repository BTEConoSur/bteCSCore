package com.bteconosur.core.command;
import java.util.UUID;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PlayerRegistry;

public class HelpCommand extends BaseCommand {

    public HelpCommand() {
        super("help", "[pagina]", "btecs.command.help", CommandMode.BOTH);
        this.addSubcommand(new HelpCommandCommand());
        this.addSubcommand(new HelpVisitarCommand());
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        org.bukkit.entity.Player bukkitPlayer = (org.bukkit.entity.Player) sender;               
        int page = 1;
        if (args.length > 0) {
            try {
                page = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
            }
        }
        bukkitPlayer.performCommand("bteconosur:help command " + page);
        
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
