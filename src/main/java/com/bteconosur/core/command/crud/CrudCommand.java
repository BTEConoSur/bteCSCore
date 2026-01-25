package com.bteconosur.core.command.crud;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.command.crud.ciudad.CRUDCiudadCommand;
import com.bteconosur.core.command.crud.pais.CRUDPaisCommand;
import com.bteconosur.core.command.crud.player.CRUDPlayerCommand;
import com.bteconosur.core.command.crud.rangousuario.CRUDRangoUsuarioCommand;
import com.bteconosur.core.command.crud.tipoproyecto.CRUDTipoProyectoCommand;
import com.bteconosur.core.command.crud.tipousuario.CRUDTipoUsuarioCommand;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.util.PlayerLogger;

public class CrudCommand extends BaseCommand {

    private final YamlConfiguration lang;

    public CrudCommand() {
        super("crud", "Realizar operaciones CRUD. (Crear, Leer, Actualizar, Eliminar). Reiniciar servidor para que se apliquen los cambios.", null, CommandMode.BOTH);
        this.addSubcommand(new CRUDPlayerCommand());
        this.addSubcommand(new CRUDTipoUsuarioCommand());
        this.addSubcommand(new CRUDRangoUsuarioCommand());
        this.addSubcommand(new CRUDPaisCommand());
        this.addSubcommand(new CRUDCiudadCommand());
            this.addSubcommand(new CRUDTipoProyectoCommand());
        this.addSubcommand(new GenericHelpCommand(this));
        ConfigHandler configHandler = ConfigHandler.getInstance();
        lang = configHandler.getLang();
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        String message = lang.getString("help-command-usage").replace("%command%", getFullCommand());
        PlayerLogger.info(sender, message, (String) null);
        return true;
    }

}
