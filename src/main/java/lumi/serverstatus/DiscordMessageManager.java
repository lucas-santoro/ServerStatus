package lumi.serverstatus;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.EmbedBuilder;
import org.slf4j.Logger;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import lumi.serverstatus.listeners.PlayerCountListener;

public class DiscordMessageManager {

    private final Logger logger;
    private final ConfigManager configManager;
    private final PlayerCountListener playerCountListener;
    private Message lastMessage;

    public DiscordMessageManager(Logger logger, ConfigManager configManager, PlayerCountListener playerCountListener) {
        this.logger = logger;
        this.configManager = configManager;
        this.playerCountListener = playerCountListener;
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

    public void findLastMessage(TextChannel channel) {
        List<Message> messages = channel.getHistory().retrievePast(10).complete();
        for (Message message : messages) {
            if (message.getAuthor().getId().equals(channel.getJDA().getSelfUser().getId())) {
                lastMessage = message;
                editEmbed(message, configManager.getOnlineEmbedConfig());
                return;
            }
        }
        sendEmbed(channel, configManager.getOnlineEmbedConfig());
    }

    private EmbedBuilder buildEmbed(ConfigManager.EmbedConfig embedConfig) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(replacePlaceholders(embedConfig.getTitle()));
        embed.setDescription(replacePlaceholders(embedConfig.getDescription()));
        if (embedConfig.isTimestamp()) {
            embed.setTimestamp(OffsetDateTime.now());
        }
        embed.setColor(embedConfig.getColor());

        for (ConfigManager.Field field : embedConfig.getFields()) {
            embed.addField(replacePlaceholders(field.getName()), replacePlaceholders(field.getValue()), field.isInline());
        }

        if (!embedConfig.getAvatar().isEmpty()) {
            embed.setAuthor(embedConfig.getAvatar(), embedConfig.getAvatarUrl(), embedConfig.getAvatarUrl());
        }

        if (!embedConfig.getImage().isEmpty()) {
            embed.setImage(embedConfig.getImage());
        }

        if (!embedConfig.getThumbnail().isEmpty()) {
            embed.setThumbnail(embedConfig.getThumbnail());
        }

        if (!embedConfig.getFooter().isEmpty()) {
            if (!embedConfig.getFooterIcon().isEmpty()) {
                if (isValidURL(embedConfig.getFooterIcon())) {
                    embed.setFooter(replacePlaceholders(embedConfig.getFooter()), embedConfig.getFooterIcon());
                } else {
                    logger.warn("Invalid footer icon URL: {}", embedConfig.getFooterIcon());
                    embed.setFooter(replacePlaceholders(embedConfig.getFooter()));
                }
            } else {
                embed.setFooter(replacePlaceholders(embedConfig.getFooter()));
            }
        }

        return embed;
    }

    private String replacePlaceholders(String text) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("%time%", LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        placeholders.put("%date%", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        placeholders.put("%date-us%", LocalDate.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy")));
        placeholders.put("%player_count%", String.valueOf(playerCountListener.getPlayerCount()));
        placeholders.put("%max_players%", String.valueOf(playerCountListener.getMaxPlayers()));

        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            text = text.replace(entry.getKey(), entry.getValue());
        }

        return text;
    }

    private boolean isValidURL(String url) {
        try {
            new java.net.URL(url).toURI();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void updateMessagePeriodically(TextChannel channel, int updateInterval) {
        Runnable task = () -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(updateInterval * 1000);
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
            sendEmbed(channel, configManager.getOfflineEmbedConfig());
        }
    }

    public void updatePlayerCount(int playerCount) {
        if (lastMessage != null) {
            editEmbed(lastMessage, configManager.getOnlineEmbedConfig());
        }
    }
}
