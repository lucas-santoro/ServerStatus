package lumi.serverstatus;

import org.slf4j.Logger;
import org.yaml.snakeyaml.Yaml;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
            }
        }

        try (InputStream in = Files.newInputStream(configFile)) {
            loadPropertiesFromYAML(in);
        }
    }

    private void loadPropertiesFromYAML(InputStream inputStream) {
        Yaml yaml = new Yaml();
        Map<String, Object> yamlProps = yaml.load(inputStream);
        flattenProperties("", yamlProps);
    }

    private void flattenProperties(String prefix, Map<String, Object> map) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            if (entry.getValue() instanceof Map) {
                flattenProperties(key, (Map<String, Object>) entry.getValue());
            } else if (entry.getValue() instanceof List) {
                List<?> list = (List<?>) entry.getValue();
                if (!list.isEmpty() && list.get(0) instanceof Map) {
                    for (int i = 0; i < list.size(); i++) {
                        flattenProperties(key + "[" + i + "]", (Map<String, Object>) list.get(i));
                    }
                } else {
                    properties.put(key, entry.getValue().toString());
                }
            } else {
                properties.put(key, entry.getValue().toString());
            }
        }
    }

    public String getBotToken() {
        return properties.getProperty("bot-token").replaceAll("^\"|\"$", "");
    }

    public String getGuildId() {
        return properties.getProperty("guild-id").replaceAll("^\"|\"$", "");
    }

    public String getChannelId() {
        return properties.getProperty("channel-id").replaceAll("^\"|\"$", "");
    }

    public int getReconnectAttempts() {
        return Integer.parseInt(properties.getProperty("reconnect-attempts", "3"));
    }

    public int getReconnectInterval() {
        return Integer.parseInt(properties.getProperty("reconnect-interval", "5"));
    }

    public int getUpdateInterval(){
        return Integer.parseInt(properties.getProperty("update-interval", "120"));
    }

    public EmbedConfig getOnlineEmbedConfig() {
        return getEmbedConfig("online-embed");
    }

    public EmbedConfig getOfflineEmbedConfig() {
        return getEmbedConfig("offline-embed");
    }


    private EmbedConfig getEmbedConfig(String prefix) {
        String title = properties.getProperty(prefix + ".title", "Status");
        String description = properties.getProperty(prefix + ".description", "");
        boolean timestamp = Boolean.parseBoolean(properties.getProperty(prefix + ".timestamp", "true"));
        Color color = Color.decode(properties.getProperty(prefix + ".color", "#5897984"));

        String avatar = properties.getProperty(prefix + ".avatar", "");
        String avatarUrl = properties.getProperty(prefix + ".avatar_url", "");
        String image = properties.getProperty(prefix + ".image", "");
        String thumbnail = properties.getProperty(prefix + ".thumbnail", "");
        String footer = properties.getProperty(prefix + ".footer", "");
        String footerIcon = properties.getProperty(prefix + ".footer_icon", "");

        List<Field> fields = new ArrayList<>();
        int index = 0;
        while (properties.containsKey(prefix + ".fields[" + index + "].name")) {
            String name = properties.getProperty(prefix + ".fields[" + index + "].name");
            String value = properties.getProperty(prefix + ".fields[" + index + "].value");
            boolean inline = Boolean.parseBoolean(properties.getProperty(prefix + ".fields[" + index + "].inline"));
            fields.add(new Field(name, value, inline));
            index++;
        }

        return new EmbedConfig(title, description, timestamp, color, fields, avatar, avatarUrl, image, thumbnail, footer, footerIcon);
    }

    public static class Field {
        private final String name;
        private final String value;
        private final boolean inline;

        public Field(String name, String value, boolean inline) {
            this.name = name;
            this.value = value;
            this.inline = inline;
        }

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }

        public boolean isInline() {
            return inline;
        }

        @Override
        public String toString() {
            return "Field{name='" + name + "', value='" + value + "', inline=" + inline + "}";
        }
    }

    public static class EmbedConfig {
        private final String title;
        private final String description;
        private final boolean timestamp;
        private final Color color;
        private final List<Field> fields;
        private final String avatar;
        private final String avatarUrl;
        private final String image;
        private final String thumbnail;
        private final String footer;
        private final String footerIcon;

        public EmbedConfig(String title, String description, boolean timestamp, Color color, List<Field> fields, String avatar, String avatarUrl, String image, String thumbnail, String footer, String footerIcon) {
            this.title = title;
            this.description = description;
            this.timestamp = timestamp;
            this.color = color;
            this.fields = fields;
            this.avatar = avatar;
            this.avatarUrl = avatarUrl;
            this.image = image;
            this.thumbnail = thumbnail;
            this.footer = footer;
            this.footerIcon = footerIcon;
        }

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
        }

        public boolean isTimestamp() {
            return timestamp;
        }

        public Color getColor() {
            return color;
        }

        public List<Field> getFields() {
            return fields;
        }

        public String getAvatar() {
            return avatar;
        }

        public String getAvatarUrl() {
            return avatarUrl;
        }

        public String getImage() {
            return image;
        }

        public String getThumbnail() {
            return thumbnail;
        }

        public String getFooter() {
            return footer;
        }

        public String getFooterIcon() {
            return footerIcon;
        }

        @Override
        public String toString() {
            return "EmbedConfig{title='" + title + "', description='" + description + "', timestamp=" + timestamp + ", color=" + color + ", fields=" + fields + ", avatar='" + avatar + "', avatarUrl='" + avatarUrl + "', image='" + image + "', thumbnail='" + thumbnail + "', footer='" + footer + "', footerIcon='" + footerIcon + "'}";
        }
    }
}
