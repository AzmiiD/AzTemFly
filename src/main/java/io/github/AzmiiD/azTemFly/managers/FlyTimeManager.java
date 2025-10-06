package io.github.AzmiiD.azTemFly.managers;

import io.github.AzmiiD.azTemFly.AzTemFly;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages fly time for all players
 * Handles saving, loading, and countdown operations
 */
public class FlyTimeManager {

    private final AzTemFly plugin;
    private final ConfigManager configManager;
    private final Map<UUID, Integer> flyTimeMap; // UUID -> remaining seconds
    private final Map<UUID, Boolean> flyToggleMap; // UUID -> is fly enabled (true/false)
    private final Map<UUID, Boolean> lowTimeWarned; // Track if player was warned
    private final File dataFile;
    private FileConfiguration dataConfig;
    private BukkitTask countdownTask;

    public FlyTimeManager(AzTemFly plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.flyTimeMap = new HashMap<>();
        this.flyToggleMap = new HashMap<>();
        this.lowTimeWarned = new HashMap<>();
        this.dataFile = new File(plugin.getDataFolder(), "flydata.yml");
    }

    /**
     * Add fly time to a player
     * @param uuid Player's UUID
     * @param seconds Seconds to add
     */
    public void addFlyTime(UUID uuid, int seconds) {
        int currentTime = flyTimeMap.getOrDefault(uuid, 0);
        flyTimeMap.put(uuid, currentTime + seconds);
        flyToggleMap.put(uuid, true); // Auto-enable fly when time is given
        lowTimeWarned.remove(uuid); // Reset warning when time is added

        // Enable flight for online players
        Player player = Bukkit.getPlayer(uuid);
        if (player != null && player.isOnline()) {
            enableFlight(player);
        }
    }

    /**
     * Toggle fly on/off for a player
     * @param uuid Player's UUID
     * @param enabled true to enable, false to disable
     */
    public void setFlyEnabled(UUID uuid, boolean enabled) {
        if (!hasFlyTime(uuid)) {
            return; // Can't toggle if no time
        }

        flyToggleMap.put(uuid, enabled);
        Player player = Bukkit.getPlayer(uuid);
        if (player != null && player.isOnline()) {
            if (enabled) {
                enableFlight(player);
            } else {
                disableFlight(player);
            }
        }
    }

    /**
     * Check if player has fly enabled (not just has time)
     * @param uuid Player's UUID
     * @return true if fly is enabled
     */
    public boolean isFlyEnabled(UUID uuid) {
        return flyToggleMap.getOrDefault(uuid, false);
    }

    /**
     * Get remaining fly time for a player
     * @param uuid Player's UUID
     * @return Remaining seconds
     */
    public int getFlyTime(UUID uuid) {
        return flyTimeMap.getOrDefault(uuid, 0);
    }

    /**
     * Check if player has fly time
     * @param uuid Player's UUID
     * @return true if player has fly time
     */
    public boolean hasFlyTime(UUID uuid) {
        return getFlyTime(uuid) > 0;
    }

    /**
     * Format seconds into readable time format
     * @param seconds Total seconds
     * @return Formatted string like "2m 30s" or "1h 5m 30s"
     */
    public String formatTime(int seconds) {
        if (seconds <= 0) return "0" + configManager.getSecondLabel();

        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int secs = seconds % 60;

        StringBuilder sb = new StringBuilder();
        if (hours > 0 && configManager.shouldShowHours()) {
            sb.append(hours).append(configManager.getHourLabel()).append(" ");
        }
        if (minutes > 0 && configManager.shouldShowMinutes()) {
            sb.append(minutes).append(configManager.getMinuteLabel()).append(" ");
        }
        if ((secs > 0 || sb.length() == 0) && configManager.shouldShowSeconds()) {
            sb.append(secs).append(configManager.getSecondLabel());
        }

        return sb.toString().trim();
    }

    /**
     * Enable flight for a player
     */
    private void enableFlight(Player player) {
        if (player.getGameMode() != GameMode.CREATIVE && player.getGameMode() != GameMode.SPECTATOR) {
            player.setAllowFlight(true);
            player.setFlying(true);
        }
    }

    /**
     * Disable flight for a player
     * Only disable if they don't have other fly permissions
     */
    private void disableFlight(Player player) {
        // Don't disable if in Creative/Spectator
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }

        // Check compatibility permissions from config
        for (String permission : configManager.getCompatibilityPermissions()) {
            if (player.hasPermission(permission)) {
                // Player has fly from another plugin, don't disable
                return;
            }
        }

