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
    private BukkitTask tipRotationTask;
    
    private volatile int currentTip = 0;

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

        startTipRotationTask();

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new BTEConoSurExpansion(BTEConoSur.getInstance()).register();
        } else {
            ConsoleLogger.error(LanguageHandler.getText("placeholder-not-found"));
        }

        tabAPI.getEventBus().register(PlayerLoadEvent.class, event -> {
            setTab(event.getPlayer());
        });

        
    }

    /**
     * Obtiene el tip actual para el idioma solicitado.
     *
     * @param language idioma del jugador.
     * @return tip actual del idioma indicado, o una cadena vacía si no hay tips configurados.
     */
    public String getCurrentTip(Language language) {
        List<String> tips = LanguageHandler.getTextList(language, "tips");
        if (tips.isEmpty()) return "";
        return tips.get(currentTip % tips.size());
    }

    /**
     * Inicia una tarea asíncrona que rota el índice del tip actual cada cierta cantidad de segundos.
     * Si las listas de tips en español e inglés no coinciden en tamaño,
     * o si el intervalo configurado no es válido, registra el error y no inicia la tarea.
     */
    private void startTipRotationTask() {
        List<String> spanishTips = LanguageHandler.getTextList(Language.SPANISH, "tips");
        List<String> englishTips = LanguageHandler.getTextList(Language.ENGLISH, "tips");
        if (spanishTips.size() != englishTips.size()) {
            ConsoleLogger.error("Las listas de tips en es_ES y en_US deben tener el mismo tamaño.");
            return;
        }
        if (spanishTips.isEmpty()) return;

        long rotationTicks = ConfigHandler.getInstance().getConfig().getLong("current-tip-rotation") * 20L;

        tipRotationTask = Bukkit.getScheduler().runTaskTimerAsynchronously(BTEConoSur.getInstance(), () -> {
            currentTip = (currentTip + 1) % spanishTips.size();
        }, rotationTicks, rotationTicks);
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
        setTabLine(tabPlayer, player);
    }

    /**
     * Configura la TAB para un jugador específico.
     * Obtiene el TabPlayer de la API y aplica el diseño personalizado.
     *
     * @param player jugador a configurar.
     */
    public void setTabLine(Player player) {
        if (player == null) return;
        TabPlayer tabPlayer = tabAPI.getPlayer(player.getUuid());
        if (tabPlayer == null) {
            return;
        }
        setTab(tabPlayer);
    }

    /**
     * Configura el prefijo, sufijo y nombre personalizado de un jugador en la lista de TAB.
     * Reemplaza placeholders según el idioma del jugador.
     *
     * @param tabPlayer jugador de TAB API a configurar.
     * @param player jugador con información de idioma y datos personales.
     */
    public void setTabLine(TabPlayer tabPlayer, Player player) {
        Language language = player.getLanguage();
        TabListFormatManager tlm = tabAPI.getTabListFormatManager();
        tlm.setPrefix(tabPlayer, PlaceholderUtils.replaceMC(LanguageHandler.getText("tab-prefix"), language, player));
        tlm.setSuffix(tabPlayer, PlaceholderUtils.replaceMC(LanguageHandler.getText("tab-suffix"), language, player));
        tlm.setName(tabPlayer, PlaceholderUtils.replaceMC(LanguageHandler.getText("tab-name"), language, player));
    }
    
    /**
     * Detiene el gestor de TAB y libera todos los recursos.
     * Limpia la instancia singleton.
     */
    public void shutdown() {
        ConsoleLogger.info(LanguageHandler.getText("tab-manager-shutting-down"));
        if (tipRotationTask != null) {
            tipRotationTask.cancel();
            tipRotationTask = null;
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
