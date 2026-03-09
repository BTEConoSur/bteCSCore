package com.bteconosur.core.command;

import com.bteconosur.core.BTEConoSur;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.registry.PlayerRegistry;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Clase base abstracta para todos los comandos del plugin.
 * Proporciona gestión automática de subcomandos, permisos, cooldowns,
 * autocompletado dinámico y validación de tipos de emisor.
 */
public abstract class BaseCommand extends Command {

    /**
     * Modo de ejecución permitido para el comando.
     */
    public enum CommandMode {
        PLAYER_ONLY,
        CONSOLE_ONLY,
        BOTH
    }

    protected final String command;
    protected String fullCommand;
    protected final String args;
    private final CommandMode commandMode;
    private final String permission;
    protected final Map<String, BaseCommand> subcommands = new HashMap<>();
    protected final Set<String> aliases = new HashSet<>();
    protected final BTEConoSur plugin;
    private BaseCommand parent;

    protected HashMap<UUID, Long> timeCooldowns = new HashMap<>();

    protected final YamlConfiguration config;

    /**
     * Crea un comando sin permiso específico y disponible para jugador/consola.
     *
     * @param command nombre base del comando.
     * @param args representación de argumentos para ayuda/autocompletado.
     */
    public BaseCommand(String command, String args) {
        this(command, args, null, CommandMode.BOTH);
    }

    /**
     * Crea un comando sin permiso específico y con modo de ejecución definido.
     *
     * @param command nombre base del comando.
     * @param args representación de argumentos para ayuda/autocompletado.
     * @param mode modo permitido de ejecución.
     */
    public BaseCommand(String command, String args, CommandMode mode) {
        this(command, args, null, mode);
    }

    /**
     * Crea un comando con permiso específico y disponible para jugador/consola.
     *
     * @param command nombre base del comando.
     * @param args representación de argumentos para ayuda/autocompletado.
     * @param permission permiso requerido para ejecutar el comando.
     */
    public BaseCommand(String command, String args, String permission) {
        this(command, args, permission, CommandMode.BOTH);
    }

    /**
     * Crea un comando con permiso y modo de ejecución.
     *
     * @param command nombre base del comando.
     * @param args representación de argumentos para ayuda/autocompletado.
     * @param permission permiso requerido para ejecutar el comando.
     * @param mode modo permitido de ejecución.
     */
    public BaseCommand(String command, String args, String permission, CommandMode mode) {
        super(command);

        ConfigHandler configHandler = ConfigHandler.getInstance();
        config = configHandler.getConfig();

        this.command = command;
        this.fullCommand = command;
        this.plugin = BTEConoSur.getInstance();
        this.permission = permission;
        this.commandMode = mode;
        this.args = args;

        String configPath = fullCommand.replace(" ", ".") + ".aliases";
        List<String> rawAliases = config.getStringList("commands-aliases." + configPath);
        if (rawAliases != null && !rawAliases.isEmpty()) {
            aliases.addAll(rawAliases);
            setAliases(new ArrayList<>(aliases));
        }
    }

    /**
     * Ejecuta el comando validando tipo de emisor, permisos, subcomandos y cooldown.
     *
     * @param sender emisor del comando.
     * @param commandLabel etiqueta usada para invocar el comando.
     * @param args argumentos recibidos.
     * @return {@code true} si se ejecutó correctamente; {@code false} si se bloqueó por validaciones.
     */
    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        Language language = Language.getDefault();
        if (!isAllowedSender(sender)) {
            if (commandMode == CommandMode.PLAYER_ONLY && !(sender instanceof Player)) PlayerLogger.error(sender, LanguageHandler.getText("player-only-command"), (String) null);
            else if (commandMode == CommandMode.CONSOLE_ONLY && sender instanceof Player) {
                language = PlayerRegistry.getInstance().get(sender).getLanguage();
                PlayerLogger.error(sender, LanguageHandler.getText(language, "console-only-command"), (String) null);
            }
            return false;
        };
        
        if (permission != null && !sender.hasPermission(permission)) {
            PlayerLogger.error(sender, LanguageHandler.getText(language, "no-permission"), (String) null);
            return false;
        }

        if (!customPermissionCheck(sender)) {
            PlayerLogger.error(sender, LanguageHandler.getText(language, "no-permission"), (String) null);
            return false;
        }

        if (args.length > 0 && !subcommands.isEmpty()) {
            String subcommandName = args[0].toLowerCase();
            BaseCommand subcommand = getSubcommand(subcommandName);

            if (subcommand != null) {
                return subcommand.execute(sender, commandLabel + " " + subcommand.getCommand(), shiftArgs(args));
            }
        }

