package org.acornmc.drchat;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CommandDrChat implements CommandExecutor {
    ConfigManager configManager;

    public CommandDrChat(ConfigManager configManager) {
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (args.length == 0) {
            return false;
        }
        if (args[0].equals("reload")) {
            if (!sender.hasPermission("drchat.reload")) {
                String noPerms = configManager.get().getString("messages.no-permission");
                if (noPerms != null) {
                    noPerms = ChatColor.translateAlternateColorCodes('&', noPerms);
                    sender.sendMessage(noPerms);
                    return true;
                }
                if (command.getPermissionMessage() != null) {
                    sender.sendMessage(command.getPermissionMessage());
                }
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
        return false;
    }
}
