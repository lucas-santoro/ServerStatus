package lumi.serverstatus;

import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.EmbedBuilder;
import org.slf4j.Logger;

import java.time.OffsetDateTime;

public class DiscordMessageManager {

    private final Logger logger;
    private final ConfigManager configManager;

    public DiscordMessageManager(Logger logger, ConfigManager configManager) {
        this.logger = logger;
        this.configManager = configManager;
    }

    public void sendEmbed(TextChannel channel, ConfigManager.EmbedConfig embedConfig) {
        try {
            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle(embedConfig.getTitle());
            if (embedConfig.isTimestamp()) {
                embed.setTimestamp(OffsetDateTime.now());
            }
            embed.setColor(embedConfig.getColor());

            for (ConfigManager.Field field : embedConfig.getFields()) {
                embed.addField(field.getName(), field.getValue(), field.isInline());
            }
            logger.info("Sending embed with fields: {}", embedConfig.getFields());
            channel.sendMessageEmbeds(embed.build()).queue();
        } catch (Exception e) {
            logger.error("An error occurred while sending the embed message.", e);
        }
    }
}
