package org.acornmc.drchat;

import net.ess3.api.events.MuteStatusChangeEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class EssentialsHook implements Listener {

    @EventHandler
    public void updateMuteStatus(MuteStatusChangeEvent event) {
        Player player = event.getAffected().getBase();
        if (event.getValue()) {
            Util.discordMute(player);
            return;
        }
        Util.discordUnmute(player);
    }
}
