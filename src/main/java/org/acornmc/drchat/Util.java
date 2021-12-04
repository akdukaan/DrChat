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

import java.io.IOException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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
        Bukkit.getConsoleSender().sendMessage(colorize("[DrChat] " + message));
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
                if (repeatCounter >= 3 && !Character.isDigit(currentChar)) {
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

    public static String filterMessage(String string) {
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
                .replace("%displayname%", Util.legacyOf(player.displayName()))
                .replace("%name%", player.getName())
                .replace("%message%", message)), "drchat.staffchat");
    }

    public static void sendStaffchatMCToDiscord(Player player, String message) {
        message = colorize(message);
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

    public static OfflinePlayer getOfflinePlayer(User user) {
        UUID uuid = uuidOf(user);
        if (uuid == null) return null;
        return Bukkit.getOfflinePlayer(uuid);
    }

    public static boolean canUseDiscord(User user) {
        return EssentialsHook.isBanned(user) ||
                EssentialsHook.isMuted(user) ||
                LitebansHook.isBanned(user) ||
                LitebansHook.isMuted(user);
    }

    public static void punishForSwearing(OfflinePlayer player) {
        if (player.getName() == null) return;
        final String command = Config.SWEAR_PUNISHMENT
                .replace("%player%", player.getName());
        Runnable runnable = () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        Bukkit.getScheduler().runTask(DrChat.getInstance(), runnable);
    }

    public static String removeFancyChat(String string) {
        return string
                .replace("ᵃ", "a")
                .replace("ᵇ", "b")
                .replace("ᶜ", "c")
                .replace("ᵈ", "d")
                .replace("ᵉ", "e")
                .replace("ᶠ", "f")
                .replace("ᵍ", "g")
                .replace("ʰ", "h")
                .replace("ᶦ", "i")
                .replace("ʲ", "j")
                .replace("ᵏ", "k")
                .replace("ˡ", "l")
                .replace("ᵐ", "m")
                .replace("ⁿ", "n")
                .replace("ᵒ", "o")
                .replace("ᵖ", "p")
                .replace("ᵠ", "q")
                .replace("ʳ", "r")
                .replace("ˢ", "s")
                .replace("ᵗ", "t")
                .replace("ᵘ", "u")
                .replace("ᵛ", "v")
                .replace("ʷ", "w")
                .replace("ˣ", "x")
                .replace("ʸ", "y")
                .replace("ᶻ", "z")
                .replace("ａ", "a")
                .replace("ｂ", "b")
                .replace("ｃ", "c")
                .replace("ｄ", "d")
                .replace("ｅ", "e")
                .replace("ｆ", "f")
                .replace("ｇ", "g")
                .replace("ｈ", "h")
                .replace("ｉ", "i")
                .replace("ｊ", "j")
                .replace("ｋ", "k")
                .replace("ｌ", "l")
                .replace("ｍ", "m")
                .replace("ｎ", "n")
                .replace("ｏ", "o")
                .replace("ｐ", "p")
                .replace("ｑ", "q")
                .replace("ｒ", "r")
                .replace("ｓ", "s")
                .replace("ｔ", "t")
                .replace("ｕ", "u")
                .replace("ｖ", "v")
                .replace("ｗ", "w")
                .replace("ｘ", "x")
                .replace("ｙ", "y")
                .replace("ｚ", "z")
                .replace("Ａ", "a")
                .replace("Ｂ", "b")
                .replace("Ｃ", "c")
                .replace("Ｄ", "d")
                .replace("Ｅ", "e")
                .replace("Ｆ", "f")
                .replace("Ｇ", "g")
                .replace("Ｈ", "h")
                .replace("Ｉ", "i")
                .replace("Ｊ", "j")
                .replace("Ｋ", "k")
                .replace("Ｌ", "l")
                .replace("Ｍ", "m")
                .replace("Ｎ", "n")
                .replace("Ｏ", "o")
                .replace("Ｐ", "p")
                .replace("Ｑ", "q")
                .replace("Ｒ", "r")
                .replace("Ｓ", "s")
                .replace("Ｔ", "t")
                .replace("Ｕ", "u")
                .replace("Ｖ", "v")
                .replace("Ｗ", "w")
                .replace("Ｘ", "x")
                .replace("Ｙ", "y")
                .replace("Ｚ", "z")
                .replace("ᴀ", "a")
                .replace("ʙ", "b")
                .replace("ᴄ", "c")
                .replace("ᴅ", "d")
                .replace("ᴇ", "e")
                .replace("ꜰ", "f")
                .replace("ɢ", "g")
                .replace("ʜ", "h")
                .replace("ɪ", "i")
                .replace("ᴊ", "j")
                .replace("ᴋ", "k")
                .replace("ʟ", "l")
                .replace("ᴍ", "m")
                .replace("ɴ", "n")
                .replace("ᴏ", "o")
                .replace("ᴘ", "p")
                .replace("ʀ", "r")
                .replace("ꜱ", "s")
                .replace("ᴛ", "t")
                .replace("ᴜ", "u")
                .replace("ᴠ", "v")
                .replace("ᴡ", "w")
                .replace("ʏ", "y")
                .replace("ᴢ", "z")
                .replace("ⓐ", "a")
                .replace("ⓑ", "b")
                .replace("ⓒ", "c")
                .replace("ⓓ", "d")
                .replace("ⓔ", "e")
                .replace("ⓕ", "f")
                .replace("ⓖ", "g")
                .replace("ⓗ", "h")
                .replace("ⓘ", "i")
                .replace("ⓙ", "j")
                .replace("ⓚ", "k")
                .replace("ⓛ", "l")
                .replace("ⓜ", "m")
                .replace("ⓝ", "n")
                .replace("ⓞ", "o")
                .replace("ⓟ", "p")
                .replace("ⓠ", "q")
                .replace("ⓡ", "r")
                .replace("ⓢ", "s")
                .replace("ⓣ", "t")
                .replace("ⓤ", "u")
                .replace("ⓥ", "v")
                .replace("ⓦ", "w")
                .replace("ⓧ", "x")
                .replace("ⓨ", "y")
                .replace("ⓩ", "z")
                .replace("Ⓐ", "a")
                .replace("Ⓑ", "b")
                .replace("Ⓒ", "c")
                .replace("Ⓓ", "d")
                .replace("Ⓔ", "e")
                .replace("Ⓕ", "f")
                .replace("Ⓖ", "g")
                .replace("Ⓗ", "h")
                .replace("Ⓘ", "i")
                .replace("Ⓙ", "j")
                .replace("Ⓚ", "k")
                .replace("Ⓛ", "l")
                .replace("Ⓜ", "m")
                .replace("Ⓝ", "n")
                .replace("Ⓞ", "o")
                .replace("Ⓟ", "p")
                .replace("Ⓠ", "q")
                .replace("Ⓡ", "r")
                .replace("Ⓢ", "s")
                .replace("Ⓣ", "t")
                .replace("Ⓤ", "u")
                .replace("Ⓥ", "v")
                .replace("Ⓦ", "w")
                .replace("Ⓧ", "x")
                .replace("Ⓨ", "y")
                .replace("？", "?")
                .replace("．", ".")
                .replace("［", "[")
                .replace("］", "]")
                .replace("｛", "{")
                .replace("｝", "}")
                .replace("＋", "+")
                .replace("＝", "=")
                .replace("！", "!")
                .replace("＠", "@")
                .replace("＃", "#")
                .replace("＄", "$")
                .replace("％", "%")
                .replace("＾", "^")
                .replace("＆", "&")
                .replace("＊", "*")
                .replace("－", "-")
                .replace("＿", "_")
                .replace("｜", "|")
                .replace("；", ";")
                .replace("：", ":")
                .replace("｀", "`")
                .replace("＇", "'")
                .replace("１", "1")
                .replace("２", "2")
                .replace("３", "3")
                .replace("４", "4")
                .replace("５", "5")
                .replace("６", "6")
                .replace("７", "7")
                .replace("８", "8")
                .replace("９", "9")
                .replace("０", "0")
                .replace("（", "(")
                .replace("）", ")")
                .replace("，", ",");
    }
}
