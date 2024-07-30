# ServerStatusBot

ServerStatusBot is a powerful plugin designed to integrate your Minecraft server with Discord, providing real-time updates about the server status directly in a specified Discord channel. This plugin is compatible with Velocity, BungeeCord, and Bukkit platforms.

## Features

- **Real-Time Updates**: Automatically updates the Discord embed with the current server status and player count.
- **Customizable Embeds**: Fully customizable embed messages, including title, description, fields, colors, images, thumbnails, and footer icons.
- **Markdown Support**: Supports markdown formatting in embed fields for enhanced text styling.
- **Connection Resilience**: Automatically attempts to reconnect to Discord in case of connection failures, with configurable retry settings.
- **Offline Status Handling**: Automatically updates the embed to reflect the server's offline status when the server shuts down or the plugin is disabled.
- **Placeholders**: Use placeholders to dynamically display information such as time, date, player count, and more.

## Placeholders

- `%time%` -> The time in the HH:mm:ss format
- `%date%` -> The date in the dd/MM/yyyy format
- `%date-us%` -> The date in the MM/dd/yyyy format
- `%player_count%` -> The number of players online
- `%max_players%` -> The maximum number of players that can be online
- `%players%` -> The server's players
- `%players_list%` -> The server's players list

## Installation

1. **Create a new Discord Application [here](https://discord.com/developers/applications).**

2. **Enable the "MESSAGE CONTENT INTENT" permission:**

    ![image](https://github.com/user-attachments/assets/c77927e4-3bbf-4573-9a2f-cdefa7ed09fa)

3. **Grant the necessary permissions in your Discord Server.**

4. **Select a channel to be the status channel and copy the ID.**

5. **Configure the plugin:**

    Edit the `config.yml` file in your server's plugins folder to customize the bot settings and embed messages.

    ```yaml
    bot-token: "YOUR_BOT_TOKEN"
    guild-id: "YOUR_GUILD_ID"
    channel-id: "YOUR_CHANNEL_ID"
    update-message: 120
    connection-attempts: 3
    reconnect-interval: 5

    online-embed:
      title: "Server Status"
      timestamp: false
      color: "#7edb64"
      fields:
        - name: "> **Status**"
          value: "```fix\nOnline```"
          inline: true
        - name: "> **Players**"
          value: "```cs\n%player_count%```"
          inline: true
        - name: "IP:"
          value: "```prolog\nONEWAYCRAFT.NET```"
          inline: false
        - name: "Bedrock port:"
          value: "```yaml\n19132```"
          inline: false
      avatar: ""
      avatar_url: ""
      image: "https://i.imgur.com/lX19VI7.png"
      thumbnail: ""
      footer: "Last update: %time%"
      footer_icon: ""

    offline-embed:
      title: "Server Status"
      timestamp: false
      color: "#ca3931"
      fields:
        - name: "> **Status**"
          value: "```ml\nOffline```"
          inline: true
        - name: "> **Players**"
          value: "```%player_count%```"
          inline: true
        - name: "IP:"
          value: "```yourserverip.net```"
          inline: false
        - name: "Bedrock port:"
          value: "```yaml\n19132```"
          inline: false
      avatar: ""
      avatar_url: ""
      image: "https://i.imgur.com/kDJ3uCV.png"
      thumbnail: ""
      footer: "Shutdown time: %date% - %time%"
      footer_icon: ""
    ```

6. **Start your server:**

    The plugin will automatically connect to Discord and start updating the embed with the server status.

## Contributing

Contributions are welcome! Please fork the repository and submit pull requests for any enhancements or bug fixes.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

## Acknowledgments

- Thanks to the creators of the JDA library for their excellent Discord API wrapper.
- Thanks to the Velocity, BungeeCord, and Bukkit communities for their support and documentation.

---

Feel free to customize this README further to match your specific needs and details about your project.
