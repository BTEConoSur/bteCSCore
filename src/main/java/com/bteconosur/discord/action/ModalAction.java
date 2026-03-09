package com.bteconosur.discord.action;

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import com.bteconosur.db.model.Interaction;

/**
 * Interfaz para manejar acciones de modales en Discord.
 * Las implementaciones definen el comportamiento cuando un usuario envía un formulario modal.
 */
public interface ModalAction {
    /**
     * Maneja el evento de interacción con un modal.
     * 
     * @param event Evento de interacción del modal
     * @param ctx Contexto de la interacción con información adicional
     */
    void handle(ModalInteractionEvent event, Interaction ctx);
}
