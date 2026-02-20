package com.bteconosur.core.command.pais;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.model.Division;
import com.bteconosur.db.model.Pais;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PaisRegistry;
import com.bteconosur.db.registry.PlayerRegistry;

public class WhereIAmCommand extends BaseCommand {

    public WhereIAmCommand() {
        super("whereiam", "Mostrar el país y ciudad en el que estás.", null, CommandMode.PLAYER_ONLY);
    }

    @Override
    protected boolean onCommand(org.bukkit.command.CommandSender sender, String[] args) {
        org.bukkit.entity.Player player = (org.bukkit.entity.Player) sender;
        Double x = player.getLocation().getX();
        Double z = player.getLocation().getZ();
        Player commandPlayer = PlayerRegistry.getInstance().get(sender);
        Language language = commandPlayer.getLanguage();
        Pais pais = PaisRegistry.getInstance().findByLocation(x, z);
        if (pais == null) {
            PlayerLogger.info(sender, LanguageHandler.getText(language, "where.no-pais"), (String) null);
            return true;
        }

        Division division = PaisRegistry.getInstance().findDivisionByLocation(x, z, pais);
        if (division == null) {
            PlayerLogger.info(sender, LanguageHandler.replaceMC("where.no-division", language, pais), (String) null);
            return true;
        }

        String message = LanguageHandler.replaceMC("where.division", language, division); 
        PlayerLogger.info(sender, message, (String) null);
        return true;
    }

}
