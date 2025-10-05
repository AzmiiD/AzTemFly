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

### Usage Examples:
- Scoreboard: Display `Fly Time: %tfly_time_remaining%`
- Tab list: Show `[✈ %tfly_time_remaining%]` suffix
- Chat format: Include fly time in player tags

## ⚙️ Configuration

The plugin comes with a fully customizable `config.yml`:

```yaml
# Plugin prefix for all messages
prefix: "&8[&bTempFly&8]&r "

# All messages support both & color codes and MiniMessage format
messages:
  fly-time-received: "<green>You received temporary fly time for <yellow>{time}</yellow>!"
  fly-time-expired: "<red>Your temporary fly time has expired!"
  # ... and many more customizable messages

# Auto-save settings
auto-save:
  enabled: true
  interval: 60  # Save every 60 seconds

# Time format settings
time-format:
  show-seconds: true
  show-minutes: true
  show-hours: true
  hour-label: "h"
  minute-label: "m"
  second-label: "s"

# Low time warning (warns player when time is running out)
flight:
  low-time-warning:
    enabled: true
    threshold: 60  # Warn when <= 60 seconds remaining
    message: "<yellow>⚠ Your fly time is running low: <red>{time}</red> remaining!"
```

### Customizing Messages:

**Supports TWO color formats:**

1. **Legacy & codes:**
   ```yaml
   message: "&aGreen text &e&lYellow bold"
   ```

2. **MiniMessage (modern):**
   ```yaml
   message: "<green>Green text <yellow><bold>Yellow bold"
   message: "<gradient:red:blue>Rainbow text!</gradient>"
   ```

**Available placeholders in messages:**
- `{player}` - Player name
- `{time}` - Formatted time (e.g., "5m 30s")
- `{seconds}` - Raw seconds
- `{input}` - User input (for error messages)

### Reload Configuration:
```
/tflyreload
```
Reloads all messages and settings without restarting the server!

## 💡 Use Case: Vote Rewards

Perfect for rewarding players who vote for your server!

### With Votifier/NuVotifier:
Add this to your vote listener configuration:
```yaml
- cmd: tfly %player% 600
```

### With VotingPlugin:
```yaml
rewards:
  - command: "tfly %player% 1800"
```

### Recommended Time Values:
- **5 minutes** (300s) - Small reward
- **10 minutes** (600s) - Standard vote reward
- **30 minutes** (1800s) - Premium vote reward
- **1 hour** (3600s) - Special event reward

## 🎮 How It Works

1. **Receiving Fly Time**: When a player receives fly time via `/tfly`, they can immediately start flying if they're online
2. **Countdown**: Every second, all players' fly time decreases by 1 second
3. **Expiration**: When time reaches 0:
    - Flight is disabled
    - If the player is flying, they begin falling
    - A warning message is sent
4. **Persistence**: All fly time is saved to `flydata.yml` and restored on server restart

## 📁 File Structure

```
TempFly/
├── src/
│   └── main/
│       ├── java/
│       │   └── io/github/AzmilD/aTempFly/
│       │       ├── TempFly.java              # Main plugin class
│       │       ├── commands/
│       │       │   ├── TFlyCommand.java      # /tfly command
│       │       │   ├── TFlyTimeCommand.java  # /tflytime command
│       │       │   └── TFlyReloadCommand.java # /tflyreload command
│       │       ├── listeners/
│       │       │   └── PlayerListener.java   # Event handling
│       │       ├── managers/
│       │       │   ├── ConfigManager.java    # Config & messages
│       │       │   └── FlyTimeManager.java   # Core fly time logic
│       │       └── placeholders/
│       │           └── TempFlyPlaceholder.java # PAPI integration
│       └── resources/
│           ├── plugin.yml                    # Plugin configuration
│           └── config.yml                    # Default config
├── pom.xml                                   # Maven build file
└── README.md                                 # This file
```d
│       │       ├── listeners/
│       │       │   └── PlayerListener.java   # Event handling
│       │       ├── managers/
│       │       │   └── FlyTimeManager.java   # Core logic
│       │       └── placeholders/
│       │           └── TempFlyPlaceholder.java # PAPI integration
│       └── resources/
│           └── plugin.yml                    # Plugin configuration
├── pom.xml                                   # Maven build file
└── README.md                                 # This file
```

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

**Made with ❤️ for the Minecraft community**