package org.acornmc.drchat;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class PlayerChatListener extends ChatManager implements Listener {

    public PlayerChatListener(ConfigManager configManager) {
        super(configManager);
    }

    @EventHandler
    public void eventsMessageSend(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPermission("drchat.bypass.frequency") && isTooFrequent(player)) {
            event.setCancelled(true);
            String command = configManager.get().getString("checks.frequency.command");
            if (command != null) {
                command = command.replace("<player>", player.getName());
                final String finalCommand = command;
                Runnable runnable = () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCommand);
                Bukkit.getScheduler().runTask(configManager.plugin, runnable);
            }
            Bukkit.getLogger().info(player.getName() + " tried speaking too fast.\nMessage: " + event.getMessage());
            String cancelMessage = configManager.get().getString("messages.cancel-notification");
            if (cancelMessage != null) {
                cancelMessage = ChatColor.translateAlternateColorCodes('&', cancelMessage);
                cancelMessage = cancelMessage.replace("%player%", player.getName());
                cancelMessage = cancelMessage.replace("%original-message%", event.getMessage());
                Bukkit.broadcast(cancelMessage, "drchat.notify.cancel");
            }
        } else {
            increment(player);
            String newMessage = event.getMessage();
            if (!player.hasPermission("drchat.bypass.font")) {
                newMessage = fixFont(newMessage);
            }
            if (!player.hasPermission("drchat.bypass.spacing")) {
                newMessage = fixSpacing(newMessage);
            }
            if (!player.hasPermission("drchat.bypass.capital")) {
                newMessage = fixCapital(newMessage);
            }
            if (!player.hasPermission("drchat.bypass.character")) {
                newMessage = fixCharacter(newMessage);
            }
            if (!newMessage.equals(event.getMessage())) {
                Bukkit.getLogger().info("DrChat modified " + player.getName() + "'s message.");
                Bukkit.getLogger().info("Original message: " + event.getMessage());
                String notifyMessage = configManager.get().getString("messages.modify-notification");
                if (notifyMessage != null) {
                    notifyMessage = ChatColor.translateAlternateColorCodes('&', notifyMessage);
                    notifyMessage = notifyMessage.replace("%player%", player.getName());
                    notifyMessage = notifyMessage.replace("%original-message%", event.getMessage());
                    Bukkit.broadcast(notifyMessage, "drchat.notify.modify");
                }
                event.setMessage(newMessage);
            }
        }
    }
}
