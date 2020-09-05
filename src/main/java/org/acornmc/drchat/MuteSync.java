package org.acornmc.drchat;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Role;
import github.scarsz.discordsrv.objects.managers.AccountLinkManager;
import net.ess3.api.events.MuteStatusChangeEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.UUID;

public class MuteSync implements Listener {
    ConfigManager configManager;
    public MuteSync(ConfigManager configManager) {
        this.configManager = configManager;
    }

    @EventHandler
    public void muteStatus(MuteStatusChangeEvent event) {
        System.out.println("Player's name is " + event.getAffected());
        System.out.println("Player is muted: " + event.getValue());
        Player player = event.getAffected().getBase();
        if (event.getValue()) {
            muteOnDiscord(player);
            return;
        }
        unmuteOnDiscord(player);
    }

    public void muteOnDiscord(Player player) {
        AccountLinkManager alm = DiscordSRV.getPlugin().getAccountLinkManager();
        UUID uuid = player.getUniqueId();
        String id = alm.getDiscordId(uuid);
        String mutedID = configManager.get().getString("discord.mute-role-id");
        if (mutedID == null) {
            return;
        }
        Role mutedRole = DiscordSRV.getPlugin().getMainGuild().getRoleById(mutedID);
        if (mutedRole == null) {
            return;
        }
        DiscordSRV.getPlugin().getMainGuild().addRoleToMember(id, mutedRole).queue();
    }

    public void unmuteOnDiscord(Player player) {
        AccountLinkManager alm = DiscordSRV.getPlugin().getAccountLinkManager();
        UUID uuid = player.getUniqueId();
        String id = alm.getDiscordId(uuid);
        String mutedID = configManager.get().getString("discord.mute-role-id");
        if (mutedID == null) {
            return;
        }
        Role mutedRole = DiscordSRV.getPlugin().getMainGuild().getRoleById(mutedID);
        if (mutedRole == null) {
            return;
        }
        DiscordSRV.getPlugin().getMainGuild().removeRoleFromMember(id, mutedRole).queue();
    }
}
