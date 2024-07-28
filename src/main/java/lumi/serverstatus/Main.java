package lumi.serverstatus;

import com.google.inject.Inject;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import org.slf4j.Logger;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Plugin(
        id = "serverstatus",
        name = "ServerStatus",
        version = "1.0-SNAPSHOT"
)
public class Main {

    @Inject
    private Logger logger;

    @Inject
    @DataDirectory
    private Path dataDirectory;

    private ConfigManager configManager = null;
    private DiscordBot discordBot;

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        try {
            configManager = new ConfigManager(dataDirectory, logger);
            configManager.setupConfig();

            String botToken = configManager.getBotToken();
            String guildId = configManager.getGuildId();
            String channelId = configManager.getChannelId();
            int reconnectAttempts = configManager.getReconnectAttempts();
            int reconnectInterval = configManager.getReconnectInterval();

            discordBot = new DiscordBot(botToken, guildId, channelId, reconnectAttempts, reconnectInterval, configManager, logger);
            discordBot.start();
            discordBot.findLastMessage(channelId);
        } catch (Exception e) {
            logger.error("An error occurred during plugin initialization.", e);
        }
        logger.info("ServerStatus was enabled!");
    }
}
