package com.bteconosur.discord.util;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.configuration.file.YamlConfiguration;

import com.bteconosur.core.BTEConoSur;
import com.bteconosur.core.config.ConfigHandler;
import com.bteconosur.core.config.LanguageHandler;
import com.bteconosur.core.util.ConsoleLogger;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

public class MessageService {

    private static final YamlConfiguration config = ConfigHandler.getInstance().getConfig();

    private static Map<String, Set<MessageRef>> messageRefs = new LinkedHashMap<>();

    public static boolean hasMessageRefs(String key) {
        return messageRefs.containsKey(key);
    }

    public static void addMessageKey(String key) {
        if (messageRefs.size() >= config.getInt("discord-message-track")) {
            String oldestKey = messageRefs.keySet().iterator().next();
            messageRefs.remove(oldestKey);
        }
        messageRefs.put(key, new HashSet<>());
    }

    public static void addMessageRef(String key, MessageRef ref) {
        messageRefs.get(key).add(ref);
    }

    public static boolean deleteByMessageId(Long messageId) {
        String keyEncontrada = null;

        for (Entry<String, Set<MessageRef>> entry : messageRefs.entrySet()) {
            for (MessageRef ref : entry.getValue()) {
                if (ref.getMessageId().equals(messageId)) {
                    keyEncontrada = entry.getKey();
                    break;
                }
            }
            if (keyEncontrada != null) break;
        }
        if (keyEncontrada == null) return false;

        Set<MessageRef> refs = messageRefs.get(keyEncontrada);
        for (MessageRef ref : refs) {
            deleteMessage(ref.getChannelId(), ref.getMessageId());
        }

        messageRefs.remove(keyEncontrada);
        return true;
    }


    public static void sendMessage(Long channelId, String message) {
        if (!DiscordValidate.jda()) return;
        if (!DiscordValidate.channelId(channelId) || !DiscordValidate.messageContent(message)) return;
        sendMessage(BTEConoSur.getDiscordManager().getJda().getTextChannelById(channelId), message);
    }

    @SuppressWarnings("null")
    public static void sendMessage(TextChannel channel, String message) {
        if (!DiscordValidate.jda()) return;
        if (!DiscordValidate.channel(channel) || !DiscordValidate.messageContent(message)) return;  
        try {
            //ConsoleLogger.debug("Enviando mensaje al canal " + channel.getName() + " (" + channel.getId() + ")");
            channel.sendMessage(message).queue();
        } catch (Exception e) {
            ConsoleLogger.error(LanguageHandler.getText("ds-error.send-channel").replace("%channelId%", channel.getId()), e);
        }
    }

    @SuppressWarnings("null")
    public static void sendMessage(TextChannel channel, String message, String messageId) {
        if (!DiscordValidate.jda()) return;
        if (!DiscordValidate.channel(channel) || !DiscordValidate.messageContent(message)) return;  
        try {
            //ConsoleLogger.debug("Enviando mensaje al canal " + channel.getName() + " (" + channel.getId() + ")");
            channel.sendMessage(message).queue(messageSent -> {
                if (messageId != null) addMessageRef(messageId, new MessageRef(channel.getIdLong(), messageSent.getIdLong()));
            });
        } catch (Exception e) {
            ConsoleLogger.error(LanguageHandler.getText("ds-error.send-channel").replace("%channelId%", channel.getId()), e);
        }
    }

    public static void sendDM(Long dsUserId, String message) {
        if (!DiscordValidate.jda()) return;
        if (!DiscordValidate.userId(dsUserId) || !DiscordValidate.messageContent(message)) return;
        BTEConoSur.getDiscordManager().getJda().retrieveUserById(dsUserId).queue(user -> sendDM(user, message));
    }

