package org.acornmc.drchat;


import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.*;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Message;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Role;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import github.scarsz.discordsrv.dependencies.jda.api.entities.User;
import org.bukkit.OfflinePlayer;

import java.util.List;

public class DiscordSRVHook {

    public static void discordMute(OfflinePlayer player) {
        Role muteRole = DiscordSRV.getPlugin().getMainGuild().getRoleById(Config.MUTED_ROLE_ID);
        if (muteRole == null) return;
        String id = Util.getMemberID(player);
        if (id == null) return;
        DiscordSRV.getPlugin().getMainGuild().addRoleToMember(id, muteRole).queue();
    }

    public static void discordUnmute(OfflinePlayer player) {
        Role muteRole = DiscordSRV.getPlugin().getMainGuild().getRoleById(Config.MUTED_ROLE_ID);
        if (muteRole == null) return;
        String id = Util.getMemberID(player);
        if (id == null) return;
        DiscordSRV.getPlugin().getMainGuild().removeRoleFromMember(id, muteRole).queue();
    }

    @Subscribe
    public void postProcess(DiscordGuildMessagePostProcessEvent event) {
        // If it was sent in staffchat channel, send it in staffchat
        TextChannel staffchatChannel = DiscordSRV.getPlugin().getDestinationTextChannelForGameChannelName("staff-chat");
        if (staffchatChannel != null && event.getChannel().getId().equals(staffchatChannel.getId())) {
            List<Message.Attachment> attachments = event.getMessage().getAttachments();
            Util.sendStaffchatDiscordToMC(event.getMember(), event.getMessage().getContentDisplay(), attachments);
            event.setCancelled(true);
            return;
        }

        // If the player is muted or banned ingame, cancel the message and mute them on Discord
        User user = event.getAuthor();
        if (!Util.canUseDiscord(user)) {
            event.setCancelled(true);
            event.getMessage().delete().queue();
            DiscordSRVHook.discordMute(Util.getOfflinePlayer(user));
        }

        // Split the message into parts
        String eventMessage = event.getProcessedMessage();
        String messageSplitter = Config.MESSAGE_SPLITTER;
        String[] splitMessage = eventMessage.split(messageSplitter, 2);
        String messagePart1 = "";
        String messagePart2 = eventMessage;
        if (splitMessage.length == 2) {
            messagePart1 = eventMessage.split(messageSplitter, 2)[0];
            messagePart2 = eventMessage.split(messageSplitter, 2)[1];
        } else {
            Util.log("&4Error: Message splitter does not exist in the message that was sent.");
        }

        // Remove spam
        String modifiedMessage = Util.filterMessage(messagePart2);

        // Handle swears in the message
        if (Util.containsSwears(messagePart2)) {
            String username = Util.usernameOf(user);
            Util.notifyCancelled(username, messagePart2);
            event.setCancelled(true);
            Util.punishForSwearing(Util.getOfflinePlayer(user));
            return;
        }

        // Either reward or notify modified
        if (modifiedMessage.equals(messagePart2)) {
            Util.tryRewarding(Util.uuidOf(user));
        } else {
            String username = Util.usernameOf(event.getAuthor());
            Util.notifyModified(username, messagePart2);
            event.setProcessedMessage(messagePart1 + messageSplitter + modifiedMessage);
        }
    }
}
