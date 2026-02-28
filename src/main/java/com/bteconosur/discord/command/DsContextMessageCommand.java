package com.bteconosur.discord.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.ConsoleLogger;
import com.bteconosur.db.model.Pais;
import com.bteconosur.db.registry.PaisRegistry;
import com.bteconosur.discord.DiscordManager;
import com.bteconosur.discord.util.CommandMode;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public abstract class DsContextMessageCommand {

    protected String command;
    private Collection<Permission> permissions = new ArrayList<>();
    private CommandMode mode = CommandMode.GLOBAL;

    public DsContextMessageCommand(String command, Collection<Permission> permissions, CommandMode mode) {
        this.command = command;
        this.permissions = permissions;
        this.mode = mode;
    }

    public abstract void execute(MessageContextInteractionEvent event);

    @SuppressWarnings("null")
    public void registerCommand() {
        JDA jda = DiscordManager.getInstance().getJda();
        DefaultMemberPermissions perm = permissions != null && !permissions.isEmpty() ? DefaultMemberPermissions.enabledFor(permissions) : DefaultMemberPermissions.ENABLED;
        if (mode == CommandMode.COUNTRY_ONLY || mode == CommandMode.COUNTRY_AND_STAFFHUB) {
            PaisRegistry paisRegistry = PaisRegistry.getInstance();
            List<Pais> paises = paisRegistry.getList();
            for (Pais pais : paises) {
                Long guildId = pais.getDsIdGuild();
                if (guildId == null) continue;
                Guild guild = jda.getGuildById(guildId);
                if (guild == null) continue;

                guild.upsertCommand(Commands.message(command).setDefaultPermissions(perm)).queue();; 
            }
        }
        if (mode == CommandMode.GLOBAL) {
            jda.upsertCommand(Commands.message(command).setDefaultPermissions(perm)).queue();
        }
        if (mode == CommandMode.STAFFHUB_ONLY || mode == CommandMode.COUNTRY_AND_STAFFHUB) {
            Guild staffHubGuild = jda.getGuildById(1425856269029474304L);
            if (staffHubGuild != null) {
                staffHubGuild.upsertCommand(Commands.message(command).setDefaultPermissions(perm)).queue();
            } else {
                ConsoleLogger.warn(LanguageHandler.getText("ds-error.staffhub-not-found"));
            }
        }
    }

    public CommandMode getMode() {
        return mode;
    }

    public String getCommand() {
        return command;
    }
}
