package org.acornmc.drchat;


import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.*;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;

public class DiscordSRVHook {

    @Subscribe
    public void postProcess(DiscordGuildMessagePostProcessEvent event) {
        TextChannel staffchatChannel = DiscordSRV.getPlugin().getDestinationTextChannelForGameChannelName("staff-chat");
        if (staffchatChannel != null && event.getChannel().getId().equals(staffchatChannel.getId())) {
            Util.sendStaffchatDiscordToMC(event.getMember(), event.getMessage().getContentDisplay());
            event.setCancelled(true);
            return;
        }

        String eventMessage = Util.legacyOf(event.getMinecraftMessage());
        String messagePart1 = eventMessage.split("> ", 2)[0];
        String messagePart2 = eventMessage.split("> ", 2)[1];

        if (Util.containsSwears(messagePart2)) {
            String username = Util.usernameOf(event.getAuthor());
            Util.notifyCancelled(username, messagePart2);
            event.setCancelled(true);
            return;
        }

        String modifiedMessage = Util.modifySpam(messagePart2);
        if (!modifiedMessage.equals(messagePart2)) {
            String username = Util.usernameOf(event.getAuthor());
            Util.notifyModified(username, messagePart2);
            event.setMinecraftMessage(Util.componentOf(messagePart1 + "> " + modifiedMessage));
        }
    }
}
