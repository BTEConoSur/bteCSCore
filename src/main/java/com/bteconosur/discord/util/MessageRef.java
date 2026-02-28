package com.bteconosur.discord.util;

public class MessageRef {

    private final Long channelId;
    private final Long messageId;

    public MessageRef(Long channelId, Long messageId) {
        this.channelId = channelId;
        this.messageId = messageId;
    }

    public Long getChannelId() {
        return channelId;
    }

    public Long getMessageId() {
        return messageId;
    }
}
