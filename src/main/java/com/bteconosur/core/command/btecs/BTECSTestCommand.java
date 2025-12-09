package com.bteconosur.core.command.btecs;

import java.time.Duration;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.ConfigHandler;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.Title.Times;

public class BTECSTestCommand extends BaseCommand {

    public BTECSTestCommand() {
        super("test", CommandMode.PLAYER_ONLY);
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Audience audience = (Audience) sender;
        audience.showTitle(Title.title(Component.text("Este es un título de prueba"), 
            MiniMessage.miniMessage().deserialize("<green>Este es un subtítulo de prueba</green>"), 
            Times.times(Duration.ofMillis(0), Duration.ofMillis(1000), Duration.ofMillis(0))
        ));
        return true;
    }
}
