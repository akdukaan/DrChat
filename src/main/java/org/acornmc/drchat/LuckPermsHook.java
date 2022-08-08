package org.acornmc.drchat;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;

import java.util.UUID;

public class LuckPermsHook {
    private static LuckPerms luckPerms = null;

    public static boolean hasPermission(UUID uuid, String permission) {
        if (luckPerms == null) return true;
        User user = luckPerms.getUserManager().getUser(uuid);
        if (user == null) return false;
        return user.getCachedData().getPermissionData().checkPermission(permission).asBoolean();
    }

    public static void setApi(LuckPerms api) {
        luckPerms = api;
    }
}
