package com.bteconosur.discord.action;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import com.bteconosur.db.model.Interaction;

/**
 * Interfaz para manejar acciones de botones en Discord.
 * Las implementaciones definen el comportamiento cuando un usuario interactúa con un botón.
 */
public interface ButtonAction {
    /**
     * Maneja el evento de interacción con un botón.
     * 
     * @param event Evento de interacción del botón
     * @param ctx Contexto de la interacción con información adicional
     */
    void handle(ButtonInteractionEvent event, Interaction ctx);
}
