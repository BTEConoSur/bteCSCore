package com.bteconosur.discord.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.discord.util.CommandMode;
import com.bteconosur.db.model.Pais;
import com.bteconosur.db.registry.PaisRegistry;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.util.ConsoleLogger;
import com.bteconosur.discord.DiscordManager;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.restaction.CommandCreateAction;

public abstract class DsCommand {

    protected String command;
    protected String description;
    private CommandMode mode = CommandMode.GLOBAL;

    protected Map<String, DsSubcommand> subcommands = new HashMap<>();
    protected Collection<OptionData> options = new ArrayList<>();
    private Collection<Permission> permissions = new ArrayList<>();

    protected final YamlConfiguration lang;
    protected final YamlConfiguration config;

    public DsCommand(String command, String description, Collection<OptionData> options, Collection<Permission> permissions, CommandMode mode) {
        ConfigHandler configHandler = ConfigHandler.getInstance();
        lang = configHandler.getLang();
        config = configHandler.getConfig();

        this.command = command;
        this.description = description;
        this.options = options;
        this.permissions = permissions;
        this.mode = mode;
    }

    public DsCommand(String command, String description, Collection<Permission> permissions, CommandMode mode) {
        this(command, description, null, permissions, mode);
    }

    public abstract void execute(SlashCommandInteractionEvent event);

    public void addSubcommand(DsSubcommand subcommand) {
        subcommands.put(subcommand.getCommand(), subcommand);
    }

    @SuppressWarnings("null")
    public void registerCommand() {
        JDA jda = DiscordManager.getInstance().getJda();
        CommandCreateAction commandData;
        if (mode == CommandMode.COUNTRY_ONLY || mode == CommandMode.COUNTRY_AND_STAFFHUB) {
            PaisRegistry paisRegistry = PaisRegistry.getInstance();
            List<Pais> paises = paisRegistry.getList();
            for (Pais pais : paises) {
                Long guildId = pais.getDsIdGuild();
                if (guildId == null) continue;
                Guild guild = jda.getGuildById(guildId);
                if (guild == null) continue;

                commandData = guild.upsertCommand(command, description); 
                registerCommandData(commandData);
            }
        }
        if (mode == CommandMode.GLOBAL) {
            commandData = jda.upsertCommand(command, description);
            registerCommandData(commandData);
        }
        if (mode == CommandMode.STAFFHUB_ONLY || mode == CommandMode.COUNTRY_AND_STAFFHUB) {
            Guild staffHubGuild = jda.getGuildById(1425856269029474304L);
            if (staffHubGuild != null) {
                commandData = staffHubGuild.upsertCommand(command, description);
                registerCommandData(commandData);
            } else {
                ConsoleLogger.warn("Error de Discord: No se pudo encontrar el StaffHub.");
            }
        }
    }
    
    @SuppressWarnings("null")
    private void registerCommandData(CommandCreateAction commandData) {
        if (permissions != null && !permissions.isEmpty()) commandData.setDefaultPermissions(DefaultMemberPermissions.enabledFor(permissions));
        else commandData.setDefaultPermissions(DefaultMemberPermissions.ENABLED);
        if (options != null && !options.isEmpty()) commandData.addOptions(options);
        if (subcommands.isEmpty()) {
            commandData.queue();
            ConsoleLogger.debug("Comando de Discord '"+ mode +"' registrado en un servidor: /" + command);
        } else {
            for (DsSubcommand subcommand : subcommands.values()) {
                commandData.addSubcommands(subcommand.geSubcommandData());
            }
            commandData.queue();
            ConsoleLogger.debug("Comando de Discord '"+ mode + "' con subcomandos registrado: /" + command);
        }
    }

    public CommandMode getMode() {
        return mode;
    }

    public String getCommand() {
        return command;
    }

    public String getDescription() {
        return description;
    }

    public boolean hasSubcommands() {
        return !subcommands.isEmpty();
    }

    public Map<String, DsSubcommand> getSubcommands() {
        return subcommands;
    }

}
