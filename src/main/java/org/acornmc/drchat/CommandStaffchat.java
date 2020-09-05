package org.acornmc.drchat;

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
        if (!(sender instanceof Player)) {
            Bukkit.getLogger().info("This command can only be used by players");
            return true;
        }
        Player player = (Player) sender;
        if (args.length < 1) {
            ManagerStaffchat.toggle(player);
            String toggleMessage;
            if (ManagerStaffchat.isToggled(player.getUniqueId())) {
                toggleMessage = configManager.get().getString("messages.staffchat.toggle-on");
            } else {
                toggleMessage = configManager.get().getString("messages.staffchat.toggle-off");
            }
            if (toggleMessage != null) {
                toggleMessage = ChatColor.translateAlternateColorCodes('&', toggleMessage);
                player.sendMessage(toggleMessage);
            }
            return true;
        }
        String message = String.join(" ", args);
        String mcToMc = configManager.get().getString("messages.staffchat.mc-to-mc-format");
        ManagerStaffchat.sendMinecraft(message, player, mcToMc);
        ManagerStaffchat.sendDiscord(message, player);
        return true;
    }
}