        // Safe to disable - player has no other fly source
        player.setAllowFlight(false);
        player.setFlying(false);
    }

    /**
     * Start the countdown task that decreases fly time every second
     */
    public void startCountdownTask() {
        int autoSaveInterval = configManager.getAutoSaveInterval() * 20; // Convert to ticks

        countdownTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (UUID uuid : new HashMap<>(flyTimeMap).keySet()) {
                int currentTime = flyTimeMap.get(uuid);

                if (currentTime <= 0) {
                    flyTimeMap.remove(uuid);
                    flyToggleMap.remove(uuid);
                    lowTimeWarned.remove(uuid);
                    continue;
                }

                // Check for low time warning
                if (configManager.isLowTimeWarningEnabled() && currentTime > 0) {
                    int threshold = configManager.getLowTimeWarningThreshold();
                    if (currentTime <= threshold && !lowTimeWarned.getOrDefault(uuid, false)) {
                        Player player = Bukkit.getPlayer(uuid);
                        if (player != null && player.isOnline()) {
                            String formattedTime = formatTime(currentTime);
                            player.sendMessage(configManager.getLowTimeWarningMessage(formattedTime));
                            lowTimeWarned.put(uuid, true);
                        }
                    }
                }

                // Decrease time by 1 second
                currentTime--;
                flyTimeMap.put(uuid, currentTime);

                // Check if time expired
                if (currentTime <= 0) {
                    flyTimeMap.remove(uuid);
                    flyToggleMap.remove(uuid);
                    lowTimeWarned.remove(uuid);
                    Player player = Bukkit.getPlayer(uuid);
                    if (player != null && player.isOnline()) {
                        disableFlight(player);
                        player.sendMessage(configManager.getMessage("fly-time-expired"));
                    }
                }
            }

            // Auto-save at configured interval
            if (configManager.isAutoSaveEnabled() && Bukkit.getCurrentTick() % autoSaveInterval == 0) {
                saveData();
                if (configManager.isDebugEnabled()) {
                    plugin.getLogger().info("Auto-saved fly time data.");
                }
            }
        }, 20L, 20L); // Run every second (20 ticks)
    }

    /**
     * Stop the countdown task
     */
    public void stopCountdownTask() {
        if (countdownTask != null) {
            countdownTask.cancel();
        }
    }

    /**
     * Load fly time data from file
     */
    public void loadData() {
        if (!dataFile.exists()) {
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create flydata.yml!");
                e.printStackTrace();
                return;
            }
        }

        dataConfig = YamlConfiguration.loadConfiguration(dataFile);

        // Load all stored fly times
        if (dataConfig.contains("flytime")) {
            for (String key : dataConfig.getConfigurationSection("flytime").getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(key);
                    int seconds = dataConfig.getInt("flytime." + key);
                    if (seconds > 0) {
                        flyTimeMap.put(uuid, seconds);
                    }
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid UUID in data file: " + key);
                }
            }
        }

        // Load fly toggle states
        if (dataConfig.contains("flytoggle")) {
            for (String key : dataConfig.getConfigurationSection("flytoggle").getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(key);
                    boolean enabled = dataConfig.getBoolean("flytoggle." + key, true);
                    flyToggleMap.put(uuid, enabled);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid UUID in toggle data: " + key);
                }
            }
        }

        plugin.getLogger().info("Loaded fly time data for " + flyTimeMap.size() + " players.");
    }

    /**
     * Save fly time data to file
     */
    public void saveData() {
        if (dataConfig == null) {
            dataConfig = new YamlConfiguration();
        }

        // Clear old data
        dataConfig.set("flytime", null);
        dataConfig.set("flytoggle", null);

        // Save current fly times
        for (Map.Entry<UUID, Integer> entry : flyTimeMap.entrySet()) {
            dataConfig.set("flytime." + entry.getKey().toString(), entry.getValue());
        }

        // Save fly toggle states
        for (Map.Entry<UUID, Boolean> entry : flyToggleMap.entrySet()) {
            dataConfig.set("flytoggle." + entry.getKey().toString(), entry.getValue());
        }

        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save flydata.yml!");
            e.printStackTrace();
        }
    }

    /**
     * Handle player join - restore flight if they have time
     */
    public void handlePlayerJoin(Player player) {
        if (hasFlyTime(player.getUniqueId()) && player.hasPermission("tfly.use")) {
            // Only enable if fly toggle is ON
            if (isFlyEnabled(player.getUniqueId())) {
                enableFlight(player);
            }
        }
    }

    /**
     * Handle player quit - save their data
     */
    public void handlePlayerQuit(Player player) {
        saveData();
    }
}