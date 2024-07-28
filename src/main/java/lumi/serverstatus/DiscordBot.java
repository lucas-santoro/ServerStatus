package lumi.serverstatus;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import org.slf4j.Logger;

import java.time.OffsetDateTime;

public class DiscordBot {

    private JDA jda;
    private final String botToken;
    private final String guildId;
    private final String channelId;
    private final int reconnectAttempts;
    private final int reconnectInterval;
    private final Logger logger;
    private final DiscordMessageManager messageManager;
    private final ConfigManager configManager;

    public DiscordBot(String botToken, String guildId, String channelId, int reconnectAttempts, int reconnectInterval, ConfigManager configManager, Logger logger) {
        this.botToken = botToken;
        this.guildId = guildId;
        this.channelId = channelId;
        this.reconnectAttempts = reconnectAttempts;
        this.reconnectInterval = reconnectInterval;
        this.logger = logger;
        this.configManager = configManager;
        this.messageManager = new DiscordMessageManager(logger, configManager);
    }

    public void start() {
        int attempts = 0;
        boolean success = false;

        while (attempts < reconnectAttempts && !success) {
            try {
                jda = JDABuilder.createDefault(botToken).build();
                jda.awaitReady();
                Guild guild = jda.getGuildById(guildId);
                if (guild != null) {
                    TextChannel channel = guild.getTextChannelById(channelId);
                    if (channel != null) {
                        findLastMessage(channelId);
                    } else {
                        logger.warn("TextChannel not found for ID: " + channelId);
                    }
                } else {
                    logger.warn("Guild not found for ID: " + guildId);
                }
                success = true;
            } catch (Exception e) {
                logger.error("An error occurred while starting the Discord bot.", e);
                attempts++;
                try {
                    Thread.sleep(reconnectInterval * 1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    logger.error("Reconnection attempt interrupted", ie);
                }
            }
        }

        if (!success) {
            logger.error("Failed to start Discord bot after " + reconnectAttempts + " attempts");
        }
    }

    public void findLastMessage(String channelId) {
        Guild guild = jda.getGuildById(guildId);
        if (guild != null) {
            TextChannel channel = guild.getTextChannelById(channelId);
            if (channel != null) {
                channel.getHistory().retrievePast(1).queue(messages -> {
                    for (Message message : messages) {
                        if (message.getAuthor().getId().equals(jda.getSelfUser().getId())) {
                            editMessage(message, configManager.getOnlineEmbedConfig());
                        } else {
                            sendMessage(channel, configManager.getOnlineEmbedConfig());
                        }
                    }
                }, throwable -> {
                    logger.error("Failed to retrieve message history", throwable);
                });
            } else {
                logger.warn("TextChannel not found for ID: " + channelId);
            }
        } else {
            logger.warn("Guild not found for ID: " + guildId);
        }
    }

    public void editMessage(Message message, ConfigManager.EmbedConfig embedConfig) {
        EmbedBuilder embed = buildEmbed(embedConfig);
        message.editMessageEmbeds(embed.build()).queue();
    }

    public void sendMessage(TextChannel channel, ConfigManager.EmbedConfig embedConfig) {
        EmbedBuilder embed = buildEmbed(embedConfig);
        channel.sendMessageEmbeds(embed.build()).queue();
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
        logger.info("Built embed with fields: {}", embedConfig.getFields());
        return embed;
    }

    public void shutdown() {
        if (jda != null) {
            jda.shutdown();
        }
    }
}
