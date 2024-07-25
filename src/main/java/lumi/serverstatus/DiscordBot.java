package lumi.serverstatus;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import org.slf4j.Logger;

public class DiscordBot {

    private JDA jda;
    private final String botToken;
    private final String guildId;
    private final String channelId;
    private final int reconnectAttempts;
    private final Logger logger;

    public DiscordBot(String botToken, String guildId, String channelId, Logger logger, int reconnectAttempts) {
        this.botToken = botToken;
        this.guildId = guildId;
        this.channelId = channelId;
        this.reconnectAttempts = reconnectAttempts;
        this.logger = logger;
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
                        channel.sendMessage("Bot started!").queue();
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
                    Thread.sleep(5000); // Esperar 5 segundos antes de tentar novamente
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

    public void shutdown() {
        if (jda != null) {
            jda.shutdown();
        }
    }
}