        if (!checkCooldown(sender)) return false;

        return onCommand(sender, args);
    }

    /**
     * Devuelve sugerencias de autocompletado para subcomandos y argumentos.
     *
     * @param sender emisor que solicita el autocompletado.
     * @param alias alias usado en la invocación.
     * @param args argumentos escritos hasta el momento.
     * @return lista de sugerencias aplicables al contexto actual.
     */
    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) {
        BaseCommand currentCommand = this;
        int subcommandDepth = 0;
        
        for (int i = 0; i < args.length - 1; i++) {
            BaseCommand nextCommand = currentCommand.getSubcommand(args[i].toLowerCase());
            if (nextCommand == null) {
                return super.tabComplete(sender, alias, args);
            }
            currentCommand = nextCommand;
            subcommandDepth++;
        }

        List<String> completions = new ArrayList<>();
        
        if (!currentCommand.subcommands.isEmpty()) {
            String currentArg = args[args.length - 1].toLowerCase();
            for (BaseCommand subcommand : currentCommand.subcommands.values()) {
                if (subcommand.permission != null && !sender.hasPermission(subcommand.permission)) {
                    continue;
                }

                if (!subcommand.isAllowedSender(sender)) {
                    continue;
                }

                if (!subcommand.customPermissionCheck(sender)) {
                    continue;
                }

                String subcommandName = subcommand.getCommand();
                if (subcommandName.startsWith(currentArg)) {
                    completions.add(subcommandName);
                }

                /* 
                for (String aliasName : subcommand.aliases) {
                    if (aliasName.startsWith(currentArg)) {
                        completions.add(aliasName);
                    }
                }
                */
            }
        }

        String[] remainingArgs = Arrays.copyOfRange(args, subcommandDepth, args.length);
        List<String> argCompletions = currentCommand.tabCompleteArgs(sender, alias, remainingArgs);
        if (argCompletions != null && !argCompletions.isEmpty()) {
            completions.addAll(argCompletions);
        }
        
        return completions.isEmpty() ? super.tabComplete(sender, alias, args) : completions;
    }

    /**
     * Punto de extensión para autocompletar argumentos propios del comando.
     *
     * @param sender emisor que solicita el autocompletado.
     * @param alias alias usado en la invocación.
     * @param args argumentos parciales del comando actual.
     * @return sugerencias de argumentos o las sugerencias por defecto.
     */
    protected List<String> tabCompleteArgs(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) {
        return super.tabComplete(sender, alias, args);
    }

    /**
     * Validación extra de permisos para comandos.
     *
     * @param sender emisor a validar.
     * @return {@code true} si tiene permiso lógico para continuar.
     */
    protected boolean customPermissionCheck(CommandSender sender) {
        return true;
    }

    /**
     * Verifica y actualiza el cooldown del comando por jugador.
     *
     * @param sender emisor del comando.
     * @return {@code true} si puede ejecutar; {@code false} si aún está en cooldown.
     */
    protected boolean checkCooldown(CommandSender sender) {
        if (sender instanceof Player player) {
            Language language = PlayerRegistry.getInstance().get(sender).getLanguage();
            UUID playerUUID = player.getUniqueId();
            if (timeCooldowns.containsKey(playerUUID)) {
                String configPath = fullCommand.replace(" ", ".");
                Long cooldown = config.getLong("commands-cooldowns." + configPath, config.getLong("default-cooldown"));
                Long playerCooldown = timeCooldowns.get(playerUUID);

                Long actualCooldown = System.currentTimeMillis() - playerCooldown;
                if (actualCooldown < (cooldown * 1000)) {
                    long remainingMillis = cooldown * 1000 - actualCooldown;
                    String formattedTime = formatTime(remainingMillis, language);
                    String message = LanguageHandler.getText(language, "command-on-cooldown")
                            .replace("%time%", formattedTime);
                    PlayerLogger.warn(sender, message, (String) null);
                    return false; 
                } else {
                    timeCooldowns.put(playerUUID, System.currentTimeMillis());
                }
            } else {
                timeCooldowns.put(playerUUID, System.currentTimeMillis());
            }
        }   
        return true;
    }

    /**
     * Formatea el tiempo restante de cooldown según idioma.
     *
     * @param millis milisegundos restantes.
     * @param language idioma para la plantilla de formato.
     * @return texto formateado en segundos o minutos.
     */
    protected String formatTime(long millis, Language language) {
        double seconds = millis / 1000.0;
        
        if (seconds < 60) {
            return String.format(LanguageHandler.getText(language, "command-cooldown-second-format"), seconds);
        } else {
            long minutes = (long) (seconds / 60);
            double remainingSeconds = seconds % 60;
            return String.format(LanguageHandler.getText(language, "command-cooldown-minute-format"), minutes, remainingSeconds);
        }
    }

    /**
     * Ejecuta la lógica propia del comando concreto.
     *
     * @param sender emisor del comando.
     * @param args argumentos restantes después de resolver subcomandos.
     * @return {@code true} si la ejecución fue exitosa.
     */
    protected abstract boolean onCommand(CommandSender sender, String[] args);

    /**
     * Agrega un subcomando al comando actual y recalcula su ruta completa.
     *
     * @param subcommand subcomando a registrar.
     */
    public void addSubcommand(BaseCommand subcommand) {
        subcommand.parent = this;
        subcommand.updateFullCommand();
        subcommands.put(subcommand.getCommand(), subcommand);
    }

    /**
     * Busca un subcomando por nombre o alias.
     *
     * @param nameOrAlias nombre o alias ingresado.
     * @return subcomando encontrado o {@code null} si no existe.
     */
    private BaseCommand getSubcommand(String nameOrAlias) {
        BaseCommand direct = subcommands.get(nameOrAlias);
        if (direct != null) return direct;

        for (BaseCommand sub : subcommands.values()) {
            if (sub.aliases.contains(nameOrAlias)) return sub;
        }
        return null;
    }

    /**
     * Recalcula el comando completo y recarga aliases configurados recursivamente.
     */
    private void updateFullCommand() {
        if (parent != null) {
            this.fullCommand = parent.fullCommand + " " + this.command;
            String configPath = fullCommand.replace(" ", ".") + ".aliases";
            List<String> rawAliases = config.getStringList("commands-aliases." + configPath);
            if (rawAliases != null && !rawAliases.isEmpty()) {
                aliases.clear();
                aliases.addAll(rawAliases);
                setAliases(new ArrayList<>(aliases));
            }
        }
        for (BaseCommand subcommand : subcommands.values()) {
            subcommand.updateFullCommand();
        }
    }

    /**
     * Verifica si el emisor corresponde al tipo permitido por el comando.
     *
     * @param sender emisor a validar.
     * @return {@code true} si el emisor está permitido para el modo configurado.
     */
    protected boolean isAllowedSender(CommandSender sender) {
        return switch (commandMode) {
            case PLAYER_ONLY -> sender instanceof Player;
            case CONSOLE_ONLY -> !(sender instanceof Player);
            case BOTH -> true;
        };
    }

    /**
     * Desplaza el arreglo de argumentos removiendo el primero.
     *
     * @param args argumentos originales.
     * @return nuevo arreglo sin el primer argumento.
     */
    private String[] shiftArgs(String[] args) {
        if (args.length <= 1) return new String[0];
        String[] newArgs = new String[args.length - 1];
        System.arraycopy(args, 1, newArgs, 0, newArgs.length);
        return newArgs;
    }

    /**
     * Obtiene el nombre base del comando.
     *
     * @return nombre del comando.
     */
    public String getCommand() {
        return command;
    }

    /**
     * Obtiene la descripción localizada del comando.
     *
     * @param language idioma en el que se busca la descripción.
     * @return descripción del comando o {@code null} si no está definida.
     */
    public String getDescription(Language language) {
        String configPath = "commands-descriptions." + fullCommand.replace(" ", ".") + ".desc";
        String description = LanguageHandler.getTextWithouthWarn(language, configPath);
        if (description == "ERROR_KEY_NF") return null; 
        return description;
    }

    /**
     * Obtiene una copia de la lista de subcomandos registrados.
     *
     * @return lista con los subcomandos del comando actual.
     */
    public List<BaseCommand> getSubcommands() {
        return new ArrayList<>(subcommands.values());
    }

    /**
     * Obtiene la representación de argumentos para este comando.
     *
     * @return cadena de argumentos esperados.
     */
    public String getArgs() {
        return args;
    }

    /**
     * Obtiene la ruta completa del comando (incluyendo padres).
     *
     * @return comando completo.
     */
    public String getFullCommand() {
        return fullCommand;
    }

    /**
     * Obtiene el permiso requerido para ejecutar el comando.
     *
     * @return permiso configurado o {@code null} si no requiere.
     */
    public String getPermission() {
        return permission;
    }

}