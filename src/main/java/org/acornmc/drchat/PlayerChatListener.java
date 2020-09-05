package org.acornmc.drchat;

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
        if (event.isCancelled()) {
            return;
        }
        if (ManagerStaffchat.isToggled(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
            return;
        }
        Player player = event.getPlayer();
        String playerName = player.getName();
        if (!player.hasPermission("drchat.bypass.frequency") && isTooFrequent(player)) {
            event.setCancelled(true);
            useTooFrequentCommand(player);
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
            if (!newMessage.equals(event.getMessage())) {
                notifyModifiedMessage(playerName, event.getMessage());
                event.setMessage(newMessage);
            }
            reward(player);
        }
    }
}
