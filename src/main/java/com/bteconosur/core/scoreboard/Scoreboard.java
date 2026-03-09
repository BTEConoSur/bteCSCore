package com.bteconosur.core.scoreboard;

import com.bteconosur.core.config.Language;
import com.bteconosur.db.model.Player;

import net.megavex.scoreboardlibrary.api.sidebar.component.ComponentSidebarLayout;

/**
 * Interfaz que define un scoreboard personalizable del servidor.
 * Permite implementadores para especificar el diseño, comportamiento de refresco y actualización
 * automática del scoreboard para jugadores individuales o globalmente.
 */
public interface Scoreboard {
    /**
     * Genera el diseño del scoreboard para un jugador específico en su idioma.
     *
     * @param player jugador para el que se genera el diseño.
     * @param language idioma del jugador.
     * @return diseño del scoreboard con los componentes a mostrar.
     */
    ComponentSidebarLayout getLayout(Player player, Language language);

    /**
     * Determina si este scoreboard debe refrescarse automáticamente
     * según un intervalo definido por {@code getRefreshIntervalTicks()}.
     *
     * @return {@code true} si el scoreboard es actualizable periódicamente.
     */
    default boolean isRefreshable() {
        return false;
    }

    /**
     * Obtiene el intervalo de actualización del scoreboard en ticks.
     * Solo se utiliza si {@code isRefreshable()} devuelve {@code true}.
     *
     * @return intervalo en ticks (20 ticks = 1 segundo por defecto).
     */
    default long getRefreshIntervalTicks() {
        return 20L;
    }

    /**
     * Determina si los datos del scoreboard son globales o por jugador.
     * Si es global, se ejecuta {@code isGlobal()} al refrescarse o 
     * al cambiar a este scoreboard en la rotación.
     *
     * @return {@code true} si el scoreboard es global.
     */
    default boolean isGlobal() {
        return false;
    }

    /**
     * Actualiza datos globales del scoreboard al refrescarse o 
     * al cambiar a este scoreboard en la rotación.
     * Solo se invoca si {@code isGlobal()} devuelve {@code true}.
     */
    default void update() {}
    
}
