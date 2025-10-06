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

import java.util.*;
import java.util.stream.Collectors;

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

        // Cek izin
        if (!sender.hasPermission("tfly.give") && !sender.hasPermission("tfly.use")) {
            sender.sendMessage(configManager.getMessage("no-permission"));
            return true;
        }

        // Handle toggle (player only): /tfly on/off
        if (args.length == 1 && (args[0].equalsIgnoreCase("on") || args[0].equalsIgnoreCase("off"))) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(configManager.getMessage("player-only"));
                return true;
            }

            if (!player.hasPermission("tfly.use")) {
                player.sendMessage(configManager.getMessage("no-permission"));
                return true;
            }

            // Cek apakah punya waktu fly
            if (!manager.hasFlyTime(player.getUniqueId())) {
                player.sendMessage(configManager.getMessage("no-time-remaining"));
                return true;
            }

            boolean enable = args[0].equalsIgnoreCase("on");
            manager.setFlyEnabled(player.getUniqueId(), enable);

            if (enable) {
                player.sendMessage(configManager.getMessage("fly-enabled"));
            } else {
                player.sendMessage(configManager.getMessage("fly-disabled"));
            }
            return true;
        }

        // Handle give time: /tfly <player> <seconds>
        if (!sender.hasPermission("tfly.give")) {
            sender.sendMessage(configManager.getMessage("no-permission"));
            return true;
        }

        if (args.length != 2) {
            sender.sendMessage(configManager.getMessage("usage-tfly"));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("player", args[0]);
            sender.sendMessage(configManager.getMessage("player-not-found", placeholders));
            return true;
        }

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

        if (!target.hasPermission("tfly.use")) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("player", target.getName());
            sender.sendMessage(configManager.getMessage("player-no-permission", placeholders));
            return true;
        }

        manager.addFlyTime(target.getUniqueId(), seconds);
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
            // Tambahkan toggle on/off untuk player
            if (sender.hasPermission("tfly.use")) {
                completions.add("on");
                completions.add("off");
            }

            // Tambahkan nama player untuk admin yang punya tfly.give
            if (sender.hasPermission("tfly.give")) {
                completions.addAll(
                        Bukkit.getOnlinePlayers().stream()
                                .map(Player::getName)
                                .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                                .collect(Collectors.toList())
                );
            }

        } else if (args.length == 2 && sender.hasPermission("tfly.give")) {
            // Saran durasi umum (untuk admin give)
            completions.addAll(Arrays.asList("60", "300", "600", "1800", "3600"));
        }

        return completions;
    }
}
