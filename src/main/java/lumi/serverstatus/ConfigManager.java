package lumi.serverstatus;

import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class ConfigManager {

    private final Path dataDirectory;
    private final Logger logger;
    private final Path configFile;

    public ConfigManager(Path dataDirectory, Logger logger) {
        this.dataDirectory = dataDirectory;
        this.logger = logger;
        this.configFile = dataDirectory.resolve("config.yml");
    }

    public void setupConfig() throws IOException {
        if (!Files.exists(dataDirectory)) {
            Files.createDirectory(dataDirectory);
        }

        if (!Files.exists(configFile)) {
            try (InputStream in = getClass().getResourceAsStream("/config.yml")) {
                Files.copy(in, configFile, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                logger.error("It was not possible to create the config.yml file.", e);
                throw e;
            }
        }
    }
}
