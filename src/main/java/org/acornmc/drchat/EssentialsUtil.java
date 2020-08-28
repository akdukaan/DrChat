package org.acornmc.drchat;

import com.earth2me.essentials.Essentials;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class EssentialsUtil {
    Essentials essentials = JavaPlugin.getPlugin(Essentials.class);

    public boolean isMuted(UUID uuid) {
        return essentials.getUser(uuid).isMuted();
    }
}
