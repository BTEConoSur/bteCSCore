package com.bteconosur.core.command.crud;

import org.bukkit.command.CommandSender;

import com.bteconosur.core.command.BaseCommand;
import com.bteconosur.core.command.GenericHelpCommand;
import com.bteconosur.core.command.crud.division.CRUDDivisionCommand;
import com.bteconosur.core.command.crud.pais.CRUDPaisCommand;
import com.bteconosur.core.command.crud.player.CRUDPlayerCommand;
import com.bteconosur.core.command.crud.rangousuario.CRUDRangoUsuarioCommand;
import com.bteconosur.core.command.crud.tipoproyecto.CRUDTipoProyectoCommand;
import com.bteconosur.core.command.crud.tipousuario.CRUDTipoUsuarioCommand;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.model.Player;

public class CrudCommand extends BaseCommand {

    public CrudCommand() {
        super("crud", "Realizar operaciones CRUD. (Crear, Leer, Actualizar, Eliminar). Reiniciar servidor para que se apliquen los cambios.", null, CommandMode.BOTH);
        this.addSubcommand(new CRUDPlayerCommand());
        this.addSubcommand(new CRUDTipoUsuarioCommand());
        this.addSubcommand(new CRUDRangoUsuarioCommand());
        this.addSubcommand(new CRUDPaisCommand());
        this.addSubcommand(new CRUDDivisionCommand());
        this.addSubcommand(new CRUDTipoProyectoCommand());
        this.addSubcommand(new GenericHelpCommand(this));
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Player commandPlayer = Player.getBTECSPlayer((org.bukkit.entity.Player) sender);
        Language language = commandPlayer.getLanguage();
        String message = LanguageHandler.getText(language, "help-command-usage").replace("%comando%", getFullCommand());
        PlayerLogger.info(sender, message, (String) null);
        return true;
    }

}
