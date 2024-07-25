package lumi.serverstatus;

import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

public class ConfigManager {

    private final Path dataDirectory;
    private final Logger logger;
    private final Path configFile;
    private Properties properties;

    public ConfigManager(Path dataDirectory, Logger logger) {
        this.dataDirectory = dataDirectory;
        this.logger = logger;
        this.configFile = dataDirectory.resolve("config.yml");
        this.properties = new Properties();
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

        try (InputStream in = Files.newInputStream(configFile)) {
            properties.load(in);
        }
    }

    public String getBotToken() {
        return properties.getProperty("bot-token").replaceAll("^\"|\"$", "");  // Remove aspas extras
    }

    public String getGuildId() {
        return properties.getProperty("guild-id");
    }

    public String getChannelId() {
        return properties.getProperty("channel-id");
    }

    public int getReconnectAttempts() {
        return Integer.parseInt(properties.getProperty("reconnect-attempts", "3"));  // Valor padrão 3
    }
}
