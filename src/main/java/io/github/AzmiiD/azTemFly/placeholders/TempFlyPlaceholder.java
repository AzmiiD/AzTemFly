package io.github.AzmiiD.azTemFly.placeholders;

import io.github.AzmiiD.azTemFly.AzTemFly;
import io.github.AzmiiD.azTemFly.managers.ConfigManager;
import io.github.AzmiiD.azTemFly.managers.FlyTimeManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * PlaceholderAPI expansion for TempFly
 * Provides: %tfly_time_remaining%, %tfly_seconds_remaining%, %tfly_has_time%
 */
public class TempFlyPlaceholder extends PlaceholderExpansion {

    private final AzTemFly plugin;
    private final FlyTimeManager manager;
    private final ConfigManager configManager;

    public TempFlyPlaceholder(AzTemFly plugin) {
        this.plugin = plugin;
        this.manager = plugin.getFlyTimeManager();
        this.configManager = plugin.getConfigManager();
    }

    @Override
    public @NotNull String getIdentifier() {
        return "tfly";
    }

    @Override
    public @NotNull String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true; // Required to keep the expansion loaded
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) {
            return "";
        }

        // %tfly_time_remaining% - Shows formatted time remaining
        if (params.equalsIgnoreCase("time_remaining")) {
            int seconds = manager.getFlyTime(player.getUniqueId());
            if (seconds <= 0) {
                return configManager.getNoTimeFormat();
            }
            return manager.formatTime(seconds);
        }

        // %tfly_seconds_remaining% - Shows raw seconds remaining
        if (params.equalsIgnoreCase("seconds_remaining")) {
            return String.valueOf(manager.getFlyTime(player.getUniqueId()));
        }

        // %tfly_has_time% - Returns true/false
        if (params.equalsIgnoreCase("has_time")) {
            return String.valueOf(manager.hasFlyTime(player.getUniqueId()));
        }

        return null;
    }
}