package org.acornmc.drchat;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class LoginListener implements Listener {

    @EventHandler
    public void onLogin(PlayerLoginEvent event) {
        if (!Config.HANDLE_LOGIN_EVENTS) return;
        Player player = event.getPlayer();
        if (!player.hasPermission("group.donor")) return;
        if (shouldRemovePrefix(player)) {
            removePrefix(player);
        }
        if (shouldRemoveNickname(player)) {
            removeNickname(player);
        }
    }

    public boolean shouldRemovePrefix(Player player) {
        return !player.hasPermission("group.vip");
    }

    public boolean shouldRemoveNickname(Player player) {
        if (hasColoredNick(player) && !player.hasPermission("group.vip")) return true;
        Essentials ess = EssentialsHook.getEssentials();
        if (ess == null) return false;
        User user = ess.getUser(player.getUniqueId());
        if (user == null) return false;
        return !user.getLastAccountName().equals(user.getName());
    }

    public boolean hasColoredNick(Player player) {
        Essentials ess = EssentialsHook.getEssentials();
        if (ess == null) return false;
        User user = ess.getUser(player.getUniqueId());
        if (user == null) return false;
        String nick = user.getNickname();
        if (nick == null) return false;
        return nick.contains(ChatColor.COLOR_CHAR + "");
    }

    public void removePrefix(Player player) {
        Plugin plugin = DrChat.getInstance();
        new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + player.getName() + " meta removeprefix 1");
            }
        }.runTask(plugin);
    }

    public void removeNickname(Player player) {
        Essentials ess = EssentialsHook.getEssentials();
        if (ess == null) return;
        ess.getUser(player.getUniqueId()).setNickname(player.getName());
    }
}
