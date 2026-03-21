package com.bteconosur.core.command.btecs.test;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.model.Pais;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.RangoUsuario;
import com.bteconosur.db.model.TipoUsuario;
import com.bteconosur.db.registry.PaisRegistry;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.db.registry.RangoUsuarioRegistry;
import com.bteconosur.db.registry.TipoUsuarioRegistry;
import com.bteconosur.db.util.PlaceholderUtils;

public class TestChatFormatCommand extends BaseCommand {

    public TestChatFormatCommand() {
        super("chatformat", null, "btecs.command.btecs.test", CommandMode.PLAYER_ONLY);
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        org.bukkit.entity.Player bukkitPlayer = (org.bukkit.entity.Player) sender;
        Player commandPlayer = PlayerRegistry.getInstance().get(bukkitPlayer.getUniqueId());
        Language language = commandPlayer.getLanguage();

        PlayerLogger.send(sender, "<gold>==== TEST FORMATO CHAT ====</gold>", (String) null);

        PlayerLogger.send(sender, "<yellow>Prefijos de pais (MC):</yellow>", (String) null);
        for (Pais pais : PaisRegistry.getInstance().getList()) {
            String key = "placeholder.pais-mc.prefix." + pais.getNombre().toLowerCase();
            String prefix = LanguageHandler.getText(language, key);
            PlayerLogger.send(sender, "<gray>-</gray> <white>" + pais.getNombrePublico() + "</white>: <reset>" + prefix, (String) null);
        }
        String internacional = LanguageHandler.getText(language, "placeholder.pais-mc.prefix.internacional");
        PlayerLogger.send(sender, "<gray>-</gray> <white>Internacional</white>: <reset>" + internacional, (String) null);

        PlayerLogger.send(sender, "<yellow>Rangos (badge + label):</yellow>", (String) null);
        for (RangoUsuario rango : RangoUsuarioRegistry.getInstance().getList()) {
            String badge = PlaceholderUtils.replaceMC("%rango.badge%", language, rango);
            String label = PlaceholderUtils.replaceMC("%rango.label%", language, rango);
            PlayerLogger.send(sender, "<gray>-</gray> <white>" + rango.getNombre() + "</white>: " + badge + label, (String) null);
        }

        PlayerLogger.send(sender, "<yellow>Tipos (badge + label):</yellow>", (String) null);
        for (TipoUsuario tipo : TipoUsuarioRegistry.getInstance().getList()) {
            String badge = PlaceholderUtils.replaceMC("%tipo.badge%", language, tipo);
            String label = PlaceholderUtils.replaceMC("%tipo.label%", language, tipo);
            PlayerLogger.send(sender, "<gray>-</gray> <white>" + tipo.getNombre() + "</white>: " + badge + label, (String) null);
        }

        String fakeTemplate = "%player.paisPrefix%%player.prefix% <gold>%player.nombrePublico%</gold><reset><gray>: </gray>%mensaje%";
        String fakeMessage = fakeTemplate.replace("%mensaje%", "<white>Mensaje falso de prueba en chat</white>");
        fakeMessage = PlaceholderUtils.replaceMC(fakeMessage, language, commandPlayer);

        PlayerLogger.send(sender, "<yellow>Mensaje falso:</yellow>", (String) null);
        PlayerLogger.send(sender, fakeMessage, (String) null);

        return true;
    }
}
