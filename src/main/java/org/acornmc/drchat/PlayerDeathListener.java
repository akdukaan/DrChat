package org.acornmc.drchat;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerDeathListener implements Listener {

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!Config.HANDLE_DEATH_EVENTS) return;
        Player killer = event.getPlayer().getKiller();
        Player killed = event.getPlayer();
        String message = Util.legacyOf(event.deathMessage());
        Util.send(killer, message);
        Util.send(killed, message);
        event.setCancelled(true);
    }
}
