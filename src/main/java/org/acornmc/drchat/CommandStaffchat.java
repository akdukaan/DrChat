package org.acornmc.drchat;

import github.scarsz.discordsrv.DiscordSRV;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CommandStaffchat implements CommandExecutor {
        ConfigManager configManager;

    public CommandStaffchat(ConfigManager configManager) {
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("drchat.staffchat")) {
            String noPerms = configManager.get().getString("messages.no-permission");
            if (noPerms != null) {
                sender.sendMessage(noPerms);
            }
            return true;
        }
        if (args.length < 1) {
            return false;
        }
        if (!(sender instanceof Player)) {
            Bukkit.getLogger().info("This command can only be used by players");
            return true;
        }
        Player player = (Player) sender;
        String message = String.join(" ", args);
        String mcToMc = configManager.get().getString("messages.staffchat.mc-to-mc-format");
        mcToMc = addPlaceholders(mcToMc, player, message);
        mcToMc = ChatColor.translateAlternateColorCodes('&', mcToMc);
        Bukkit.broadcast(mcToMc, "drchat.staffchat");
        if (Bukkit.getPluginManager().isPluginEnabled("DiscordSRV")) {
            Bukkit.getServer().getScheduler().runTaskAsynchronously(configManager.plugin, () ->
                DiscordSRV.getPlugin().processChatMessage(player, message, "staff-chat", false));
        }
        return true;
    }

    public String addPlaceholders(String format, Player player, String message) {
        format = format.replace("%name%", player.getName());
        format = format.replace("%nickname%", player.getDisplayName());
        format = format.replace("%message%", message);
        return format;
    }
}
