package com.bteconosur.core.command.pais;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.model.Division;
import com.bteconosur.db.model.Pais;
import com.bteconosur.db.registry.PaisRegistry;

public class WhereIAmCommand extends BaseCommand {

    private final YamlConfiguration lang;

    public WhereIAmCommand() {
        super("whereiam", "Mostrar el país y ciudad en el que estás.", null, CommandMode.PLAYER_ONLY);
        lang = ConfigHandler.getInstance().getLang();
    }

    @Override
    protected boolean onCommand(org.bukkit.command.CommandSender sender, String[] args) {
        Player player = (Player) sender;
        Double x = player.getLocation().getX();
        Double z = player.getLocation().getZ();

        Pais pais = PaisRegistry.getInstance().findByLocation(x, z);
        if (pais == null) {
            PlayerLogger.info(sender, lang.getString("where-no-pais"), (String) null);
            return true;
        }

        Division division = PaisRegistry.getInstance().findDivisionByLocation(x, z, pais);
        if (division == null) {
            PlayerLogger.info(sender, lang.getString("where-no-division").replace("%pais%", pais.getNombre()), (String) null);
            return true;
        }

        String message = lang.getString("where-division")
                .replace("%pais%", pais.getNombrePublico())
                .replace("%divisionGna%", division.getGna())
                .replace("%divisionContext%", division.getContexto())
                .replace("%divisionNam%", division.getNam())
                .replace("%divisionFna%", division.getFna());

        PlayerLogger.info(sender, message, (String) null);
        return true;
    }

}
