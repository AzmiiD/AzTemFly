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
        manager.handlePlayerJoin(event.getPlayer());
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
     * (unless they're in creative/spectator mode)
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
        var player = event.getPlayer();

        // Allow creative and spectator mode players to fly normally
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }

        // If trying to enable flight
        if (event.isFlying()) {
            // Check if player has fly time and permission
            if (!manager.hasFlyTime(player.getUniqueId()) || !player.hasPermission("tfly.use")) {
                event.setCancelled(true);
                player.setAllowFlight(false);
            }
        }
    }

    /**
     * When a player changes gamemode, handle their flight status
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onGameModeChange(PlayerGameModeChangeEvent event) {
        var player = event.getPlayer();
        GameMode newMode = event.getNewGameMode();

        // If changing to survival/adventure, check if they should have flight
        if (newMode == GameMode.SURVIVAL || newMode == GameMode.ADVENTURE) {
            // Schedule this to run next tick to ensure gamemode change is complete
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (manager.hasFlyTime(player.getUniqueId()) && player.hasPermission("tfly.use")) {
                    player.setAllowFlight(true);
                } else {
                    player.setAllowFlight(false);
                    player.setFlying(false);
                }
            }, 1L);
        }
    }
}