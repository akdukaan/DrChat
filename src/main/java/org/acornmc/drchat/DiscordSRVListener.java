package org.acornmc.drchat;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.*;
import github.scarsz.discordsrv.objects.managers.AccountLinkManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

public class DiscordSRVListener extends ChatManager{

    public DiscordSRVListener(ConfigManager configManager) {
        super(configManager);
    }

    @Subscribe
    public void discordMessageProcessed(DiscordGuildMessagePostProcessEvent event) {
        String id = event.getMember().getUser().getId();
        AccountLinkManager alm = DiscordSRV.getPlugin().getAccountLinkManager();
        boolean linked = true;
        if (alm == null) {
            linked = false;
        }
        UUID uuid = null;
        if (alm != null) {
            uuid = alm.getUuid(id);
        }
        if (uuid == null) {
            linked = false;
        }
        if (linked) {
            OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
            if (isTooFrequent(player)) {
                event.setCancelled(true);
                return;
            }
            increment(player);
            System.out.println("incremented player " + player.getName() +" to " + getCount(player));
        }
        event.setProcessedMessage(fixFont(event.getProcessedMessage()));
        event.setProcessedMessage(fixSpacing(event.getProcessedMessage()));
        event.setProcessedMessage(fixCapital(event.getProcessedMessage()));
        event.setProcessedMessage(fixCharacter(event.getProcessedMessage()));
    }
}