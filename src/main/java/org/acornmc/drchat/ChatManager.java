package org.acornmc.drchat;

import github.scarsz.discordsrv.DiscordSRV;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.*;

public class ChatManager {
    ConfigManager configManager;
    public static HashMap<OfflinePlayer, Integer> spamCheck = new HashMap<>();
    public static Set<OfflinePlayer> ecoCheck = new HashSet<>();
    static boolean chatIsFrozen = false;

    public ChatManager (ConfigManager configManager) {
        this.configManager = configManager;
    }

    public String fixSpacing(String message) {
        String[] words = message.split(" ");
        String newMessage = "";
        int i = 0;
        while (i < words.length) {
            if (words.length >= i+3 && words[i].length() == 1 && words[i+1].length() == 1 && words[i+2].length() == 1) {
                while (words.length > i && words[i].length() == 1) {
                    newMessage = newMessage + words[i];
                    i++;
                }
            } else {
                newMessage = newMessage + words[i];
                i++;
            }
            newMessage = newMessage + " ";
        }
        return newMessage.substring(0, newMessage.length() - 1);
    }

    public String fixCharacter(String message) {
        StringBuilder newMessage = new StringBuilder();
        int limit = configManager.get().getInt("checks.character.limit");
        for (int i = 0; i < limit && i < message.length(); i++) {
            newMessage.append(message.charAt(i));
        }
        for (int i = limit; i < message.length(); i++) {
            boolean allTheSame = true;
            for (int j = i-limit; j < i; j++) {
                if (message.charAt(j) != message.charAt(i)) {
                    allTheSame = false;
                    break;
                }
            }
            if (!allTheSame) {
                newMessage.append(message.charAt(i));
            } else {
                List<Character> ignoredCharacters = configManager.get().getCharacterList("checks.character.exceptions");
                if (ignoredCharacters.contains(message.charAt(i))) {
                    newMessage.append(message.charAt(i));
                }
            }
        }
        return newMessage.toString();
    }

    public String fixCapital(String message) {
        int uppercaseCount = 0;
        String colorlessMessage = ChatColor.stripColor(message);
        for (int i = 0; i < colorlessMessage.length(); i++) {
            if (Character.isUpperCase(colorlessMessage.charAt(i))) {
                uppercaseCount++;
            }
        }
        if (uppercaseCount > configManager.get().getInt("checks.capital.limit")) {
            boolean ignoreCapitalLinks = configManager.get().getBoolean("checks.capital.ignore-links");
            if (ignoreCapitalLinks) {
                String[] words = message.split(" ");
                String newMessage = "";
                for (String word : words) {
                    if (word.startsWith("http://") || word.startsWith("https://")) {
                        newMessage += word;
                    } else {
                        newMessage += word.toLowerCase();
                    }
                    newMessage += " ";
                }
                message = newMessage;
            } else {
                message = message.toLowerCase();
            }
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
        if (!spamCheck.containsKey(player)) {
            return false;
        }
        int limit = configManager.get().getInt("checks.frequency.limit");
        return spamCheck.get(player) >= limit;
    }

    public void increment(OfflinePlayer player) {
        int newCount = 1;
        if (spamCheck.containsKey(player)) {
            newCount = spamCheck.get(player) + 1;
        }
        spamCheck.put(player, newCount);
        int interval = configManager.get().getInt("checks.frequency.interval");
        new BukkitRunnable() {
            @Override
            public void run() {
                spamCheck.put(player, spamCheck.get(player) - 1);
                if (spamCheck.get(player) == 0) {
                    spamCheck.remove(player);
                }
            }
        }.runTaskLater(configManager.plugin, interval);
    }

    public void notifyModifiedMessage(String playerName, String message) {
        message = ChatColor.stripColor(message);
        List<String> notifyMessages = configManager.get().getStringList("messages.modify-notification");
        for (String notifyMessage : notifyMessages) {
            notifyMessage = ChatColor.translateAlternateColorCodes('&', notifyMessage);
            notifyMessage = notifyMessage.replace("%player%", playerName);
            notifyMessage = notifyMessage.replace("%original-message%", message);
            Bukkit.broadcast(notifyMessage, "drchat.notify.modify");
            Bukkit.getLogger().info(notifyMessage);
        }
    }

    public void notifyCancelledMessage(String playerName, String message) {
        message = ChatColor.stripColor(message);
        List<String> notifyMessages = configManager.get().getStringList("messages.cancel-notification");
        for (String notifyMessage : notifyMessages) {
            notifyMessage = ChatColor.translateAlternateColorCodes('&', notifyMessage);
            notifyMessage = notifyMessage.replace("%player%", playerName);
            notifyMessage = notifyMessage.replace("%original-message%", message);
            Bukkit.broadcast(notifyMessage, "drchat.notify.cancel");
            Bukkit.getLogger().info(notifyMessage);
        }
    }

    public void useTooFrequentCommands(OfflinePlayer player) {
        String playerName = player.getName();
        if (playerName == null) {
            playerName = "?";
        }
        List<String> stringList = configManager.get().getStringList("checks.frequency.commands");
        for (String command : stringList) {
            command = command.replace("%player%", playerName);
            final String finalCommand = command;
            Runnable runnable = () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCommand);
            Bukkit.getScheduler().runTask(configManager.plugin, runnable);
        }
    }

