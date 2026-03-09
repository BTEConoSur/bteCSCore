package com.bteconosur.discord.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.ConsoleLogger;
import com.bteconosur.db.model.Pais;
import com.bteconosur.db.registry.PaisRegistry;
import com.bteconosur.discord.DiscordManager;
import com.bteconosur.discord.util.CommandMode;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

/**
 * Clase base abstracta para comandos de contexto de usuarios en Discord.
 * Los comandos de contexto aparecen en el menú contextual al hacer clic derecho sobre un usuario.
 * Soporta diferentes modos de registro (global, por país, staffhub).
 */
public abstract class DsContextUserCommand {

    protected String command;
    private Collection<Permission> permissions = new ArrayList<>();
    private CommandMode mode = CommandMode.GLOBAL;

    private final YamlConfiguration secret = ConfigHandler.getInstance().getSecret();

    /**
     * Constructor de un comando de contexto de usuario.
     * 
     * @param command Nombre del comando
     * @param permissions Permisos requeridos para ejecutar el comando
     * @param mode Modo de registro del comando (global, país, staffhub)
     */
    public DsContextUserCommand(String command, Collection<Permission> permissions, CommandMode mode) {
        this.command = command;
        this.permissions = permissions;
        this.mode = mode;
    }

    /**
     * Ejecuta la lógica del comando cuando es invocado desde el contexto de un usuario.
     * 
     * @param event Evento de interacción del comando de contexto de usuario
     */
    public abstract void execute(UserContextInteractionEvent event);

    /**
     * Registra el comando de contexto en Discord según el modo configurado.
     * Puede registrarse globalmente, en servidores de países, o en el staffhub.
     */
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

                guild.upsertCommand(Commands.user(command).setDefaultPermissions(perm)).queue();; 
            }
        }
        if (mode == CommandMode.GLOBAL) {
            jda.upsertCommand(Commands.user(command).setDefaultPermissions(perm)).queue();
        }
        if (mode == CommandMode.STAFFHUB_ONLY || mode == CommandMode.COUNTRY_AND_STAFFHUB) {
            Guild staffHubGuild = jda.getGuildById(secret.getLong("discord-staff-guild-id"));
            if (staffHubGuild != null) {
                staffHubGuild.upsertCommand(Commands.user(command).setDefaultPermissions(perm)).queue();
            } else {
                ConsoleLogger.warn(LanguageHandler.getText("ds-error.staffhub-not-found"));
            }
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

}
