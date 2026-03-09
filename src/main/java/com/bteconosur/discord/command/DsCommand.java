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
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.ConsoleLogger;
import com.bteconosur.discord.DiscordManager;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.restaction.CommandCreateAction;

/**
 * Clase base abstracta para comandos de Discord.
 * Define la estructura y funcionalidad común para todos los comandos slash del bot.
 * Soporta diferentes modos de registro (global, por país, staffhub) y subcomandos.
 */
public abstract class DsCommand {

    protected String command;
    protected String description;
    private CommandMode mode = CommandMode.GLOBAL;

    protected Map<String, DsSubcommand> subcommands = new HashMap<>();
    protected Collection<OptionData> options = new ArrayList<>();
    private Collection<Permission> permissions = new ArrayList<>();

    protected final YamlConfiguration config;
    private final YamlConfiguration secret = ConfigHandler.getInstance().getSecret();

    /**
     * Constructor completo de un comando de Discord.
     * 
     * @param command Nombre del comando
     * @param description Descripción del comando
     * @param options Opciones del comando
     * @param permissions Permisos requeridos para ejecutar el comando
     * @param mode Modo de registro del comando (global, país, staffhub)
     */
    public DsCommand(String command, String description, Collection<OptionData> options, Collection<Permission> permissions, CommandMode mode) {
        ConfigHandler configHandler = ConfigHandler.getInstance();
        config = configHandler.getConfig();

        this.command = command;
        this.description = description;
        this.options = options;
        this.permissions = permissions;
        this.mode = mode;
    }

    /**
     * Constructor simplificado de un comando de Discord sin opciones.
     * 
     * @param command Nombre del comando
     * @param description Descripción del comando
     * @param permissions Permisos requeridos para ejecutar el comando
     * @param mode Modo de registro del comando
     */
    public DsCommand(String command, String description, Collection<Permission> permissions, CommandMode mode) {
        this(command, description, null, permissions, mode);
    }

    /**
     * Ejecuta la lógica del comando cuando es invocado.
     * 
     * @param event Evento de interacción del comando slash
     */
    public abstract void execute(SlashCommandInteractionEvent event);

    /**
     * Añade un subcomando a este comando.
     * 
     * @param subcommand Subcomando a añadir
     */
    public void addSubcommand(DsSubcommand subcommand) {
        subcommands.put(subcommand.getCommand(), subcommand);
        subcommand.setParentCommand(command);
    }

    /**
     * Registra el comando en Discord según el modo configurado.
     * Puede registrarse globalmente, en servidores de países, o en el staffhub.
     */
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
            Guild staffHubGuild = jda.getGuildById(secret.getLong("discord-staff-guild-id"));
            if (staffHubGuild != null) {
                commandData = staffHubGuild.upsertCommand(command, description);
                registerCommandData(commandData);
            } else {
                ConsoleLogger.warn(LanguageHandler.getText("ds-error.staffhub-not-found"));
            }
        }
    }
    
    /**
     * Configura y registra los datos del comando en Discord.
     * 
     * @param commandData Acción de creación del comando
     */
    @SuppressWarnings("null")
    private void registerCommandData(CommandCreateAction commandData) {
        if (permissions != null && !permissions.isEmpty()) commandData.setDefaultPermissions(DefaultMemberPermissions.enabledFor(permissions));
        else commandData.setDefaultPermissions(DefaultMemberPermissions.ENABLED);
        if (options != null && !options.isEmpty()) commandData.addOptions(options);
        if (subcommands.isEmpty()) {
            commandData.queue();
            ConsoleLogger.debug("Comando de Discord '"+ mode +"' registrado: /" + command);
        } else {
            for (DsSubcommand subcommand : subcommands.values()) {
                commandData.addSubcommands(subcommand.geSubcommandData());
            }
            commandData.queue();
            ConsoleLogger.debug("Comando de Discord '"+ mode + "' con subcomandos registrado: /" + command);
        }
    }

    /**
     * Obtiene el modo de registro del comando.
     * 
     * @return El modo de registro configurado
     */
    public CommandMode getMode() {
        return mode;
    }

    /**
     * Obtiene el nombre del comando.
     * 
     * @return El nombre del comando
     */
    public String getCommand() {
        return command;
    }

    /**
     * Obtiene la descripción del comando.
     * 
     * @return La descripción del comando
     */
    public String getDescription() {
        return description;
    }

    /**
     * Verifica si el comando tiene subcomandos registrados.
     * 
     * @return true si tiene subcomandos, false en caso contrario
     */
    public boolean hasSubcommands() {
        return !subcommands.isEmpty();
    }

    /**
     * Obtiene el mapa de subcomandos registrados.
     * 
     * @return Mapa con los subcomandos, indexados por su nombre
     */
    public Map<String, DsSubcommand> getSubcommands() {
        return subcommands;
    }

}
