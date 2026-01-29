package com.bteconosur.core.menu.project;

import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.chat.ChatUtil;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.menu.ConfirmationMenu;
import com.bteconosur.core.menu.Menu;
import com.bteconosur.core.util.DiscordLogger;
import com.bteconosur.core.util.MenuUtils;
import com.bteconosur.core.util.PlayerLogger;
import com.bteconosur.db.PermissionManager;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.model.TipoUsuario;
import com.bteconosur.db.registry.TipoUsuarioRegistry;

import dev.triumphteam.gui.guis.BaseGui;
import dev.triumphteam.gui.guis.Gui;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class ProjectPromoteMenu extends Menu {
    
    private Player BTECSPlayer;
    private ConfirmationMenu confirmationMenu;

    public ProjectPromoteMenu(Player player, String title) {
        super(title, 4, player);
        this.BTECSPlayer = player;
    }

    public ProjectPromoteMenu(Player player, Menu previousMenu, String title) {
        super(title, 4, player, previousMenu);
        this.BTECSPlayer = player;
    }

    @Override
    protected BaseGui createGui() {
        gui = Gui.gui()
            .title(MiniMessage.miniMessage().deserialize(title))
            .rows(rows)
            .disableAllInteractions()
            .create();

        YamlConfiguration lang = ConfigHandler.getInstance().getLang();
        String messageSwitch = lang.getString("tipo-switched");
        String messageSet = lang.getString("tipo-set");
        String promoteLog = lang.getString("tipo-promote-staff-log");
        Player playerMenu = Player.getBTECSPlayer(player);

        gui.getFiller().fill(MenuUtils.getFillerItem());

        TipoUsuarioRegistry tur = TipoUsuarioRegistry.getInstance(); 
        PermissionManager pm = PermissionManager.getInstance();
        TipoUsuario visita = tur.getVisita();
        gui.setItem(2,3, MenuUtils.getTipoUsuario(visita, pm.isTipoUsuario(BTECSPlayer, visita)));
        gui.addSlotAction(2,3, event -> {
            if (pm.isTipoUsuario(BTECSPlayer, visita)) return;
            confirmationMenu = new ConfirmationMenu(lang.getString("gui-titles.tipo-confirm"), player, this, confirmClick -> {
                pm.switchTipoUsuario(BTECSPlayer, visita);
                PlayerLogger.info(BTECSPlayer, messageSwitch.replace("%tipo%", visita.getNombre()), ChatUtil.getDsTipoUsuarioSwitched(visita));
                if (!BTECSPlayer.equals(playerMenu)) PlayerLogger.info(playerMenu, messageSet.replace("%tipo%", visita.getNombre()).replace("%player%", BTECSPlayer.getNombre()), (String) null);
                DiscordLogger.staffLog(promoteLog.replace("%staff%", playerMenu.getNombre()).replace("%player%", BTECSPlayer.getNombre()).replace("%tipo%", visita.getNombre()));
                confirmationMenu.getGui().close(player);
            }, (cancelClick -> {
                confirmationMenu.getGui().close(player);
            }));
            confirmationMenu.open();
        });

        TipoUsuario postulante = tur.getPostulante();
        gui.setItem(2,5, MenuUtils.getTipoUsuario(postulante, pm.isTipoUsuario(BTECSPlayer, postulante)));
        gui.addSlotAction(2,5, event -> {
            if (pm.isTipoUsuario(BTECSPlayer, postulante)) return;
            confirmationMenu = new ConfirmationMenu(lang.getString("gui-titles.tipo-confirm"), player, this, confirmClick -> {
                pm.switchTipoUsuario(BTECSPlayer, postulante);
                PlayerLogger.info(BTECSPlayer, messageSwitch.replace("%tipo%", postulante.getNombre()), ChatUtil.getDsTipoUsuarioSwitched(postulante));
                if (!BTECSPlayer.equals(playerMenu)) PlayerLogger.info(playerMenu, messageSet.replace("%tipo%", postulante.getNombre()).replace("%player%", BTECSPlayer.getNombre()), (String) null);
                DiscordLogger.staffLog(promoteLog.replace("%staff%", playerMenu.getNombre()).replace("%player%", BTECSPlayer.getNombre()).replace("%tipo%", postulante.getNombre()));
                confirmationMenu.getGui().close(player);
            }, (cancelClick -> {
                confirmationMenu.getGui().close(player);
            }));
            confirmationMenu.open();
        });

        TipoUsuario constructor = tur.getConstructor();
        gui.setItem(2,7, MenuUtils.getTipoUsuario(constructor, pm.isTipoUsuario(BTECSPlayer, constructor)));
        gui.addSlotAction(2,7, event -> {
            if (pm.isTipoUsuario(BTECSPlayer, constructor)) return;
            confirmationMenu = new ConfirmationMenu(lang.getString("gui-titles.tipo-confirm"), player, this, confirmClick -> {
                pm.switchTipoUsuario(BTECSPlayer, constructor);
                PlayerLogger.info(BTECSPlayer, messageSwitch.replace("%tipo%", constructor.getNombre()), ChatUtil.getDsTipoUsuarioSwitched(constructor));
                if (!BTECSPlayer.equals(playerMenu)) PlayerLogger.info(playerMenu, messageSet.replace("%tipo%", constructor.getNombre()).replace("%player%", BTECSPlayer.getNombre()), (String) null);
                DiscordLogger.staffLog(promoteLog.replace("%staff%", playerMenu.getNombre()).replace("%player%", BTECSPlayer.getNombre()).replace("%tipo%", constructor.getNombre()));
                confirmationMenu.getGui().close(player);
            }, (cancelClick -> {
                confirmationMenu.getGui().close(player);
            }));
            confirmationMenu.open();
        });

        return gui;
    }

    public void setBTECSPlayer(Player BTECSPlayer) {
        this.BTECSPlayer = BTECSPlayer;
    }
}