    public boolean hasSwear(String message) {
        List<String> swears = configManager.get().getStringList("checks.swear.words");
        String lowerCaseMessage = message.toLowerCase();
        for (String swear : swears) {
            if (lowerCaseMessage.contains(swear)) {
                return true;
            }
        }
        return false;
    }

    public void useSwearCommands(OfflinePlayer player) {
        String playerName = player.getName();
        if (playerName == null) {
            playerName = "?";
        }
        List<String> cmds = configManager.get().getStringList("checks.swear.commands");
        for (String command : cmds) {
            command = command.replace("%player%", playerName);
            final String finalCommand = command;
            Runnable runnable = () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCommand);
            Bukkit.getScheduler().runTask(configManager.plugin, runnable);
        }

    }

    public void reward(OfflinePlayer player) {
        if (!Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            return;
        }
        if (!ecoCheck.contains(player)) {
            ecoCheck.add(player);
            int interval = configManager.get().getInt("rewards.cooldown");
            new BukkitRunnable() {
                @Override
                public void run() {
                    ecoCheck.remove(player);
                }
            }.runTaskLater(configManager.plugin, interval);
            double money = configManager.get().getDouble("rewards.money");
            DrChat.getEconomy().depositPlayer(player, money);
        }
    }

    public static boolean chatIsFrozen() {
        return chatIsFrozen;
    }

    public static void toggleChatFreeze() {
        chatIsFrozen = !chatIsFrozen;
    }

    public void postSearchResults(String newMessage) {
        String trigger = configManager.get().getString("search.trigger");
        if (trigger != null) {
            if (newMessage.startsWith(trigger)) {
                newMessage = newMessage.substring(trigger.length());
                newMessage = newMessage.replace(" ", "%20");
                if (newMessage.length() >= 1) {
                    String regex = configManager.get().getString("search.regex");
                    if (regex != null && newMessage.matches(regex)) {
                        minecraftSearch(newMessage);
                        discordSearch(newMessage);
                    }
                }
            }
        }
    }

    public void minecraftSearch(String message) {
        String searchFormat = configManager.get().getString("messages.search.minecraft");
        if (searchFormat != null) {
            searchFormat = searchFormat.replace("%search%", message);
            String finalUrl = ChatColor.translateAlternateColorCodes('&', searchFormat);
            BukkitScheduler scheduler = configManager.plugin.getServer().getScheduler();
            scheduler.scheduleSyncDelayedTask(configManager.plugin, () ->
                    Bukkit.broadcastMessage(finalUrl), 2L);
        }
    }

    public void discordSearch(String message) {
        if (Bukkit.getPluginManager().isPluginEnabled("DiscordSRV")) {
            String searchFormat = configManager.get().getString("messages.search.discord");
            if (searchFormat != null) {
                String finalSearchFormat = searchFormat.replace("%search%", message);
                BukkitScheduler scheduler = configManager.plugin.getServer().getScheduler();
                String gameChannelName = configManager.get().getString("discord.channel-name");
                scheduler.scheduleSyncDelayedTask(configManager.plugin, () ->
                        DiscordSRV.getPlugin().getDestinationTextChannelForGameChannelName(gameChannelName).sendMessage(finalSearchFormat).queue(),
                        2L);;
            }
        }
    }
}
