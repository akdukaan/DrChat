package org.acornmc.drchat;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CommandDrChat implements CommandExecutor {
    ConfigManager configManager;

    public CommandDrChat(ConfigManager configManager) {
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            return false;
        }
        String subcommand = args[0].toLowerCase();
        if (subcommand.equals("reload")) {
            if (!sender.hasPermission("drchat.reload")) {
                noPerms(sender);
                return true;
            }
            configManager.reload();
            String reloadMessage = configManager.get().getString("messages.reload");
            if (reloadMessage != null) {
                reloadMessage = ChatColor.translateAlternateColorCodes('&', reloadMessage);
                sender.sendMessage(reloadMessage);
            }
            return true;
        }
        if (subcommand.equals("freeze")) {
            if (!sender.hasPermission("drchat.freeze")) {
                noPerms(sender);
                return true;
            }
            ChatManager.toggleChatFreeze();
            String message;
            if (ChatManager.chatIsFrozen()) {
                message = configManager.get().getString("messages.freeze.toggle-on");
            } else {
                message = configManager.get().getString("messages.freeze.toggle-off");
            }
            if (message != null) {
                message = ChatColor.translateAlternateColorCodes('&', message);
                sender.sendMessage(message);
            }
            return true;
        }
        if (subcommand.equals("clear")) {
            if (!sender.hasPermission("drchat.clear")) {
                noPerms(sender);
                return true;
            }
            if (args.length == 1) {
                int lineCount = configManager.get().getInt("messages.clear.lines");
                for (int i = 0; i < lineCount; i++) {
                    Bukkit.broadcastMessage("");
                }
                String clearMessage = configManager.get().getString("messages.clear.all");
                if (clearMessage != null) {
                    clearMessage = ChatColor.translateAlternateColorCodes('&', clearMessage);
                    sender.sendMessage(clearMessage);
                }
                return true;
            }
            Player targetPlayer = Bukkit.getPlayer(args[1]);
            if (targetPlayer == null) {
                String invalidPlayer = configManager.get().getString("messages.clear.invalid-player");
                if (invalidPlayer != null) {
                    invalidPlayer = ChatColor.translateAlternateColorCodes('&', invalidPlayer);
                    sender.sendMessage(invalidPlayer);
                }
                return true;
            }
            int lineCount = configManager.get().getInt("messages.clear.lines");
            for (int i = 0; i < lineCount; i++) {
                targetPlayer.sendMessage("");
            }
            String clearMessage = configManager.get().getString("messages.clear.player");
            if (clearMessage != null) {
                clearMessage = ChatColor.translateAlternateColorCodes('&', clearMessage);
                clearMessage = clearMessage.replace("%player%", targetPlayer.getName());
                sender.sendMessage(clearMessage);
            }
            return true;
        }
        if (subcommand.equals("broadcast")) {
            if (!sender.hasPermission("messages.broadcast")) {
                noPerms(sender);
                return true;
            }
            if (args.length < 3) {
                return false;
            }
            String perm = args[1];
            String o = "This is a string";
            String[] p = o.split(" ", 2);
            String message = "";
            for (int i = 2; i < args.length; i++) {
                message = message + args[i] + " ";
            }
            message = ChatManager.convertHex(message);
            message = ChatColor.translateAlternateColorCodes('&', message);
            Bukkit.broadcast(message, perm);
            return true;
        }
        return false;
    }

    public void noPerms(CommandSender sender) {
        String noPerms = configManager.get().getString("messages.no-permission");
        if (noPerms != null) {
            noPerms = ChatColor.translateAlternateColorCodes('&', noPerms);
            sender.sendMessage(noPerms);
        }
    }
}
