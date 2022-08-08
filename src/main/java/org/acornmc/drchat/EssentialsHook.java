package org.acornmc.drchat;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.IEssentials;
import github.scarsz.discordsrv.dependencies.jda.api.entities.User;
import net.ess3.api.events.MuteStatusChangeEvent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.UUID;

public class EssentialsHook implements Listener {

    @EventHandler
    public void updateMuteStatus(MuteStatusChangeEvent event) {
        Player player = event.getAffected().getBase();
        if (event.getValue()) {
            DiscordSRVHook.discordMute(player);
            return;
        }
        DiscordSRVHook.discordUnmute(player);
    }

    public static Essentials getEssentials() {
        return (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
    }

    public static boolean isMuted(User user) {
        Essentials ess = EssentialsHook.getEssentials();
        if (ess == null) return false;
        UUID uuid = Util.uuidOf(user);
        if (uuid == null) return false;
        com.earth2me.essentials.User essUser = ess.getUser(uuid);
        if (essUser == null) return false;
        return essUser.isMuted();
    }

    public static boolean isBanned(User user) {
        UUID uuid = Util.uuidOf(user);
        if (uuid == null) return false;
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        return player.isBanned();
    }

}
