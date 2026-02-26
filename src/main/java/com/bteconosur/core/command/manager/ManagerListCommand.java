package com.bteconosur.core.command.manager;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.menu.player.PlayerListMenu;
import com.bteconosur.core.util.MenuUtils;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.model.Pais;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PaisRegistry;
import com.bteconosur.db.registry.PlayerRegistry;

public class ManagerListCommand extends BaseCommand {

    private final Set<String> paises = PaisRegistry.getInstance().getMap().values().stream().map(Pais::getNombre).collect(Collectors.toSet());

    public ManagerListCommand() {
        super("list", "<nombre_pais>", "btecs.command.manager", CommandMode.PLAYER_ONLY);
        this.addSubcommand(new GenericHelpCommand(this));
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player player = Player.getBTECSPlayer((org.bukkit.entity.Player) sender);
        Language language = player.getLanguage();
        if (args.length != 1) {
            String message = LanguageHandler.getText(language, "help-command-usage").replace("%comando%", getFullCommand());
            PlayerLogger.info(sender, message, (String) null);
            return true;
        }

        Pais pais = PaisRegistry.getInstance().get(args[0].toLowerCase());

        if (pais == null) {
            String message = LanguageHandler.getText(language, "pais-not-found").replace("%search%", args[0]);
            PlayerLogger.error(sender, message, (String) null);
            return true;
        }
        List<Player> managers = PlayerRegistry.getInstance().getManagers(pais);
        String title = LanguageHandler.replaceMC("gui-titles.manager-list-country", language, pais);
        PlayerListMenu menu = new PlayerListMenu(player, title, Set.copyOf(managers), false, MenuUtils.PlayerContext.MANAGER, (p, e) -> {
            e.setCancelled(true);
        });
        menu.open();
        return true;
    }

    @Override
    protected List<String> tabCompleteArgs(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) return paises.stream().filter(p -> p.toLowerCase().startsWith(args[0].toLowerCase())).toList();
        return super.tabComplete(sender, alias, args);
    }

}
