package lumi.serverstatus;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.EmbedBuilder;
import org.slf4j.Logger;

import java.time.OffsetDateTime;
import java.util.List;

public class DiscordMessageManager {

    private final Logger logger;
    private final ConfigManager configManager;
    private Message lastMessage;

    public DiscordMessageManager(Logger logger, ConfigManager configManager) {
        this.logger = logger;
        this.configManager = configManager;
    }

    public void sendEmbed(TextChannel channel, ConfigManager.EmbedConfig embedConfig) {
        try {
            EmbedBuilder embed = buildEmbed(embedConfig);
            channel.sendMessageEmbeds(embed.build()).queue(message -> lastMessage = message);
        } catch (Exception e) {
            logger.error("An error occurred while sending the embed message.", e);
        }
    }

    public void editEmbed(Message message, ConfigManager.EmbedConfig embedConfig) {
        try {
            EmbedBuilder embed = buildEmbed(embedConfig);
            message.editMessageEmbeds(embed.build()).queue();
        } catch (Exception e) {
            logger.error("An error occurred while editing the embed message.", e);
        }
    }

    public boolean findLastMessage(TextChannel channel) {
        List<Message> messages = channel.getHistory().retrievePast(10).complete();
        for (Message message : messages) {
            if (message.getAuthor().getId().equals(channel.getJDA().getSelfUser().getId())) {
                lastMessage = message;
                editEmbed(message, configManager.getOnlineEmbedConfig());
                return true;
            }
        }
        sendEmbed(channel, configManager.getOnlineEmbedConfig());
        return false;
    }

    private EmbedBuilder buildEmbed(ConfigManager.EmbedConfig embedConfig) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(embedConfig.getTitle());
        if (embedConfig.isTimestamp()) {
            embed.setTimestamp(OffsetDateTime.now());
        }
        embed.setColor(embedConfig.getColor());

        for (ConfigManager.Field field : embedConfig.getFields()) {
            embed.addField(field.getName(), field.getValue(), field.isInline());
        }
        return embed;
    }

    public void updateMessagePeriodically(TextChannel channel, long intervalSeconds) {
        Runnable task = () -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(intervalSeconds * 1000);
                    if (lastMessage != null) {
                        editEmbed(lastMessage, configManager.getOnlineEmbedConfig());
                    } else {
                        findLastMessage(channel);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.error("Message update interrupted", e);
                }
            }
        };
        Thread updateThread = new Thread(task);
        updateThread.start();
    }

    public void setOfflineStatus(TextChannel channel) {
        if (lastMessage != null) {
            editEmbed(lastMessage, configManager.getOfflineEmbedConfig());
        } else {
            List<Message> messages = channel.getHistory().retrievePast(10).complete();
            for (Message message : messages) {
                if (message.getAuthor().getId().equals(channel.getJDA().getSelfUser().getId())) {
                    editEmbed(message, configManager.getOfflineEmbedConfig());
                    return;
                }
            }
            sendEmbed(channel, configManager.getOfflineEmbedConfig());
        }
    }
}
