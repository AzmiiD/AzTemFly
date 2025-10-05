package io.github.AzmiiD.azTemFly.commands;

import io.github.AzmiiD.azTemFly.AzTemFly;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * Command: /tflyreload
 * Reloads the plugin configuration
 */
public class TFlyReloadCommand implements CommandExecutor {

    private final AzTemFly plugin;

    public TFlyReloadCommand(AzTemFly plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {

        // Check permission
        if (!sender.hasPermission("tfly.reload")) {
            sender.sendMessage(Component.text("You don't have permission to use this command!", NamedTextColor.RED));
            return true;
        }

        // Reload configuration
        plugin.getConfigManager().loadConfig();

        sender.sendMessage(Component.text("TempFly configuration reloaded!", NamedTextColor.GREEN));

        return true;
    }
}