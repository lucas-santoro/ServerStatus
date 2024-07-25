package lumi.serverstatus;

import com.google.inject.Inject;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import org.slf4j.Logger;

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

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        try {
            if (!Files.exists(dataDirectory)) {
                Files.createDirectory(dataDirectory);
            }
        } catch (Exception e) {
            logger.error("It was not possible to create the plugin directory.", e);
        }
        logger.info("ServerStatus was enabled!");
    }
}