    @SuppressWarnings("null")
    private static void sendDM(User user, String message) {
        if (!DiscordValidate.jda()) return;
        if (!DiscordValidate.user(user) || !DiscordValidate.messageContent(message)) return;

        try {
            ConsoleLogger.debug("Enviando mensaje a usuario " + user.getName() + " (" + user.getId() + ")");
            user.openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage(message).queue());
        } catch (Exception e) {
            ConsoleLogger.error(LanguageHandler.getText("ds-error.send-user").replace("%userId%", String.valueOf(user.getIdLong())), e);
        }
    }

    public static void sendEmbed(Long channelId, MessageEmbed embed) {
        if (!DiscordValidate.jda()) return;
        if (!DiscordValidate.channelId(channelId) || !DiscordValidate.embed(embed)) return;
        sendEmbed(BTEConoSur.getDiscordManager().getJda().getTextChannelById(channelId), embed);
    }

    @SuppressWarnings("null")
    public static void sendEmbed(TextChannel channel, MessageEmbed embed) {
        if (!DiscordValidate.jda()) return;
        if (!DiscordValidate.channel(channel) || !DiscordValidate.embed(embed)) return;
        try {
            //ConsoleLogger.debug("Enviando embed al canal " + channel.getName() + " (" + channel.getId() + ")");
            channel.sendMessageEmbeds(embed).queue();
        } catch (Exception e) {
            ConsoleLogger.error(LanguageHandler.getText("ds-error.send-embed-channel").replace("%channelId%", channel.getId()), e);
        }
    }

    public static void sendEmbedDM(Long dsUserId, MessageEmbed embed) {
        if (!DiscordValidate.jda()) return;
        if (!DiscordValidate.userId(dsUserId) || !DiscordValidate.embed(embed)) return;
        BTEConoSur.getDiscordManager().getJda().retrieveUserById(dsUserId).queue(user -> sendEmbedDM(user, embed));
    }

    @SuppressWarnings("null")
    private static void sendEmbedDM(User user, MessageEmbed embed) {
        if (!DiscordValidate.jda()) return;
        if (!DiscordValidate.user(user) || !DiscordValidate.embed(embed)) return;
        try {
            ConsoleLogger.debug("Enviando embed a usuario " + user.getName() + " (" + user.getId() + ")");
            user.openPrivateChannel().queue(privateChannel -> privateChannel.sendMessageEmbeds(embed).queue());
        } catch (Exception e) {
            ConsoleLogger.error(LanguageHandler.getText("ds-error.send-embed-user").replace("%userId%", String.valueOf(user.getIdLong())), e);
        }
    }

    public static void sendBroadcastMessage(List<Long> channelsIds, String message, String messageId) {
        if (!DiscordValidate.jda()) return;
        ConsoleLogger.debug("Enviando mensaje a canales: " + channelsIds.toString());
        for (Long channelId : channelsIds) {
            if (!DiscordValidate.channelId(channelId)) continue;
            TextChannel channel = getTextChannelById(channelId);
            sendMessage(channel, message, messageId);
        }
    }

    public static void sendBroadcastMessage(List<Long> channelsIds, String message) {
        if (!DiscordValidate.jda()) return;
        ConsoleLogger.debug("Enviando mensaje a canales: " + channelsIds.toString());
        for (Long channelId : channelsIds) {
            if (!DiscordValidate.channelId(channelId)) continue;
            TextChannel channel = getTextChannelById(channelId);
            sendMessage(channel, message);
        }
    }

    public static void sendBroadcastEmbed(List<Long> channelsIds, MessageEmbed embed) {
        if (!DiscordValidate.jda()) return;
        ConsoleLogger.debug("Enviando embed a canales: " + channelsIds.toString());
        for (Long channelId : channelsIds) {
            if (!DiscordValidate.channelId(channelId)) continue;
            TextChannel channel = getTextChannelById(channelId);
            sendEmbed(channel, embed);
        }
    }

    public static void sendBroadcastDM(List<Long> usersIds, String message) {
        if (!DiscordValidate.jda()) return;
        ConsoleLogger.debug("Enviando mensaje directo a usuarios: " + usersIds.toString());
        for (Long userId : usersIds) {
            if (!DiscordValidate.userId(userId)) continue;
            sendDM(userId, message);
        }
    }

    public static void sendBroadcastEmbedDM(List<Long> usersIds, MessageEmbed embed) {
        if (!DiscordValidate.jda()) return;
        ConsoleLogger.debug("Enviando mensaje directo a usuarios: " + usersIds.toString());
        for (Long userId : usersIds) {
            if (!DiscordValidate.userId(userId)) continue;
            sendEmbedDM(userId, embed);
        }
    }
    
    public static void deleteMessage(Long channelId, Long messageId) {
        if (!DiscordValidate.jda()) return;
        if (!DiscordValidate.channelId(channelId) || !DiscordValidate.messageId(messageId)) return;
        TextChannel channel = BTEConoSur.getDiscordManager().getJda().getTextChannelById(channelId);
        if (channel == null) return;
        try {
            ConsoleLogger.debug("Eliminando mensaje de ID " + messageId + " del canal " + channel.getName() + " (" + channel.getId() + ")");
            channel.deleteMessageById(messageId).queue();
        } catch (Exception e) {
            ConsoleLogger.error(LanguageHandler.getText("ds-error.delete-channel-message").replace("%channelId%", channelId.toString()).replace("%messageId%", messageId.toString()), e);
        }
    }

    public static void deleteDMMessage(Long userId, Long messageId) {
        if (!DiscordValidate.jda()) return;
        if (!DiscordValidate.userId(userId) || !DiscordValidate.messageId(messageId)) return;
        BTEConoSur.getDiscordManager().getJda().retrieveUserById(userId).queue(user -> {
            try {
                ConsoleLogger.debug("Eliminando mensaje de ID " + messageId + " del usuario " + user.getName() + " (" + user.getId() + ")");
                user.openPrivateChannel().queue(privateChannel -> privateChannel.deleteMessageById(messageId).queue());
            } catch (Exception e) {
                ConsoleLogger.error(LanguageHandler.getText("ds-error.delete-user-message").replace("%userId%", userId.toString()).replace("%messageId%", messageId.toString()), e);
            }
        });
    }

    public static TextChannel getTextChannelById(Long channelId) {
        if (!DiscordValidate.jda()) return null;
        if (!DiscordValidate.channelId(channelId)) return null;
        return BTEConoSur.getDiscordManager().getJda().getTextChannelById(channelId);
    }

}