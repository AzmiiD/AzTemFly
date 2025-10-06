# TempFly - Temporary Fly Time Plugin

A Spigot/Paper plugin for Minecraft 1.21+ that allows players to receive temporary fly time, perfect for vote rewards.

## 🎯 Features

- **Give Temporary Fly Time**: Award players with temporary flight capabilities
- **Automatic Countdown**: Fly time decreases every second automatically
- **Persistent Storage**: Fly time is saved and persists through server restarts
- **PlaceholderAPI Support**: Display fly time in scoreboards, chat, and more
- **Permission-Based**: Control who can give and use fly time
- **Clean Expiration**: Players are safely grounded when time expires

## 📦 Installation

1. Download the plugin JAR file
2. Place it in your server's `plugins` folder
3. Restart your server
4. (Optional) Install [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) for placeholder support

## 🔧 Building from Source

Requirements:
- Java 21 or higher
- Maven

```bash
git clone <repository-url>
cd TempFly
mvn clean package
```

The compiled JAR will be in the `target` folder.

## 📝 Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/tfly <player> <seconds>` | Give temporary fly time to a player | `tfly.give` |
| `/tflytime` | Check your remaining fly time | `tfly.use` |
| `/tflyreload` | Reload plugin configuration | `tfly.reload` |

### Examples:
```
/tfly Steve 600        # Give Steve 10 minutes of fly time
/tfly Notch 3600       # Give Notch 1 hour of fly time
/tflytime              # Check your own fly time
```

## 🔐 Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `tfly.give` | Allows giving fly time to players | OP |
| `tfly.use` | Allows using temporary fly time | true (all players) |
| `tfly.reload` | Allows reloading configuration | OP |

## 🎨 PlaceholderAPI Placeholders

| Placeholder | Description | Example Output |
|-------------|-------------|----------------|
| `%tfly_time_remaining%` | Formatted time remaining | `5m 30s` |
| `%tfly_seconds_remaining%` | Raw seconds remaining | `330` |
| `%tfly_has_time%` | Whether player has fly time | `true` or `false` |

## 🔄 Data Storage

Fly time data is stored in `plugins/TempFly/flydata.yml`:

```yaml
flytime:
  uuid-here: 1234  # seconds remaining
  another-uuid: 5678
```

Data is automatically saved:
- Every 60 seconds (auto-save)
- When a player quits
- When the server shuts down

## ⚙️ Configuration

Currently, the plugin works out of the box with no configuration needed. Future versions may include:
- Configurable messages
- Maximum fly time limits
- Customizable save intervals
- Multi-world support

## 🐛 Troubleshooting

**Players can't fly after receiving time:**
- Ensure they have `tfly.use` permission
- Check they're not in Creative/Spectator mode (plugin skips these)

**Fly time not saving:**
- Check console for errors
- Verify write permissions for `plugins/TempFly/` folder

**PlaceholderAPI not working:**
- Ensure PlaceholderAPI is installed
- Run `/papi reload` after installing TempFly

## 📜 License

This plugin is provided as-is for use on Minecraft servers.

## 🤝 Contributing

Contributions are welcome! Feel free to submit issues and pull requests.

## 📞 Support

For support, please open an issue on the repository or contact the plugin developer.

---

**Made with Azmii for the Minecraft community**