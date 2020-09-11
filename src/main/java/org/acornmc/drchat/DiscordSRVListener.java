package org.acornmc.drchat;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.*;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
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
            essentialsUtil = new EssentialsUtil(configManager);
        }
    }

    @Subscribe
    public void discordMessagePostProcess(DiscordGuildMessagePostProcessEvent event) {
        String staffchatChannelId = DiscordSRV.getPlugin().getDestinationTextChannelForGameChannelName("staff-chat").getId();
        if (event.getChannel().getId().equals(staffchatChannelId)) {
            event.setCancelled(true);
            return;
        }
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

        String spamExemptRole = configManager.get().getString("discord.bypass-role");
        boolean spamExempt = event.getMember().getRoles().stream().anyMatch(role -> role.getName().equals(spamExemptRole));
        if (chatIsFrozen()) {
            if (!spamExempt) {
                event.setCancelled(true);
                String emote = configManager.get().getString("discord.reactions.cancelled");
                if (emote != null) {
                    event.getMessage().addReaction(emote).queue();
                }
            }
        }
        if (linked && !spamExempt) {
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
                useTooFrequentCommands(player);
                return;
            }
            increment(player);
        }
        if (!spamExempt) {
            String originalFullMessage = event.getProcessedMessage();
            String barrier = configManager.get().getString("discord.barrier");
            if (barrier != null) {
                int barrierPosition = originalFullMessage.indexOf(barrier);
                String preBarrier = "";
                String postBarrier = event.getProcessedMessage();
                if (barrierPosition != -1) {
                    preBarrier = event.getProcessedMessage().substring(0, barrierPosition);
                    postBarrier = event.getProcessedMessage().substring(barrierPosition + barrier.length());
                }
                boolean checkFont = configManager.get().getBoolean("discord.checks.font");
                boolean checkSpacing = configManager.get().getBoolean("discord.checks.spacing");
                boolean checkCapital = configManager.get().getBoolean("discord.checks.capital");
                boolean checkCharacter = configManager.get().getBoolean("discord.checks.character");
                boolean checkSwear = configManager.get().getBoolean("discord.checks.swear");
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
                if (checkSwear && hasSwear(postBarrier)) {
                    event.setCancelled(true);
                    notifyCancelledMessage(playerName, event.getProcessedMessage());
                    if (player != null) {
                        useSwearCommands(player);
                    }
                }
                if (!originalPostBarrier.equals(postBarrier)) {
                    String emote = configManager.get().getString("discord.reactions.modified");
                    if (emote != null) {
                        event.getMessage().addReaction(emote).queue();
                    }
                    notifyModifiedMessage(playerName, originalPostBarrier);
                }
                event.setProcessedMessage(preBarrier + barrier + postBarrier);
            }
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
            boolean rewardAllChats = configManager.get().getBoolean("reward.discord.all-messages");
            if (rewardAllChats) {
                reward(player);
            } else {
                boolean rewardDiscordToMC = configManager.get().getBoolean("reward.discord.mc-messages");
                 String mcChannelId = DiscordSRV.getPlugin().getDestinationTextChannelForGameChannelName("global").getId();
                if (rewardDiscordToMC && event.getChannel().getId().equals(mcChannelId)) {
                    reward(player);
                }
            }
        }
        TextChannel staffchatChannel = DiscordSRV.getPlugin().getDestinationTextChannelForGameChannelName("staff-chat");
        if (event.getChannel().getId().equals(staffchatChannel.getId())) {
            String discordToMc = configManager.get().getString("messages.staffchat.discord-to-mc-format");
            if (discordToMc != null) {
                discordToMc = discordToMc.replace("%nickname%", event.getMember().getEffectiveName());
                discordToMc = discordToMc.replace("%message%", event.getMessage().getContentDisplay());
                discordToMc = ChatManager.convertHex(discordToMc);
                discordToMc = ChatColor.translateAlternateColorCodes('&', discordToMc);
                Bukkit.broadcast(discordToMc, "drchat.staffchat");
            }
        }
    }
}