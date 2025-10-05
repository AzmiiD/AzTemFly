package io.github.AzmiiD.azTemFly;

import io.github.AzmiiD.azTemFly.commands.TFlyCommand;
import io.github.AzmiiD.azTemFly.commands.TFlyTimeCommand;
import io.github.AzmiiD.azTemFly.commands.TFlyReloadCommand;
import io.github.AzmiiD.azTemFly.listeners.PlayerListener;
import io.github.AzmiiD.azTemFly.managers.ConfigManager;
import io.github.AzmiiD.azTemFly.managers.FlyTimeManager;
import io.github.AzmiiD.azTemFly.placeholders.TempFlyPlaceholder;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * TempFly - Main plugin class
 * Manages temporary fly time for players as vote rewards
 */
public final class AzTemFly extends JavaPlugin {

    private ConfigManager configManager;
    private FlyTimeManager flyTimeManager;

    @Override
    public void onEnable() {
        // Initialize config manager first
        configManager = new ConfigManager(this);

        // Initialize fly time manager
        flyTimeManager = new FlyTimeManager(this);

        // Load data
        flyTimeManager.loadData();

        // Register commands
        getCommand("tfly").setExecutor(new TFlyCommand(this));
        getCommand("tflytime").setExecutor(new TFlyTimeCommand(this));
        getCommand("tflyreload").setExecutor(new TFlyReloadCommand(this));

        // Register listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        // Register PlaceholderAPI expansion if available
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new TempFlyPlaceholder(this).register();
            getLogger().info("PlaceholderAPI hook registered!");
        }

        // Start fly time countdown task (runs every second)
        flyTimeManager.startCountdownTask();

        getLogger().info("TempFly has been enabled!");
    }

    @Override
    public void onDisable() {
        // Save data before shutdown
        if (flyTimeManager != null) {
            flyTimeManager.saveData();
            flyTimeManager.stopCountdownTask();
        }

        getLogger().info("TempFly has been disabled!");
    }

    /**
     * Get the ConfigManager instance
     */
    public ConfigManager getConfigManager() {
        return configManager;
    }

    /**
     * Get the FlyTimeManager instance
     */
    public FlyTimeManager getFlyTimeManager() {
        return flyTimeManager;
    }
}
