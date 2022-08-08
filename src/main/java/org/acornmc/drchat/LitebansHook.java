package org.acornmc.drchat;

import java.util.UUID;

import github.scarsz.discordsrv.dependencies.jda.api.entities.User;
import litebans.api.Database;

public class LitebansHook {

    public static boolean isMuted(User user) {
        UUID uuid = Util.uuidOf(user);
        if (DrChat.getInstance().getServer().getPluginManager().isPluginEnabled("LiteBans")) {
            return Database.get().isPlayerMuted(uuid, null);
        }
        return false;
    }

    public static boolean isBanned(User user) {
        UUID uuid = Util.uuidOf(user);
        if (DrChat.getInstance().getServer().getPluginManager().isPluginEnabled("LiteBans")) {
            return Database.get().isPlayerBanned(uuid, null);
        }
        return false;
    }
}
