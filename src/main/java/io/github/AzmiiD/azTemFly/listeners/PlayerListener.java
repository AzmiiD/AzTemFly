package io.github.AzmiiD.azTemFly.listeners;

import io.github.AzmiiD.azTemFly.AzTemFly;
import io.github.AzmiiD.azTemFly.managers.FlyTimeManager;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;

/**
 * Handles player events related to temporary fly
 */
public class PlayerListener implements Listener {

    private final AzTemFly plugin;
    private final FlyTimeManager manager;

    public PlayerListener(AzTemFly plugin) {
        this.plugin = plugin;
        this.manager = plugin.getFlyTimeManager();
    }

    /**
     * When a player joins, restore their flight if they have time remaining
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Delay to ensure other plugins (like Essentials) load first
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            manager.handlePlayerJoin(event.getPlayer());
        }, 20L); // Wait 1 second
    }

    /**
     * When a player quits, save their data
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        manager.handlePlayerQuit(event.getPlayer());
    }

    /**
     * Prevent players without fly time from toggling flight
     * (unless they're in creative/spectator mode or have permission from another plugin)
     *
     * IMPORTANT: This runs at LOW priority to let other plugins (like Essentials) handle first
     */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
        var player = event.getPlayer();

        // Allow creative and spectator mode players to fly normally
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }

        // If player is trying to DISABLE flight, always allow it
        if (!event.isFlying()) {
            return;
        }

        // If trying to enable flight, only check if player doesn't have TempFly time
        // This allows Essentials or other plugins to work independently
        if (!manager.hasFlyTime(player.getUniqueId())) {
            // Player doesn't have TempFly time, let other plugins handle it
            // Don't cancel here to avoid conflicts
            return;
        }

        // Player has TempFly time, ensure they can fly
        if (!player.hasPermission("tfly.use")) {
            event.setCancelled(true);
            player.setAllowFlight(false);
        }
    }

    /**
     * When a player changes gamemode, handle their flight status
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onGameModeChange(PlayerGameModeChangeEvent event) {
        var player = event.getPlayer();
        GameMode newMode = event.getNewGameMode();

        // If changing to survival/adventure, check if they should have flight
        if (newMode == GameMode.SURVIVAL || newMode == GameMode.ADVENTURE) {
            // Schedule this to run next tick to ensure gamemode change is complete
            // and to avoid conflicts with other plugins
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (manager.hasFlyTime(player.getUniqueId()) && player.hasPermission("tfly.use")) {
                    player.setAllowFlight(true);
                }
                // If player doesn't have TempFly time, don't touch their flight
                // (let Essentials or other plugins manage it)
            }, 2L);
        }
    }
}