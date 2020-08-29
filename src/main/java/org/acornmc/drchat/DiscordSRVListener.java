package org.acornmc.drchat;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.*;
import github.scarsz.discordsrv.objects.managers.AccountLinkManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

public class DiscordSRVListener extends ChatManager {
    EssentialsUtil essentialsUtil;

    public DiscordSRVListener(ConfigManager configManager) {
        super(configManager);
        if (Bukkit.getPluginManager().isPluginEnabled("Essentials")) {
            essentialsUtil = new EssentialsUtil();
        }
    }

    @Subscribe
    public void discordMessageProcessed(DiscordGuildMessagePostProcessEvent event) {
        String id = event.getMember().getUser().getId();
        AccountLinkManager alm = DiscordSRV.getPlugin().getAccountLinkManager();
        boolean linked = false;
        UUID uuid = null;
        if (alm != null) {
            uuid = alm.getUuid(id);
            if (uuid != null) {
                linked = true;
            }
        }

        OfflinePlayer player = null;
        String spamExemptRole = configManager.get().getString("discord.bypass-role");
        boolean spamExempt = event.getMember().getRoles().stream().anyMatch(role -> role.getName().equals(spamExemptRole));
        if (linked && !spamExempt) {
            player = Bukkit.getOfflinePlayer(uuid);
            String playerName = player.getName();
            boolean muteSync = configManager.get().getBoolean("discord.mute-sync");
            String emote = configManager.get().getString("discord.reactions.cancelled");
            if (muteSync && Bukkit.getPluginManager().isPluginEnabled("Essentials")) {
                if (essentialsUtil.isMuted(uuid)) {
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
                useTooFrequentCommand(player);
                return;
            }
            increment(player);
        }
        if (!spamExempt) {
            String originalFullMessage = event.getProcessedMessage();
            String barrier = configManager.get().getString("discord.barrier");
            if (barrier != null) {
                int barrierPosition = originalFullMessage.indexOf(barrier);
                String postBarrier = event.getProcessedMessage().substring(barrierPosition + barrier.length());
                String preBarrier = event.getProcessedMessage().substring(0, barrierPosition);
                boolean checkFont = configManager.get().getBoolean("discord.checks.font");
                boolean checkSpacing = configManager.get().getBoolean("discord.checks.spacing");
                boolean checkCapital = configManager.get().getBoolean("discord.checks.capital");
                boolean checkCharacter = configManager.get().getBoolean("discord.checks.character");
                String originalPostBarrier = postBarrier;
                if (checkFont) {
                    postBarrier = fixFont(postBarrier);
                }
                if (checkSpacing) {
                    postBarrier = fixSpacing(postBarrier);
                }
                if (checkCapital) {
                    postBarrier = fixCapital(postBarrier);
                }
                if (checkCharacter) {
                    postBarrier = fixCharacter(postBarrier);
                }
                if (!originalPostBarrier.equals(postBarrier)) {
                    String emote = configManager.get().getString("discord.reactions.modified");
                    if (emote != null) {
                        event.getMessage().addReaction(emote).queue();
                    }
                    String playerName = "unknown";
                    if (player != null) {
                        playerName = player.getName();
                    }
                    notifyModifiedMessage(playerName, originalPostBarrier);
                }
                event.setProcessedMessage(preBarrier + barrier + postBarrier);
            }
        }
    }

    @Subscribe
    public void discordStaffMessage(DiscordGuildMessageReceivedEvent event) {
        String staffchatChannel = configManager.get().getString("discord.staffchat.channel-id");
        if (event.getChannel().getId().equals(staffchatChannel)) {
            String discordToMc = configManager.get().getString("discord.staffchat.discord-to-mc-format");
            if (discordToMc != null) {
                discordToMc = discordToMc.replace("%nickname%", event.getMember().getEffectiveName());
                discordToMc = discordToMc.replace("%message%", event.getMessage().getContentDisplay());
                discordToMc = ChatColor.translateAlternateColorCodes('&', discordToMc);
                Bukkit.broadcast(discordToMc, "drchat.staffchat");
            }
        }
    }
}