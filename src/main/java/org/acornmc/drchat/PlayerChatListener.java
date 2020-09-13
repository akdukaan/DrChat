package org.acornmc.drchat;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitScheduler;

public class PlayerChatListener extends ChatManager implements Listener {

    public PlayerChatListener(ConfigManager configManager) {
        super(configManager);
    }

    @EventHandler
    public void eventsMessageSend(AsyncPlayerChatEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Player player = event.getPlayer();
        if (chatIsFrozen()) {
            if (!player.hasPermission("drchat.bypass.freeze")) {
                event.setCancelled(true);
                String chatFrozenMessage = configManager.get().getString("messages.frozen.notify");
                if (chatFrozenMessage != null) {
                    chatFrozenMessage = ChatColor.translateAlternateColorCodes('&', chatFrozenMessage);
                    player.sendMessage(chatFrozenMessage);
                }
                return;
            }
        }
        if (ManagerStaffchat.isToggled(player.getUniqueId())) {
            event.setCancelled(true);
            String mcToMc = configManager.get().getString("messages.staffchat.mc-to-mc-format");
            ManagerStaffchat.sendMinecraft(event.getMessage(), event.getPlayer(), mcToMc);
            ManagerStaffchat.sendDiscord(event.getMessage(), event.getPlayer());
            return;
        }
        String playerName = player.getName();
        if (!player.hasPermission("drchat.bypass.frequency") && isTooFrequent(player)) {
            event.setCancelled(true);
            useTooFrequentCommands(player);
            notifyCancelledMessage(playerName, event.getMessage());
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
            if (!player.hasPermission("drchat.bypass.swear") && hasSwear(newMessage)) {
                event.setCancelled(true);
                useSwearCommands(player);
                notifyCancelledMessage(playerName, event.getMessage());
            }
            if (!newMessage.equals(event.getMessage())) {
                notifyModifiedMessage(playerName, event.getMessage());
                event.setMessage(newMessage);
            }
            reward(player);
            String trigger = configManager.get().getString("search.trigger");
            if (trigger != null) {
                if (newMessage.startsWith(trigger)) {
                    newMessage = newMessage.substring(trigger.length());
                    newMessage = newMessage.replace(" ", "%20");
                    String url = configManager.get().getString("messages.search");
                    url = url + newMessage;
                    String finalUrl = ChatColor.translateAlternateColorCodes('&', url);
                    BukkitScheduler scheduler = configManager.plugin.getServer().getScheduler();
                    scheduler.scheduleSyncDelayedTask(configManager.plugin, () ->
                            player.sendMessage(finalUrl), 1L);
                }
            }
        }
    }
}
