package org.acornmc.drchat;


import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.*;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Role;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import github.scarsz.discordsrv.dependencies.jda.api.entities.User;
import org.bukkit.OfflinePlayer;

public class DiscordSRVHook {

    public static void discordMute(OfflinePlayer player) {
        Role muteRole = DiscordSRV.getPlugin().getMainGuild().getRoleById(Config.MUTED_ROLE_ID);
        if (muteRole == null) return;
        DiscordSRV.getPlugin().getMainGuild().addRoleToMember(Util.getMemberID(player), muteRole).queue();
    }

    public static void discordUnmute(OfflinePlayer player) {
        Role muteRole = DiscordSRV.getPlugin().getMainGuild().getRoleById(Config.MUTED_ROLE_ID);
        if (muteRole == null) return;
        DiscordSRV.getPlugin().getMainGuild().removeRoleFromMember(Util.getMemberID(player), muteRole).queue();
    }

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
        User user = event.getAuthor();
        if (!Util.canUseDiscord(user)) {
            event.setCancelled(true);
            event.getMessage().delete().queue();
            DiscordSRVHook.discordMute(Util.getOfflinePlayer(user));
        }

        // Split the message into parts
        String eventMessage = Util.legacyOf(event.getMinecraftMessage());
        String messagePart1 = eventMessage.split("> ", 2)[0];
        String messagePart2 = eventMessage.split("> ", 2)[1];

        // Handle swears in the message
        if (Util.containsSwears(messagePart2)) {
            String username = Util.usernameOf(user);
            Util.notifyCancelled(username, messagePart2);
            event.setCancelled(true);
            Util.punishForSwearing(Util.getOfflinePlayer(user));
            return;
        }

        // Handle spam in the message
        String modifiedMessage = Util.filterMessage(messagePart2);
        if (!modifiedMessage.equals(messagePart2)) {
            String username = Util.usernameOf(event.getAuthor());
            Util.notifyModified(username, messagePart2);
            event.setMinecraftMessage(Util.componentOf(messagePart1 + "> " + modifiedMessage));
        }
    }
}
