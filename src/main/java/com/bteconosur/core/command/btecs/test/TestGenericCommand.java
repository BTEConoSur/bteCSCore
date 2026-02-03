package com.bteconosur.core.command.btecs.test;

import java.util.Collections;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.util.ConsoleLogger;

import de.rapha149.signgui.SignGUI;
import de.rapha149.signgui.SignGUIAction;
import de.rapha149.signgui.exception.SignGUIVersionException;


public class TestGenericCommand extends BaseCommand {

    public TestGenericCommand() {
        super("generic", "Comando genérico de prueba", null, CommandMode.PLAYER_ONLY);
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        
        SignGUI gui;
        try {
            gui = SignGUI.builder()
                    .setLines("","","^^^^^^^^^", "Escribe arriba")
                    .setHandler((p, result) -> {
                        String input1 = result.getLineWithoutColor(0);
                        String input2 = result.getLineWithoutColor(1);
                        player.sendMessage("§aEscribiste: §f" + input1 + input2);
                        return Collections.singletonList(SignGUIAction.run(() -> player.sendMessage("§eSign GUI cerrado")));
                    })
                    .build();
                gui.open(player);
        } catch (SignGUIVersionException e) {
            ConsoleLogger.error("Error al crear el SignGUI: " + e.getMessage());
            e.printStackTrace();
        }
        
        return true;
    }



}