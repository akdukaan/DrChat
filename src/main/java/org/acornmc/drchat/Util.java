package org.acornmc.drchat;

import com.earth2me.essentials.IEssentials;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Member;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Role;
import github.scarsz.discordsrv.dependencies.jda.api.entities.User;
import github.scarsz.discordsrv.objects.managers.AccountLinkManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {
    private static Set<UUID> staffchatToggled = new HashSet<>();

    /**
     * Log a message to the console
     *
     * @param message
     */
    public static void log(String message) {
        Bukkit.getConsoleSender().sendMessage(colorize("[DrChat] "+ message));
    }

    /**
     * Colorize a String
     *
     * @param string String to colorize
     * @return Colorized String
     */
    public static String colorize(String string) {
        if (string == null) {
            return "";
        }
        Pattern hexPattern = Pattern.compile("&#([A-Fa-f0-9]{6})");
        Matcher matcher = hexPattern.matcher(string);
        while (matcher.find()) {
            final String before = string.substring(0, matcher.start());
            final String after = string.substring(matcher.end());
            ChatColor hexColor = ChatColor.valueOf(matcher.group().substring(1));
            string = before + hexColor + after;
            matcher = hexPattern.matcher(string);
        }
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    /**
     * Sends a message to a recipient
     *
     * @param recipient Recipient of message
     * @param message   Message to send
     */
    public static void send(CommandSender recipient, String message) {
        if (recipient != null) {
            for (String part : colorize(message).split("\n")) {
                recipient.sendMessage(part);
            }
        }
    }

    public static String legacyOf(Component component) {
        return LegacyComponentSerializer.legacySection().serialize(component);
    }

    public static Component componentOf(String string) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(string);
    }

    public static int countCaps(String string) {
        int numCaps = 0;
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            if (Character.isUpperCase(c)) {
                numCaps++;
            }
        }
        return numCaps;
    }

    public static String removeCaps(String string) {
        return string.toLowerCase();
    }

    public static int countMaxRepeats(String string) {
        int maxRepeatLength = 0;
        int thisRepeatLength = 0;
        for (int i = 0; i < string.length() - 1; i++) {
            if (!Character.isDigit(string.charAt(i)) && string.charAt(i) == string.charAt(i+1)) {
                thisRepeatLength++;
            } else {
                if (thisRepeatLength > maxRepeatLength) {
                    maxRepeatLength = thisRepeatLength;
                }
                thisRepeatLength = 1;
            }
        }
        return Math.max(maxRepeatLength, thisRepeatLength);
    }

    public static String removeRepeats(String string) {
        if (string.length() == 0) {
            return string;
        }
        char currentChar = string.charAt(0);
        int repeatCounter = 1;
        for (int i = 1; i < string.length(); i++) {
            if (string.charAt(i) == currentChar) {
                repeatCounter++;
                if (repeatCounter >= 3) {
                    string = string.substring(0, i) + string.substring(i + 1);
                    i--;
                }
            } else {
                currentChar = string.charAt(i);
                repeatCounter = 1;
            }
        }
        return string;
    }

    public static boolean containsSwears(String string) {
        List<String> swears = Config.SWEARS;
        for (String swear : swears) {
            if (string.contains(swear)) {
                return true;
            }
        }
        return false;
    }

    public static String modifySpam(String string) {
        if (Util.countCaps(string) > 15) {
            string = Util.removeCaps(string);
        }

        if (Util.countMaxRepeats(string) > 4) {
            string = Util.removeRepeats(string);
        }

        return string;
    }

    public static void notifyCancelled(String author, String message) {
        Bukkit.broadcast(Util.componentOf(colorize("&7" + author + "'s message was cancelled")), "drchat.notify");
        Bukkit.broadcast(Util.componentOf(colorize("&7Original: " + message)), "drchat.notify");
        log("&7" + author + "'s message was cancelled");
        log("&7Original: " + message);
    }

    public static void notifyModified(String author, String message) {
        Bukkit.broadcast(Util.componentOf(colorize("&7" + author + "'s message was modified")), "drchat.notify");
        Bukkit.broadcast(Util.componentOf(colorize("&7Original: " + message)), "drchat.notify");
    }

    public static void sendStaffchatDiscordToMC(Member member, String message) {
        String discordToMc = Config.DISCORD_TO_MC_FORMAT;
        discordToMc = discordToMc.replace("%name%", member.getUser().getName());
        discordToMc = discordToMc.replace("%nickname%", member.getEffectiveName());
        discordToMc = discordToMc.replace("%message%", message);
        Bukkit.broadcast(Util.componentOf(discordToMc), "drchat.staffchat");

    }

    public static void sendStaffchatMCToMC(Player player, String message) {
        String mcToMc = Config.MC_TO_MC_FORMAT;
        Bukkit.broadcast(Util.componentOf(mcToMc
                .replace("%displayname%", player.getDisplayName())
                .replace("%name%", player.getName())
                .replace("%message%", message)), "drchat.staffchat");
    }

    public static void sendStaffchatMCToDiscord(Player player, String message) {
        message = ChatColor.translateAlternateColorCodes('&', message);
        DiscordSRV.getPlugin().processChatMessage(player, message, "staff-chat", false);
    }

    public static void toggleStaffchat(Player player) {
        if (isStaffchatToggled(player.getUniqueId())) {
            send(player, Lang.STAFFCHAT_TOGGLED_OFF);
            staffchatToggled.remove(player.getUniqueId());

        } else {
            send(player, Lang.STAFFCHAT_TOGGLED_ON);
            staffchatToggled.add(player.getUniqueId());

        }
    }

    public static boolean isStaffchatToggled(UUID uuid) {
        return staffchatToggled.contains(uuid);
    }

    public static UUID uuidOf(User user) {
        String id = user.getId();
        AccountLinkManager alm = DiscordSRV.getPlugin().getAccountLinkManager();
        if (alm != null) {
            return alm.getUuid(id);
        }
        return null;
    }

    public static String usernameOf(User user) {
        String id = user.getId();
        AccountLinkManager alm = DiscordSRV.getPlugin().getAccountLinkManager();
        if (alm != null) {
            UUID uuid = alm.getUuid(id);
            if (uuid != null) {
                OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
                return player.getName();
            }
        }
        return "?";
    }

    public static String getMemberID(OfflinePlayer player) {
        AccountLinkManager alm = DiscordSRV.getPlugin().getAccountLinkManager();
        UUID uuid = player.getUniqueId();
        return alm.getDiscordId(uuid);

    }

    public static void discordMute(OfflinePlayer player) {
        Role muteRole = DiscordSRV.getPlugin().getMainGuild().getRoleById(Config.MUTED_ROLE_ID);
        if (muteRole == null) return;
        DiscordSRV.getPlugin().getMainGuild().addRoleToMember(getMemberID(player), muteRole).queue();
    }

    public static void discordUnmute(OfflinePlayer player) {
        Role muteRole = DiscordSRV.getPlugin().getMainGuild().getRoleById(Config.MUTED_ROLE_ID);
        if (muteRole == null) return;
        DiscordSRV.getPlugin().getMainGuild().addRoleToMember(getMemberID(player), muteRole).queue();
    }

    public static boolean isMuted(UUID uuid) {
        IEssentials iess = (IEssentials) Bukkit.getPluginManager().getPlugin("Essentials");
        if (iess == null) return false;
        return iess.getUser(uuid).isMuted();
    }
}
