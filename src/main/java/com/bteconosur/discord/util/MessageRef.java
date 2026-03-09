package com.bteconosur.discord.util;

/**
 * Referencia a un mensaje de Discord.
 * Almacena el identificador del canal y del mensaje para permitir su localización y manipulación.
 */
public class MessageRef {

    private final Long channelId;
    private final Long messageId;

    /**
     * Constructor de referencia de mensaje.
     * 
     * @param channelId Identificador del canal
     * @param messageId Identificador del mensaje
     */
    public MessageRef(Long channelId, Long messageId) {
        this.channelId = channelId;
        this.messageId = messageId;
    }

    /**
     * Obtiene el identificador del canal.
     * 
     * @return El ID del canal
     */
    public Long getChannelId() {
        return channelId;
    }

    /**
     * Obtiene el identificador del mensaje.
     * 
     * @return El ID del mensaje
     */
    public Long getMessageId() {
        return messageId;
    }
}
