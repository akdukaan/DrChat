package org.acornmc.drchat;

import litebans.api.Database;

import java.util.UUID;

public class LitebansUtil {
     ConfigManager configManager;

     public LitebansUtil(ConfigManager configManager) {
        this.configManager = configManager;
     }

     public boolean isMuted(UUID uuid) {
         System.out.println("Checking to see if player with uuid " + uuid + " is muted");
         return Database.get().isPlayerMuted(uuid, null);
     }
}
