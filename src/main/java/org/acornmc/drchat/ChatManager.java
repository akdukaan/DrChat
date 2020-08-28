package org.acornmc.drchat;

import org.bukkit.OfflinePlayer;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;

public class ChatManager {
    ConfigManager configManager;
    public static HashMap<OfflinePlayer, Integer> recentlyTalked = new HashMap<>();

    public ChatManager (ConfigManager configManager) {
        this.configManager = configManager;
    }

    public String fixSpacing(String message) {
        String[] words = message.split(" ");
        if (message.length() >= 5 && message.length() < 2 * words.length) {
            message = message.replace(" ", "");
        }
        return message;
    }

    public String fixCharacter(String message) {
        StringBuilder newMessage = new StringBuilder();
        int limit = configManager.get().getInt("checks.character.limit");
        for (int i = 0; i < limit && i < message.length(); i++) {
            newMessage.append(message.charAt(i));
        }
        for (int i = limit; i < message.length(); i++) {
            boolean allthesame = true;
            for (int j = i-limit; j < i; j++) {
                if (message.charAt(j) != message.charAt(i)) {
                    allthesame = false;
                    break;
                }
            }
            if (!allthesame) {
                newMessage.append(message.charAt(i));
            }
        }
        return newMessage.toString();
    }

    public String fixCapital(String message) {
        int uppercaseCount = 0;
        for (int i = 0; i < message.length(); i++) {
            if (Character.isUpperCase(message.charAt(i))) {
                uppercaseCount++;
            }
        }
        if (uppercaseCount > configManager.get().getInt("checks.capital.limit")) {
            message = message.toLowerCase();
        }
        return message;
    }

    public String fixFont(String message) {
        return message
                .replace("\uff41", "a")
                .replace("\uff42", "b")
                .replace("\uff43", "c")
                .replace("\uff44", "d")
                .replace("\uff45", "e")
                .replace("\uff46", "f")
                .replace("\uff47", "g")
                .replace("\uff48", "h")
                .replace("\uff49", "i")
                .replace("\uff4a", "j")
                .replace("\uff4b", "k")
                .replace("\uff4c", "l")
                .replace("\uff4d", "m")
                .replace("\uff4e", "n")
                .replace("\uff4f", "o")
                .replace("\uff50", "p")
                .replace("\uff51", "q")
                .replace("\uff52", "r")
                .replace("\uff53", "s")
                .replace("\uff54", "t")
                .replace("\uff55", "u")
                .replace("\uff56", "v")
                .replace("\uff57", "w")
                .replace("\uff58", "x")
                .replace("\uff59", "y")
                .replace("\uff5a", "z")
                .replace("\uff21", "A")
                .replace("\uff22", "B")
                .replace("\uff23", "C")
                .replace("\uff24", "D")
                .replace("\uff25", "E")
                .replace("\uff26", "F")
                .replace("\uff27", "G")
                .replace("\uff28", "H")
                .replace("\uff29", "I")
                .replace("\uff2a", "J")
                .replace("\uff2b", "K")
                .replace("\uff2c", "L")
                .replace("\uff2d", "M")
                .replace("\uff2e", "N")
                .replace("\uff2f", "O")
                .replace("\uff30", "P")
                .replace("\uff31", "Q")
                .replace("\uff32", "R")
                .replace("\uff33", "S")
                .replace("\uff34", "T")
                .replace("\uff35", "U")
                .replace("\uff36", "V")
                .replace("\uff37", "W")
                .replace("\uff38", "X")
                .replace("\uff39", "Y")
                .replace("\uff3a", "Z")
                .replace("\uff1f", "?")
                .replace("\uff0e", ".")
                .replace("\uff3b", "[")
                .replace("\uff3d", "]")
                .replace("\uff5b", "{")
                .replace("\uff5d", "}")
                .replace("\uff0b", "+")
                .replace("\uff1d", "=")
                .replace("\uff01", "!")
                .replace("\uff20", "@")
                .replace("\uff03", "#")
                .replace("\uff04", "$")
                .replace("\uff05", "%")
                .replace("\uff3e", "^")
                .replace("\uff06", "&")
                .replace("\uff0a", "*")
                .replace("\uff0d", "-")
                .replace("\uff3f", "_")
                .replace("\uff5c", "|")
                .replace("\uff1b", ";")
                .replace("\uff1a", ":")
                .replace("\uff40", "`")
                .replace("\uff07", "'")
                .replace("\uff11", "1")
                .replace("\uff12", "2")
                .replace("\uff13", "3")
                .replace("\uff14", "4")
                .replace("\uff15", "5")
                .replace("\uff16", "6")
                .replace("\uff17", "7")
                .replace("\uff18", "8")
                .replace("\uff19", "9")
                .replace("\uff10", "0")
                .replace("\uff08", "(")
                .replace("\uff09", ")")
                .replace("\uff0c", ",");
    }

    public boolean isTooFrequent(OfflinePlayer player) {
        if (!recentlyTalked.containsKey(player)) {
            increment(player);
            return false;
        }
        int limit = configManager.get().getInt("checks.frequency.limit");
        if (recentlyTalked.get(player) < limit) {
            increment(player);
            return false;
        }
        return true;
    }

    public void increment(OfflinePlayer player) {
        int newCount = 1;
        if (recentlyTalked.containsKey(player)) {
            newCount = recentlyTalked.get(player) + 1;
        }
        recentlyTalked.put(player, newCount);
        int interval = configManager.get().getInt("checks.frequency.interval");
        new BukkitRunnable() {
            @Override
            public void run() {
                recentlyTalked.put(player, recentlyTalked.get(player) - 1);
                if (recentlyTalked.get(player) == 0) {
                    recentlyTalked.remove(player);
                }
            }
        }.runTaskLater(configManager.plugin, interval);
    }

    public int getCount(OfflinePlayer player) {
        return recentlyTalked.get(player);
    }
}
