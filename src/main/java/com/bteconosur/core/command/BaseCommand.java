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

public abstract class BaseCommand extends Command {

    public enum CommandMode {
        PLAYER_ONLY,
        CONSOLE_ONLY,
        BOTH
    }

 //TODO AUTOCOMPLETADO AL CHEQUEAR PERMISOS
    protected final String command;
    protected String fullCommand;
    protected final String description;
    protected final String args;
    private final CommandMode commandMode;
    private final String permission;
    protected final Map<String, BaseCommand> subcommands = new HashMap<>();
    protected final Set<String> aliases = new HashSet<>();
    protected final BTEConoSur plugin;
    private BaseCommand parent;

    private HashMap<UUID, Long> timeCooldowns = new HashMap<>();

    private final YamlConfiguration config;

    public BaseCommand(String command, String description, String args) {
        this(command, description, args, null, CommandMode.BOTH);
    }

    public BaseCommand(String command, String description, String args, CommandMode mode) {
        this(command, description, args, null, mode);
    }

    public BaseCommand(String command, String description, String args, String permission) {
        this(command, description, args, permission, CommandMode.BOTH);
    }

    public BaseCommand(String command, String description, String args, String permission, CommandMode mode) {
        super(command);

        ConfigHandler configHandler = ConfigHandler.getInstance();
        config = configHandler.getConfig();

        this.command = command;
        this.fullCommand = command;
        this.plugin = BTEConoSur.getInstance();
        this.permission = permission;
        this.commandMode = mode;
        this.description = description;
        this.args = args;

        String configPath = fullCommand.replace(" ", ".") + ".aliases";
        List<String> rawAliases = config.getStringList("commands-aliases." + configPath);
        if (rawAliases != null && !rawAliases.isEmpty()) {
            aliases.addAll(rawAliases);
            setAliases(new ArrayList<>(aliases));
        }
    }

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
     * Método para autocompletar el comando si tiene subcomandos.
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

        if (!currentCommand.subcommands.isEmpty()) {
            List<String> completions = new ArrayList<>();
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
            return completions.isEmpty() ? super.tabComplete(sender, alias, args) : completions;
        }

        String[] remainingArgs = Arrays.copyOfRange(args, subcommandDepth, args.length);
        return currentCommand.tabCompleteArgs(sender, alias, remainingArgs);
    }

    /**
     * Método para sobrescribir en subclases que necesiten tab-complete de argumentos.
     */
    protected List<String> tabCompleteArgs(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) {
        return super.tabComplete(sender, alias, args);
    }

    protected boolean customPermissionCheck(CommandSender sender) {
        return true;
    }

    private boolean checkCooldown(CommandSender sender) {
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

    private String formatTime(long millis, Language language) {
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
     * Método abstracto para manejar la ejecución del comando.
     */
    protected abstract boolean onCommand(CommandSender sender, String[] args);

    /**
     * Agrega un subcomando a este comando.
     */
    public void addSubcommand(BaseCommand subcommand) {
        subcommand.parent = this;
        subcommand.updateFullCommand();
        subcommands.put(subcommand.getCommand(), subcommand);
    }

    private BaseCommand getSubcommand(String nameOrAlias) {
        BaseCommand direct = subcommands.get(nameOrAlias);
        if (direct != null) return direct;

        for (BaseCommand sub : subcommands.values()) {
            if (sub.aliases.contains(nameOrAlias)) return sub;
        }
        return null;
    }


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
     * Verifica si el sender es un tipo de sender permitido.
     */
    protected boolean isAllowedSender(CommandSender sender) {
        return switch (commandMode) {
            case PLAYER_ONLY -> sender instanceof Player;
            case CONSOLE_ONLY -> !(sender instanceof Player);
            case BOTH -> true;
        };
    }

    /**
     * Desplaza los argumentos eliminando el primer elemento.
     */
    private String[] shiftArgs(String[] args) {
        if (args.length <= 1) return new String[0];
        String[] newArgs = new String[args.length - 1];
        System.arraycopy(args, 1, newArgs, 0, newArgs.length);
        return newArgs;
    }

    /**
     * Obtiene el nombre del comando.
     */
    public String getCommand() {
        return command;
    }

    public String getDescription() {
        return description;
    }

    public String getArgs() {
        return args;
    }

    public String getFullCommand() {
        return fullCommand;
    }

    public String getPermission() {
        return permission;
    }

}