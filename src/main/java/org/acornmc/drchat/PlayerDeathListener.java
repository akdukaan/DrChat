package org.acornmc.drchat;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerDeathListener implements Listener {

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!Config.HANDLE_DEATH_EVENTS) return;
        Component deathMessage = event.deathMessage();
        if (deathMessage == null) return;
        event.deathMessage(null);

        Player killed = event.getPlayer();
        killed.sendMessage(deathMessage);
        Player killer = killed.getKiller();
        if (killer != null && !killer.getName().equals(killed.getName())) {
            killer.sendMessage(deathMessage);
        }
    }
}
