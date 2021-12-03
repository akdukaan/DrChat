package org.acornmc.drchat;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (Util.isStaffchatToggled(player.getUniqueId())) {
            Util.sendStaffchatMCToDiscord(player, event.getMessage());
            Util.sendStaffchatMCToMC(player, event.getMessage());
            event.setCancelled(true);
            return;
        }

        String message = event.getMessage();
        if (Util.containsSwears(message)) {
            Util.notifyCancelled(player.getName(), message);
            event.setCancelled(true);
            return;
        }

        String modifiedMessage = Util.modifySpam(message);
        if (!modifiedMessage.equals(message)) {
            Util.notifyModified(player.getName(), message);
            event.setMessage(modifiedMessage);
        }
    }
}
