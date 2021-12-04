package org.acornmc.drchat;


import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.*;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import github.scarsz.discordsrv.dependencies.jda.api.entities.User;

public class DiscordSRVHook {

    @Subscribe
    public void postProcess(DiscordGuildMessagePostProcessEvent event) {

        // If it was sent in staffchat channel, send it in staffchat
        TextChannel staffchatChannel = DiscordSRV.getPlugin().getDestinationTextChannelForGameChannelName("staff-chat");
        if (staffchatChannel != null && event.getChannel().getId().equals(staffchatChannel.getId())) {
            Util.sendStaffchatDiscordToMC(event.getMember(), event.getMessage().getContentDisplay());
            event.setCancelled(true);
            return;
        }

        // If the player is muted or banned ingame, cancel the message and mute them on Discord
        User user = event.getMember().getUser();
        if (Util.isBanned(user) || Util.isMuted(user)) {
            event.setCancelled(true);
            event.getMessage().delete().queue();
            Util.discordMute(Util.getOfflinePlayer(user));
        }

        // Split the message into parts
        String eventMessage = Util.legacyOf(event.getMinecraftMessage());
        String messagePart1 = eventMessage.split("> ", 2)[0];
        String messagePart2 = eventMessage.split("> ", 2)[1];

        // Handle swears in the message
        if (Util.containsSwears(messagePart2)) {
            String username = Util.usernameOf(event.getAuthor());
            Util.notifyCancelled(username, messagePart2);
            event.setCancelled(true);
            return;
        }

        // Handle spam in the message
        String modifiedMessage = Util.modifySpam(messagePart2);
        if (!modifiedMessage.equals(messagePart2)) {
            String username = Util.usernameOf(event.getAuthor());
            Util.notifyModified(username, messagePart2);
            event.setMinecraftMessage(Util.componentOf(messagePart1 + "> " + modifiedMessage));
        }
    }
}
