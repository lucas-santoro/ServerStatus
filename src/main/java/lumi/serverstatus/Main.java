package lumi.serverstatus;

import com.google.inject.Inject;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import org.slf4j.Logger;

@Plugin(
        id = "serverstatus",
        name = "ServerStatus",
        version = "1.0-SNAPSHOT"
)
public class Main {

    @Inject
    private Logger logger;

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        logger.info("ServerStatus plugin foi inicializado!");
    }
}
