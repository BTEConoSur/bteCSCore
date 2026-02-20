package com.bteconosur.discord.command;

import java.util.Collection;

import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.ConsoleLogger;

import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public abstract class DsSubcommand extends DsCommand {

    public DsSubcommand(String command, String description, Collection<OptionData> options) {
        super(command, description, options, null, null);
    }

    @SuppressWarnings("null")
    public SubcommandData geSubcommandData() {
        SubcommandData subcommandData = new SubcommandData(command, description);
        if (options != null && !options.isEmpty()) subcommandData.addOptions(options);
        return subcommandData;
    }

    @Override
    public void registerCommand() {
        ConsoleLogger.warn(LanguageHandler.getText("ds-error.subcommand-cant-register").replace("%subcommand%", command)    );
    }

}
