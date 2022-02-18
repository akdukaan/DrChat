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

        if (Util.getFreezeStatus() && !player.hasPermission("drchat.freeze.exempt")) {
            Util.send(player, Lang.CANT_TALK_NOW);
            event.setCancelled(true);
            Util.log(player.getName() + " tried to say " + event.getMessage());
            return;
        }

        // If the player talked, we know they shouldn't be muted on Discord
        DiscordSRVHook.discordUnmute(player);

        // Remove spam
        String message = event.getMessage();
        String modifiedMessage = Util.filterMessage(message);

        // Handle swears in the message
        if (Util.containsSwears(message)) {
            Util.notifyCancelled(player.getName(), message);
            event.setCancelled(true);
            Util.punishForSwearing(event.getPlayer());
            return;
        }

        if (Util.isTooFrequent(player)) {
            event.setCancelled(true);
            Util.send(player, "&4Please don't spam");
            Util.punishFrequency(player);
            return;
        }

        // Either reward or notify modified
        if (modifiedMessage.equals(message)) {
            Util.tryRewarding(player.getUniqueId());
        } else {
            Util.notifyModified(player.getName(), message);
            event.setMessage(modifiedMessage);
        }
    }
}
