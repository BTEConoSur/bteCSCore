package com.bteconosur.core.menu.config;

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
import com.bteconosur.db.model.RangoUsuario;
import com.bteconosur.db.registry.RangoUsuarioRegistry;

import dev.triumphteam.gui.guis.BaseGui;
import dev.triumphteam.gui.guis.Gui;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class PromoteMenu extends Menu {

    private Player BTECSPlayer;

    private ConfirmationMenu confirmationMenu;

    public PromoteMenu(Player player, String title) {
        super(title, 4, player);
        this.BTECSPlayer = player;
    }

    public PromoteMenu(Player player, Menu previousMenu, String title) {
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
        String messageSwitch = lang.getString("rango-switched");
        String messageSet = lang.getString("rango-set");
        String promoteLog = lang.getString("rango-promote-log");
        Player playerMenu = Player.getBTECSPlayer(player);

        gui.getFiller().fill(MenuUtils.getFillerItem());

        RangoUsuarioRegistry rur = RangoUsuarioRegistry.getInstance(); 
        PermissionManager pm = PermissionManager.getInstance();
        RangoUsuario admin = rur.getAdmin();
        gui.setItem(2,2, MenuUtils.getRangoUsuario(admin, pm.isAdmin(BTECSPlayer)));
        gui.addSlotAction(2,2, event -> {
            if (pm.isAdmin(BTECSPlayer)) return;
            confirmationMenu = new ConfirmationMenu(lang.getString("gui-titles.rango-confirm"), player, this, confirmClick -> {
                pm.switchRangoUsuario(BTECSPlayer, admin);
                PlayerLogger.info(BTECSPlayer, messageSwitch.replace("%rango%", admin.getNombre()), ChatUtil.getDsRangoUsuarioSwitched(admin));
                if (!BTECSPlayer.equals(playerMenu)) PlayerLogger.info(playerMenu, messageSet.replace("%rango%", admin.getNombre()).replace("%player%", BTECSPlayer.getNombre()), (String) null);
                DiscordLogger.staffLog(promoteLog.replace("%staff%", playerMenu.getNombre()).replace("%player%", BTECSPlayer.getNombre()).replace("%rango%", admin.getNombre()));
                confirmationMenu.getGui().close(player);
            }, (cancelClick -> {
                confirmationMenu.getGui().close(player);
            }));
            confirmationMenu.open();
        });

        RangoUsuario mod = rur.getMod();
        gui.setItem(2,3, MenuUtils.getRangoUsuario(mod, pm.isRangoUsuario(BTECSPlayer, mod)));
        gui.addSlotAction(2,3, event -> {
            if (pm.isRangoUsuario(BTECSPlayer, mod)) return;
            confirmationMenu = new ConfirmationMenu(lang.getString("gui-titles.rango-confirm"), player, this, confirmClick -> {
                pm.switchRangoUsuario(BTECSPlayer, mod);
                PlayerLogger.info(BTECSPlayer, messageSwitch.replace("%rango%", mod.getNombre()), ChatUtil.getDsRangoUsuarioSwitched(mod));
                if (!BTECSPlayer.equals(playerMenu)) PlayerLogger.info(playerMenu, messageSet.replace("%rango%", mod.getNombre()).replace("%player%", BTECSPlayer.getNombre()), (String) null);
                DiscordLogger.staffLog(promoteLog.replace("%staff%", playerMenu.getNombre()).replace("%player%", BTECSPlayer.getNombre()).replace("%rango%", mod.getNombre()));
                confirmationMenu.getGui().close(player);
            }, (cancelClick -> {
                confirmationMenu.getGui().close(player);
            }));
            confirmationMenu.open();
        });

        RangoUsuario normal = rur.getNormal();
        gui.setItem(2,5, MenuUtils.getRangoUsuario(normal, pm.isRangoUsuario(BTECSPlayer, normal)));
        gui.addSlotAction(2,5, event -> {
            if (pm.isRangoUsuario(BTECSPlayer, normal)) return;
            confirmationMenu = new ConfirmationMenu(lang.getString("gui-titles.rango-confirm"), player, this, confirmClick -> {
                pm.switchRangoUsuario(BTECSPlayer, normal);
                PlayerLogger.info(BTECSPlayer, messageSwitch.replace("%rango%", normal.getNombre()), ChatUtil.getDsRangoUsuarioSwitched(normal));
                if (!BTECSPlayer.equals(playerMenu)) PlayerLogger.info(playerMenu, messageSet.replace("%rango%", normal.getNombre()).replace("%player%", BTECSPlayer.getNombre()), (String) null);
                DiscordLogger.staffLog(promoteLog.replace("%staff%", playerMenu.getNombre()).replace("%player%", BTECSPlayer.getNombre()).replace("%rango%", normal.getNombre()));
                confirmationMenu.getGui().close(player);
            }, (cancelClick -> {
                confirmationMenu.getGui().close(player);
            }));
            confirmationMenu.open();
        });

        RangoUsuario influencer = rur.getInfluencer();
        gui.setItem(2,7, MenuUtils.getRangoUsuario(influencer, pm.isRangoUsuario(BTECSPlayer, influencer)));
        gui.addSlotAction(2,7, event -> {
            if (pm.isRangoUsuario(BTECSPlayer, influencer)) return;
            confirmationMenu = new ConfirmationMenu(lang.getString("gui-titles.rango-confirm"), player, this, confirmClick -> {
                pm.switchRangoUsuario(BTECSPlayer, influencer);
                PlayerLogger.info(BTECSPlayer, messageSwitch.replace("%rango%", influencer.getNombre()), ChatUtil.getDsRangoUsuarioSwitched(influencer));
                if (!BTECSPlayer.equals(playerMenu)) PlayerLogger.info(playerMenu, messageSet.replace("%rango%", influencer.getNombre()).replace("%player%", BTECSPlayer.getNombre()), (String) null);
                DiscordLogger.staffLog(promoteLog.replace("%staff%", playerMenu.getNombre()).replace("%player%", BTECSPlayer.getNombre()).replace("%rango%", influencer.getNombre()));
                confirmationMenu.getGui().close(player);
            }, (cancelClick -> {
                confirmationMenu.getGui().close(player);
            }));
            confirmationMenu.open();
        });

        RangoUsuario donador = rur.getDonador();
        gui.setItem(2,8, MenuUtils.getRangoUsuario(donador, pm.isRangoUsuario(BTECSPlayer, donador)));
        gui.addSlotAction(2,8, event -> {
            if (pm.isRangoUsuario(BTECSPlayer, donador)) return;
            confirmationMenu = new ConfirmationMenu(lang.getString("gui-titles.rango-confirm"), player, this, confirmClick -> {
                pm.switchRangoUsuario(BTECSPlayer, donador);
                PlayerLogger.info(BTECSPlayer, messageSwitch.replace("%rango%", donador.getNombre()), ChatUtil.getDsRangoUsuarioSwitched(donador));
                if (!BTECSPlayer.equals(playerMenu)) PlayerLogger.info(playerMenu, messageSet.replace("%rango%", donador.getNombre()).replace("%player%", BTECSPlayer.getNombre()), (String) null);
                DiscordLogger.staffLog(promoteLog.replace("%staff%", playerMenu.getNombre()).replace("%player%", BTECSPlayer.getNombre()).replace("%rango%", donador.getNombre()));
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
