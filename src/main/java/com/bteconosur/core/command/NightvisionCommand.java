package com.bteconosur.core.command;

import org.bukkit.command.CommandSender;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.model.Player;

public class NightvisionCommand extends BaseCommand {

    public NightvisionCommand() {
        super("nightvision", "");
        this.addSubcommand(new GenericHelpCommand(this));
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player player = Player.getBTECSPlayer((org.bukkit.entity.Player) sender);
        org.bukkit.entity.Player bukkitPlayer = (org.bukkit.entity.Player) sender;
        if (bukkitPlayer.hasPotionEffect(PotionEffectType.NIGHT_VISION)) {
            bukkitPlayer.removePotionEffect(PotionEffectType.NIGHT_VISION);
            PlayerLogger.info(player, LanguageHandler.getText(player.getLanguage(), "nightvision.disabled"), (String) null);
        } else {
            bukkitPlayer.addPotionEffect(new PotionEffect(
                PotionEffectType.NIGHT_VISION,
                Integer.MAX_VALUE,
                0,
                true,
                false
            ));
            PlayerLogger.info(player, LanguageHandler.getText(player.getLanguage(), "nightvision.enabled"), (String) null);
        }
        
        return true;
    }

}
