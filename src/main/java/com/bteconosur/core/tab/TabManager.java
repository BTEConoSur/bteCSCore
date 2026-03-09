package com.bteconosur.core.tab;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import com.bteconosur.core.BTEConoSur;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.config.Language;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.ConsoleLogger;
import com.bteconosur.db.model.Player;
import com.bteconosur.db.registry.PlayerRegistry;
import com.bteconosur.db.util.PlaceholderUtils;

import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.event.player.PlayerLoadEvent;
import me.neznamy.tab.api.tablist.HeaderFooterManager;
import me.neznamy.tab.api.tablist.TabListFormatManager;

/**
 * Gestor centralizado de TAB del servidor.
 * Administra encabezados, pies de página y formato de nombres en la lista de TAB.
 * Soporta personalización por idioma del jugador.
 * Implementa patrón singleton.
 */
public class TabManager {

    private static TabManager instance;

    private TabAPI tabAPI;
    private BukkitTask footerRefreshTask;
    
    /**
     * Crea e inicializa el gestor de TAB, registrando evento de carga de jugador
     * y configurando la API de TAB para el servidor.
     */
    public TabManager() {
        ConsoleLogger.info(LanguageHandler.getText("tab-manager-initializing"));
        try {
            tabAPI = TabAPI.getInstance();
        } catch (Exception e) {
            ConsoleLogger.error(LanguageHandler.getText("tab-manager-error"), e);
            return;
        }

        tabAPI.getEventBus().register(PlayerLoadEvent.class, event -> {
            setTab(event.getPlayer());
        });

        //startFooterRefresher();
    }

    /**
     * Configura la TAB para un jugador que ingresa al servidor.
     * Aplica encabezado, pie de página y formato personalizado según su idioma.
     *
     * @param player jugador que ingresa.
     */
    public void joinPlayer(Player player) {
        if (player == null) return;
        TabPlayer tabPlayer = tabAPI.getPlayer(player.getUuid());
        if (tabPlayer == null) {
            return;
        }
        setTab(tabPlayer);
    }

    /**
     * Aplica el diseño de TAB a un jugador específico.
     * Establece encabezado, pie de página, prefijo, sufijo y nombre personalizados
     * según el idioma del jugador, reemplazando placeholders.
     *
     * @param tabPlayer jugador de TAB API a configurar.
     */
    public void setTab(TabPlayer tabPlayer) {
        HeaderFooterManager hfm = tabAPI.getHeaderFooterManager();
        Player player = PlayerRegistry.getInstance().get(tabPlayer.getUniqueId());
        if (player == null) return;
        Language language = player.getLanguage();

        List<String> processedHeader = new ArrayList<>();
        List<String> processedFooter = new ArrayList<>();
        for (String line : LanguageHandler.getTextList(language, "tab-header")) {
            processedHeader.add(PlaceholderUtils.replaceMC(line, language, player));  
        }
        for (String line : LanguageHandler.getTextList(language, "tab-footer")) {
            processedFooter.add(PlaceholderUtils.replaceMC(line, language, player));  
        }

        hfm.setHeader(tabPlayer, String.join("\n", processedHeader));
        hfm.setFooter(tabPlayer, String.join("\n", processedFooter));
        TabListFormatManager tlm = tabAPI.getTabListFormatManager();
        tlm.setPrefix(tabPlayer, PlaceholderUtils.replaceMC(LanguageHandler.getText("tab-prefix"), language, player));
        tlm.setSuffix(tabPlayer, PlaceholderUtils.replaceMC(LanguageHandler.getText("tab-suffix"), language, player));
        tlm.setName(tabPlayer, PlaceholderUtils.replaceMC(LanguageHandler.getText("tab-name"), language, player));
    }

    public void startFooterRefresher() {
        long refreshIntervalMs = ConfigHandler.getInstance().getConfig().getLong("tab-reload");
        long refreshIntervalTicks = Math.max(1L, refreshIntervalMs / 50L);
        if (footerRefreshTask != null) {
            footerRefreshTask.cancel();
            footerRefreshTask = null;
        }
        footerRefreshTask = Bukkit.getScheduler().runTaskTimer(BTEConoSur.getInstance(), this::reloadFooter, 0L, refreshIntervalTicks);
    }

    public void reloadFooter() {
        for (Player player : PlayerRegistry.getInstance().getOnlinePlayers()) {
            if (player == null) continue;
            TabPlayer tabPlayer = tabAPI.getPlayer(player.getUuid());
            if (tabPlayer == null) continue;
            Language language = player.getLanguage();
            HeaderFooterManager hfm = tabAPI.getHeaderFooterManager();
            List<String> processedFooter = new ArrayList<>();
            for (String line : LanguageHandler.getTextList(language, "tab-footer")) {
                processedFooter.add(PlaceholderUtils.replaceMC(line, language, player));  
            }
            hfm.setFooter(tabPlayer, String.join("\n", processedFooter));
        }
    }
    

    /**
     * Detiene el gestor de TAB y libera todos los recursos.
     * Limpia la instancia singleton.
     */
    public void shutdown() {
        ConsoleLogger.info(LanguageHandler.getText("tab-manager-shutting-down"));
        if (footerRefreshTask != null) {
            footerRefreshTask.cancel();
            footerRefreshTask = null;
        }
        if (instance != null) {
            instance = null;
        }
    }

    /**
     * Obtiene la instancia única del gestor de TAB (patrón singleton).
     *
     * @return instancia única del gestor.
     */
    public static TabManager getInstance() {
        if (instance == null) {
            instance = new TabManager();
        }
        return instance;
    }
}
