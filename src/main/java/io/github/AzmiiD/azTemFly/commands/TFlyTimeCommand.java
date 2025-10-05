package io.github.AzmiiD.azTemFly.commands;

import io.github.AzmiiD.azTemFly.AzTemFly;
import io.github.AzmiiD.azTemFly.managers.FlyTimeManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Command: /tflytime
 * Shows the player's remaining fly time
 */
public class TFlyTimeCommand implements CommandExecutor {

    private final AzTemFly plugin;
    private final FlyTimeManager manager;

    public TFlyTimeCommand(AzTemFly plugin) {
        this.plugin = plugin;
        this.manager = plugin.getFlyTimeManager();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {

        // Must be a player
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("This command can only be used by players!")
                    .color(NamedTextColor.RED));
            return true;
        }

        // Check permission
        if (!player.hasPermission("tfly.use")) {
            player.sendMessage(Component.text("You don't have permission to use this command!")
                    .color(NamedTextColor.RED));
            return true;
        }

        // Get remaining time
        int remainingSeconds = manager.getFlyTime(player.getUniqueId());

        if (remainingSeconds <= 0) {
            player.sendMessage(Component.text("You have no temporary fly time remaining.", NamedTextColor.YELLOW));
        } else {
            String formattedTime = manager.formatTime(remainingSeconds);
            Component message = Component.text("Remaining fly time: ", NamedTextColor.GREEN)
                    .append(Component.text(formattedTime, NamedTextColor.YELLOW))
                    .append(Component.text(" (", NamedTextColor.GRAY))
                    .append(Component.text(remainingSeconds + " seconds", NamedTextColor.GRAY))
                    .append(Component.text(")", NamedTextColor.GRAY));
            player.sendMessage(message);
        }

        return true;
    }
}