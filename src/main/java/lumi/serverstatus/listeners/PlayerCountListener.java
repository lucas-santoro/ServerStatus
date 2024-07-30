package lumi.serverstatus.listeners;

import com.velocitypowered.api.proxy.ProxyServer;
import lumi.serverstatus.DiscordMessageManager;
import org.slf4j.Logger;

public class PlayerCountListener {

    private final ProxyServer server;
    private final DiscordMessageManager messageManager;
    private final Logger logger;

    public PlayerCountListener(ProxyServer server, DiscordMessageManager messageManager, Logger logger) {
        this.server = server;
        this.messageManager = messageManager;
        this.logger = logger;
    }

    public int getPlayerCount() {
        return server.getPlayerCount();
    }
}
