package org.acornmc.drchat;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.*;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Role;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import github.scarsz.discordsrv.objects.managers.AccountLinkManager;
import net.kyori.adventure.text.Component;
import github.scarsz.discordsrv.dependencies.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class DiscordSRVListener extends ChatManager {
    EssentialsUtil essentialsUtil;

    public DiscordSRVListener(ConfigManager configManager) {
        super(configManager);
        if (Bukkit.getPluginManager().isPluginEnabled("Essentials")) {
            essentialsUtil = new EssentialsUtil(configManager);
        }
    }

    @Subscribe
    public void discordMessagePreProcess(DiscordGuildMessagePreProcessEvent event) {
        TextChannel staffchatChannel = DiscordSRV.getPlugin().getDestinationTextChannelForGameChannelName("staff-chat");
        if (staffchatChannel != null && event.getChannel().getId().equals(staffchatChannel.getId())) {
            event.setCancelled(true);
        }
    }

    /*
    @Subscribe
    public void discordMessagePreBroadcast(DiscordGuildMessagePreBroadcastEvent e) {
        String plainMessage = LegacyComponentSerializer.legacySection().serialize(e.getMessage());
        String plainMessage2 = LegacyComponentSerializer.legacyAmpersand().serialize(e.getMessage());
        System.out.println(plainMessage);
        System.out.println(plainMessage2);
        String senderName = plainMessage.split(" ")[1].replace("⭐", "");
        System.out.println(senderName);
        UUID messageSender = Bukkit.getOfflinePlayer(senderName).getUniqueId();
        for (CommandSender s : e.getRecipients()) {
            if (s instanceof Player) {
                Player p = (Player) s;
                if (essentialsUtil.isIgnoring(p.getUniqueId(), messageSender)) {
                    e.getRecipients().remove(s);
                }
            }
        }
    } */

    @Subscribe
    public void discordMessagePostProcess(DiscordGuildMessagePostProcessEvent event) {
        String id = event.getMember().getUser().getId();
        AccountLinkManager alm = DiscordSRV.getPlugin().getAccountLinkManager();
        boolean linked = false;
        OfflinePlayer player = null;
        UUID uuid = null;
        if (alm != null) {
            uuid = alm.getUuid(id);
            if (uuid != null) {
                linked = true;
            }
        }
        String playerName;
        if (linked) {
            player = Bukkit.getOfflinePlayer(uuid);
            playerName = player.getName();
        } else {
            playerName = "?";
        }
        String spamExemptRole = configManager.get().getString("discord.spam-bypass-role-id");
        boolean spamExempt = event.getMember().getRoles().stream().anyMatch(role -> role.getId().equals(spamExemptRole));
        if (chatIsFrozen()) {
            String freezeExemptRole = configManager.get().getString("discord.frozen-bypass-role-id");
            boolean freezeExempt = event.getMember().getRoles().stream().anyMatch(role -> role.getId().equals(freezeExemptRole));
            if (!freezeExempt) {
                event.setCancelled(true);
                String emote = configManager.get().getString("discord.reactions.cancelled");
                if (emote != null) {
                    event.getMessage().addReaction(emote).queue();
                }
                return;
            }
        }

        if (linked && !spamExempt) {
            boolean muteSync = configManager.get().getBoolean("discord.mute-sync");
            String emote = configManager.get().getString("discord.reactions.cancelled");
            if (muteSync) {
                if (Bukkit.getPluginManager().isPluginEnabled("Essentials") && essentialsUtil.isMuted(uuid)) {
                    notifyCancelledMessage(playerName, event.getMessage().getContentDisplay());
                    boolean discordDeleteCancelled = configManager.get().getBoolean("discord.delete-cancelled");
                    if (discordDeleteCancelled) {
                        event.getMessage().delete().queue();
                    } else if (emote != null) {
                        event.getMessage().addReaction(emote).queue();
                    }
                    event.setCancelled(true);
                    return;
                }
            }
            if (isTooFrequent(player)) {
                if (emote != null) {
                    event.getMessage().addReaction(emote).queue();
                }
                notifyCancelledMessage(playerName, event.getMessage().getContentDisplay());
                event.setCancelled(true);
                useTooFrequentCommands(player);
                return;
            }
            increment(player);
        }
    }

    @Subscribe
    public void discordMessage(DiscordGuildMessageReceivedEvent event) {
        String id = event.getMember().getUser().getId();
        AccountLinkManager alm = DiscordSRV.getPlugin().getAccountLinkManager();
        OfflinePlayer player = null;
        UUID uuid;
        if (alm != null) {
            uuid = alm.getUuid(id);
            if (uuid != null) {
                player = Bukkit.getOfflinePlayer(uuid);
            }
        }
        if (player != null) {
            boolean rewardAllChats = configManager.get().getBoolean("rewards.discord.all-messages");
            if (rewardAllChats) {
                reward(player);
            } else {
                boolean rewardDiscordToMC = configManager.get().getBoolean("rewards.discord.mc-messages");
                String gameChannelName = configManager.get().getString("discord.channel-name");
                TextChannel mcChannel = DiscordSRV.getPlugin().getDestinationTextChannelForGameChannelName(gameChannelName);
                if (mcChannel != null) {
                    String mcChannelId = mcChannel.getId();
                    if (rewardDiscordToMC && event.getChannel().getId().equals(mcChannelId)) {
                        reward(player);
                    }
                }
            }
        }
        TextChannel staffchatChannel = DiscordSRV.getPlugin().getDestinationTextChannelForGameChannelName("staff-chat");
        if (staffchatChannel != null && event.getChannel().getId().equals(staffchatChannel.getId())) {
            String discordToMc = configManager.get().getString("messages.staffchat.discord-to-mc-format");
            if (discordToMc != null) {
                discordToMc = discordToMc.replace("%nickname%", event.getMember().getEffectiveName());
                discordToMc = discordToMc.replace("%message%", event.getMessage().getContentDisplay());
                discordToMc = ChatColor.translateAlternateColorCodes('&', discordToMc);
                Bukkit.broadcast(discordToMc, "drchat.staffchat");
            }
        }
    }
}