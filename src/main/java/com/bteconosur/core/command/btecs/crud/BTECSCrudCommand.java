package com.bteconosur.core.command.btecs.crud;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.command.btecs.crud.player.CRUDPlayerCommand;
import com.bteconosur.core.command.btecs.crud.tipousuario.CRUDTipoUsuarioCommand;
import com.bteconosur.core.command.btecs.crud.rangousuario.CRUDRangoUsuarioCommand;
import com.bteconosur.core.command.btecs.crud.pais.CRUDPaisCommand;
import com.bteconosur.core.config.ConfigHandler;

public class BTECSCrudCommand extends BaseCommand {

    private final YamlConfiguration lang;

    public BTECSCrudCommand() {
        super("crud", "Realizar operaciones CRUD. (Crear, Leer, Actualizar, Eliminar). Reiniciar servidor para que se apliquen los cambios.", null, CommandMode.BOTH);
        this.addSubcommand(new CRUDPlayerCommand());
        this.addSubcommand(new CRUDTipoUsuarioCommand());
        this.addSubcommand(new CRUDRangoUsuarioCommand());
        this.addSubcommand(new CRUDPaisCommand());
        this.addSubcommand(new GenericHelpCommand(this));
        ConfigHandler configHandler = ConfigHandler.getInstance();
        lang = configHandler.getLang();
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        // TODO: Enviar por sistema de notificaciones que use help
        String message = lang.getString("help-command-usage").replace("%command%", getFullCommand());
        sender.sendMessage(message);
        return true;
    }

}
