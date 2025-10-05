package io.github.AzmiiD.azTemFly.managers;

import io.github.AzmiiD.azTemFly.AzTemFly;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages configuration and messages
 */
public class ConfigManager {

    private final AzTemFly plugin;
    private FileConfiguration config;
    private final MiniMessage miniMessage;
    private final LegacyComponentSerializer legacySerializer;

    // Cache for parsed messages
    private final Map<String, String> messageCache;

    public ConfigManager(AzTemFly plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
        this.legacySerializer = LegacyComponentSerializer.legacyAmpersand();
        this.messageCache = new HashMap<>();
        loadConfig();
    }

    /**
     * Load or reload configuration
     */
    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();
        messageCache.clear();
    }

    /**
     * Get a message from config with placeholder support
     */
    public Component getMessage(String path, Map<String, String> placeholders) {
        String rawMessage = config.getString("messages." + path, path);

        // Apply placeholders
        if (placeholders != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                rawMessage = rawMessage.replace("{" + entry.getKey() + "}", entry.getValue());
            }
        }

        // Add prefix if not a usage message
        if (!path.startsWith("usage-")) {
            String prefix = config.getString("prefix", "");
            rawMessage = prefix + rawMessage;
        }

        // Parse message (supports both legacy & and MiniMessage)
        return parseMessage(rawMessage);
    }

    /**
     * Get a message without placeholders
     */
    public Component getMessage(String path) {
        return getMessage(path, null);
    }

    /**
     * Parse message string to Component (supports both & codes and MiniMessage)
     */
    private Component parseMessage(String message) {
        // First, convert legacy & codes to section signs
        String converted = message.replace('&', '§');

        // Try to parse as MiniMessage if it contains < >
        if (message.contains("<") && message.contains(">")) {
            try {
                // Convert legacy codes first, then parse MiniMessage
                Component legacy = legacySerializer.deserialize(message.replace('&', '§'));
                return miniMessage.deserialize(message);
            } catch (Exception e) {
                // Fallback to legacy if MiniMessage parsing fails
                return legacySerializer.deserialize(converted);
            }
        }

        // Use legacy serializer for simple color codes
        return legacySerializer.deserialize(converted);
    }

    /**
     * Get auto-save interval in seconds
     */
    public int getAutoSaveInterval() {
        return config.getInt("auto-save.interval", 60);
    }

    /**
     * Check if auto-save is enabled
     */
    public boolean isAutoSaveEnabled() {
        return config.getBoolean("auto-save.enabled", true);
    }

    /**
     * Check if low time warning is enabled
     */
    public boolean isLowTimeWarningEnabled() {
        return config.getBoolean("flight.low-time-warning.enabled", true);
    }

    /**
     * Get low time warning threshold
     */
    public int getLowTimeWarningThreshold() {
        return config.getInt("flight.low-time-warning.threshold", 60);
    }

    /**
     * Get low time warning message
     */
    public Component getLowTimeWarningMessage(String timeFormatted) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("time", timeFormatted);

        String message = config.getString("flight.low-time-warning.message",
                "<yellow>⚠ Your fly time is running low: <red>{time}</red> remaining!");

        // Apply placeholders
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("{" + entry.getKey() + "}", entry.getValue());
        }

        return parseMessage(message);
    }

    /**
     * Check if creative flight should be disabled
     */
    public boolean isDisableInCreative() {
        return config.getBoolean("flight.disable-in-creative", false);
    }

    /**
     * Check if spectator flight should be disabled
     */
    public boolean isDisableInSpectator() {
        return config.getBoolean("flight.disable-in-spectator", false);
    }

    /**
     * Get time format labels
     */
    public String getHourLabel() {
        return config.getString("time-format.hour-label", "h");
    }

    public String getMinuteLabel() {
        return config.getString("time-format.minute-label", "m");
    }

    public String getSecondLabel() {
        return config.getString("time-format.second-label", "s");
    }

    /**
     * Check if debug mode is enabled
     */
    public boolean isDebugEnabled() {
        return config.getBoolean("debug", false);
    }

    /**
     * Get placeholder format for no time
     */
    public String getNoTimeFormat() {
        return config.getString("placeholders.no-time-format", "0s");
    }

    /**
     * Check if time component should be shown
     */
    public boolean shouldShowHours() {
        return config.getBoolean("time-format.show-hours", true);
    }

    public boolean shouldShowMinutes() {
        return config.getBoolean("time-format.show-minutes", true);
    }

    public boolean shouldShowSeconds() {
        return config.getBoolean("time-format.show-seconds", true);
    }
}