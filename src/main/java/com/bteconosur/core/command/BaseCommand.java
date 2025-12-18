package com.bteconosur.core.command;

import com.bteconosur.core.BTEConoSur;
import com.bteconosur.core.config.ConfigHandler;

import net.kyori.adventure.text.minimessage.MiniMessage;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public abstract class BaseCommand extends Command {

    public enum CommandMode {
        PLAYER_ONLY,
        CONSOLE_ONLY,
        BOTH
    }

    protected final String command;
    protected String fullCommand;
    protected final String description;
    protected final String args;
    private final CommandMode commandMode;
    private final String permission;
    protected final Map<String, BaseCommand> subcommands = new HashMap<>();
    protected final BTEConoSur plugin;
    private BaseCommand parent;

    private HashMap<UUID, Long> timeCooldowns = new HashMap<>();

    private final YamlConfiguration lang;
    private final YamlConfiguration config;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

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
        lang = configHandler.getLang();
        config = configHandler.getConfig();

        this.command = command;
        this.fullCommand = command;
        this.plugin = BTEConoSur.getInstance();
        this.permission = permission;
        this.commandMode = mode;
        this.description = description;
        this.args = args;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (!isAllowedSender(sender)) {
            // Implementar mensaje de que el comando no puede ser ejecutado por ese tipo de sender.
            return false;
        }

        if (permission != null && !sender.hasPermission(permission)) {
            // Implementar mensaje de que el jugador no tiene permisos.
            return false;
        }

        if (args.length > 0 && !subcommands.isEmpty()) {
            String subcommandName = args[0].toLowerCase();
            BaseCommand subcommand = subcommands.get(subcommandName);

            if (subcommand != null) {
                return subcommand.execute(sender, commandLabel + " " + subcommandName, shiftArgs(args));
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
        for (int i = 0; i < args.length - 1; i++) {
            BaseCommand nextCommand = currentCommand.subcommands.get(args[i].toLowerCase());
            if (nextCommand == null) {
                return super.tabComplete(sender, alias, args);
            }
            currentCommand = nextCommand;
        }

        List<String> completions = new ArrayList<>();
        String currentArg = args[args.length - 1].toLowerCase();
        for (String subcommand : currentCommand.subcommands.keySet()) {
            if (subcommand.startsWith(currentArg)) {
                completions.add(subcommand);
            }
        }

        return completions.isEmpty() ? super.tabComplete(sender, alias, args) : completions;
    }

    private boolean checkCooldown(CommandSender sender) {
        if (sender instanceof Player player) {
            UUID playerUUID = player.getUniqueId();
            if (timeCooldowns.containsKey(playerUUID)) {
                String configPath = fullCommand.replace(" ", ".");
                Long cooldown = config.getLong("commands-cooldowns." + configPath, config.getLong("default-cooldown"));
                Long playerCooldown = timeCooldowns.get(playerUUID);

                Long actualCooldown = System.currentTimeMillis() - playerCooldown;
                if (actualCooldown < (cooldown * 1000)) {
                    long remainingMillis = cooldown * 1000 - actualCooldown;
                    String formattedTime = formatTime(remainingMillis);
                    String message = lang.getString("command-on-cooldown")
                            .replace("%time%", formattedTime);
                    sender.sendMessage(miniMessage.deserialize(message));
                    //TODO: Enviar con sistema de notificaciones.
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

    private String formatTime(long millis) {
        long seconds = millis / 1000;
        
        if (seconds < 60) {
            return seconds + " segundo(s)";
        } else {
            long minutes = seconds / 60;
            long remainingSeconds = seconds % 60;
            return minutes + " minuto(s) " + remainingSeconds + " segundo(s)";
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


    private void updateFullCommand() {
        if (parent != null) {
            this.fullCommand = parent.fullCommand + " " + this.command;
        }
        for (BaseCommand subcommand : subcommands.values()) {
            subcommand.updateFullCommand();
        }
    }

    /**
     * Verifica si el sender es un tipo de sender permitido.
     */
    private boolean isAllowedSender(CommandSender sender) {
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

}