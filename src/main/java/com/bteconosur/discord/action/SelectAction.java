package com.bteconosur.discord.action;

import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import com.bteconosur.db.model.Interaction;

/**
 * Interfaz para manejar acciones de menús de selección en Discord.
 * Las implementaciones definen el comportamiento cuando un usuario selecciona una opción de un menú desplegable.
 */
public interface SelectAction {
    /**
     * Maneja el evento de interacción con un menú de selección.
     * 
     * @param event Evento de interacción del menú de selección
     * @param ctx Contexto de la interacción con información adicional
     */
    void handle(StringSelectInteractionEvent event, Interaction ctx);
}
