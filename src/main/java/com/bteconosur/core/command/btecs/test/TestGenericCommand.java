package com.bteconosur.core.command.btecs.test;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;

import com.bteconosur.core.command.BaseCommand;

import dev.triumphteam.gui.guis.Gui;
import net.kyori.adventure.text.minimessage.MiniMessage;


public class TestGenericCommand extends BaseCommand {

    public TestGenericCommand() {
        super("generic", "Comando genérico de prueba", null, CommandMode.PLAYER_ONLY);
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Gui gui = Gui.gui()
                .title(MiniMessage.miniMessage().deserialize("<gradient:#FF0000:#0000FF>Comando Genérico de Prueba</gradient>"))
                .rows(3)
                .create();

        gui.open((HumanEntity) sender);
        return true;
    }



}
