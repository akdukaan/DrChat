package org.acornmc.drchat;

import litebans.api.Database;

import java.util.UUID;

public class LitebansUtil {
     ConfigManager configManager;

     public LitebansUtil(ConfigManager configManager) {
        this.configManager = configManager;
     }

     public boolean isMuted(UUID uuid) {
         return Database.get().isPlayerMuted(uuid, null);
     }
}
