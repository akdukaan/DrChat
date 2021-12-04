package org.acornmc.drchat;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        // If the message was sent to staffchat, send it in staffchat
        if (Util.isStaffchatToggled(player.getUniqueId())) {
            Util.sendStaffchatMCToDiscord(player, event.getMessage());
            Util.sendStaffchatMCToMC(player, event.getMessage());
            event.setCancelled(true);
            return;
        }

        // If the player talked, we know they shouldn't be muted on Discord
        DiscordSRVHook.discordUnmute(player);

        // Remove fancychat
        String message = event.getMessage();
        Util.removeFancyChat(message);

        // Handle swears in the message
        if (Util.containsSwears(message)) {
            Util.notifyCancelled(player.getName(), message);
            event.setCancelled(true);
            Util.punishForSwearing(event.getPlayer());
            return;
        }

        // Handle spam in the message
        String modifiedMessage = Util.filterMessage(message);
        if (!modifiedMessage.equals(message)) {
            Util.notifyModified(player.getName(), message);
            event.setMessage(modifiedMessage);
        }
    }
}
