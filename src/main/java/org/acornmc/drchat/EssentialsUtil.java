package org.acornmc.drchat;

import com.earth2me.essentials.IEssentials;
import org.bukkit.Bukkit;

import java.util.UUID;

public class EssentialsUtil {
    IEssentials iess = (IEssentials) Bukkit.getPluginManager().getPlugin("Essentials");

    public boolean isMuted(UUID uuid) {
        return iess.getUser(uuid).isMuted();
    }
}
