package io.github.AzmiiD.azTemFly.commands;

import io.github.AzmiiD.azTemFly.AzTemFly;
import io.github.AzmiiD.azTemFly.managers.ConfigManager;
import io.github.AzmiiD.azTemFly.managers.FlyTimeManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Command: /tfly <player> <seconds>
 * Gives temporary fly time to a player
 */
public class TFlyCommand implements CommandExecutor, TabCompleter {

    private final AzTemFly plugin;
    private final FlyTimeManager manager;
    private final ConfigManager configManager;

    public TFlyCommand(AzTemFly plugin) {
        this.plugin = plugin;
        this.manager = plugin.getFlyTimeManager();
        this.configManager = plugin.getConfigManager();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {

        // Check permission
        if (!sender.hasPermission("tfly.give")) {
            sender.sendMessage(configManager.getMessage("no-permission"));
            return true;
        }

        // Check arguments
        if (args.length != 2) {
            sender.sendMessage(configManager.getMessage("usage-tfly"));
            return true;
        }

        // Get target player
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("player", args[0]);
            sender.sendMessage(configManager.getMessage("player-not-found", placeholders));
            return true;
        }

        // Parse seconds
        int seconds;
        try {
            seconds = Integer.parseInt(args[1]);
            if (seconds <= 0) {
                sender.sendMessage(configManager.getMessage("must-be-positive"));
                return true;
            }
        } catch (NumberFormatException e) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("input", args[1]);
            sender.sendMessage(configManager.getMessage("invalid-number", placeholders));
            return true;
        }

        // Check if target has permission to use fly
        if (!target.hasPermission("tfly.use")) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("player", target.getName());
            sender.sendMessage(configManager.getMessage("player-no-permission", placeholders));
            return true;
        }

        // Add fly time
        manager.addFlyTime(target.getUniqueId(), seconds);

        // Send messages
        String formattedTime = manager.formatTime(seconds);

        Map<String, String> targetPlaceholders = new HashMap<>();
        targetPlaceholders.put("time", formattedTime);
        target.sendMessage(configManager.getMessage("fly-time-received", targetPlaceholders));

        Map<String, String> senderPlaceholders = new HashMap<>();
        senderPlaceholders.put("time", formattedTime);
        senderPlaceholders.put("player", target.getName());
        sender.sendMessage(configManager.getMessage("fly-time-given", senderPlaceholders));

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                @NotNull String label, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // Suggest online player names
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            // Suggest common time values
            completions.add("60");    // 1 minute
            completions.add("300");   // 5 minutes
            completions.add("600");   // 10 minutes
            completions.add("1800");  // 30 minutes
            completions.add("3600");  // 1 hour
        }

        return completions;
    }
}