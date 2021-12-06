package org.acornmc.drchat;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

public class LoginListener implements Listener {

    @EventHandler
    public void onLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPermission("drchat.resetnickonnamechange")) return;
        Essentials ess = EssentialsHook.getEssentials();
        if (ess == null) return;
        User user = ess.getUser(player.getUniqueId());
        if (user == null) return;
        if (user.getLastAccountName().equals(user.getName())) return;
        user.setNickname(player.getName());
    }
}
