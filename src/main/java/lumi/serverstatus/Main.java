package lumi.serverstatus;

import com.google.inject.Inject;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import lumi.serverstatus.listeners.PlayerCountListener;
import org.slf4j.Logger;

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

    @Inject
    private ProxyServer server;

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

            PlayerCountListener playerCountListener = new PlayerCountListener(server, null, logger);
            DiscordMessageManager messageManager = new DiscordMessageManager(logger, configManager, playerCountListener);

            discordBot = new DiscordBot(botToken, guildId, channelId, reconnectAttempts, reconnectInterval, messageManager, logger);
            discordBot.start();

            server.getEventManager().register(this, playerCountListener);
        } catch (Exception e) {
            logger.error("An error occurred during plugin initialization.", e);
        }
        logger.info("ServerStatus was enabled!");
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        if (discordBot != null) {
            discordBot.setOfflineStatus();
            discordBot.shutdown();
        }
        logger.info("ServerStatus was disabled!");
    }
}
