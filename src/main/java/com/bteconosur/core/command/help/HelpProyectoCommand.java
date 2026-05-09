package com.bteconosur.core.command.help;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.TipoProyecto;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.db.registry.TipoProyectoRegistry;

public class HelpProyectoCommand extends BaseCommand {

    public HelpProyectoCommand() {
        super("proyecto", "", "btecs.command.help", CommandMode.BOTH);
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Language language = Language.getDefault();
        if (sender instanceof org.bukkit.entity.Player) {
            Player commandPlayer = PlayerRegistry.getInstance().get(sender);
            language = commandPlayer.getLanguage();
        }

        List<TipoProyecto> tiposProyecto = new ArrayList<>(TipoProyectoRegistry.getInstance().getList());
        tiposProyecto.sort(Comparator.comparing(TipoProyecto::getId, Comparator.nullsLast(Long::compareTo)));
        String pluginPrefix = LanguageHandler.getText(language, "plugin-prefix");
        String footer = LanguageHandler.getText(language, "help-proyecto.footer").replace("%plugin-prefix%", pluginPrefix);

        StringBuilder message = new StringBuilder();
        message.append(LanguageHandler.getText(language, "help-proyecto.header"));
        message.append("\n").append(LanguageHandler.getText(language, "help-proyecto.intro"));
        message.append("\n\n").append(LanguageHandler.getText(language, "help-proyecto.title"));

        if (tiposProyecto.isEmpty()) {
            message.append("\n").append(LanguageHandler.getText(language, "help-proyecto.empty"));
            message.append("\n").append(footer);
            PlayerLogger.send(sender, message.toString(), (String) null);
            return true;
        }

        for (TipoProyecto tipoProyecto : tiposProyecto) {
            message.append("\n").append(LanguageHandler.replaceMC("help-proyecto.line", language, tipoProyecto));
        }

        message.append("\n").append(footer);

        PlayerLogger.send(sender, message.toString(), (String) null);
        return true;
    }
}
