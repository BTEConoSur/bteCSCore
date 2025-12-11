package com.bteconosur.core.command;

import com.bteconosur.core.BTEConoSur;
import com.bteconosur.core.config.ConfigHandler;

import net.kyori.adventure.text.minimessage.MiniMessage;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.checkerframework.checker.units.qual.h;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BaseCommand extends Command {

    public enum CommandMode {
        PLAYER_ONLY,
        CONSOLE_ONLY,
        BOTH
    }

    private final String command;
    private final String description;
    private final String args;
    private final CommandMode commandMode;
    private final String permission;
    private final Map<String, BaseCommand> subcommands = new HashMap<>();
    protected final BTEConoSur plugin;

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

        if (args.length > 0 && args[0].equalsIgnoreCase("help")) {
            showHelp(sender, commandLabel);
            return true;
        }

        if (args.length > 0 && !subcommands.isEmpty()) {
            String subcommandName = args[0].toLowerCase();
            BaseCommand subcommand = subcommands.get(subcommandName);

            if (subcommand != null) {
                return subcommand.execute(sender, commandLabel + " " + subcommandName, shiftArgs(args));
            }
        }

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

        if ("help".startsWith(currentArg)) {
            completions.add("help");
        }

        return completions.isEmpty() ? super.tabComplete(sender, alias, args) : completions;
    }

    private void showHelp(CommandSender sender, String fullCommand) {
        String header = lang.getString("help-command.header")
            .replace("%command%", fullCommand)
            .replace("%plugin-prefix%", lang.getString("plugin-prefix"));
        String usage = lang.getString("help-command.usage");
        String descriptionLabel = lang.getString("help-command.description");
        String subcommandsTitle = lang.getString("help-command.subcommands-title");
        String subcommandLine = lang.getString("help-command.subcommand-line");
        String footer = lang.getString("help-command.footer");

        sender.sendMessage(miniMessage.deserialize(header));
        //TODO: Enviar con sistema de notificaciones.

        if (description != null && !description.isEmpty()) {
            descriptionLabel = descriptionLabel.replace("%description%", description);
            sender.sendMessage(miniMessage.deserialize(descriptionLabel));
        }
        
        usage = usage.replace("%command%", fullCommand).replace("%args%", args != null ? args : (subcommands.isEmpty() ? "" : "<subcomando>"));
        sender.sendMessage(miniMessage.deserialize(usage));
        
        if (!subcommands.isEmpty()) {
            sender.sendMessage(miniMessage.deserialize(subcommandsTitle));
            for (BaseCommand sub : subcommands.values()) {
                String subDesc = sub.description != null ? sub.description : "";
                
                String line = subcommandLine
                    .replace("%command%", fullCommand)
                    .replace("%subcommand%", sub.command)
                    .replace("%description%", subDesc);
                
                sender.sendMessage(miniMessage.deserialize(line));
            }
        }

        sender.sendMessage(miniMessage.deserialize(footer));
    }

    /**
     * Método abstracto para manejar la ejecución del comando.
     */
    protected abstract boolean onCommand(CommandSender sender, String[] args);

    /**
     * Agrega un subcomando a este comando.
     */
    public void addSubcommand(BaseCommand subcommand) {
        subcommands.put(subcommand.getCommand(), subcommand);
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
}