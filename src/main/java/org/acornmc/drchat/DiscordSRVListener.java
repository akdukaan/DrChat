package org.acornmc.drchat;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.*;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Role;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import github.scarsz.discordsrv.objects.managers.AccountLinkManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

public class DiscordSRVListener extends ChatManager {
    EssentialsUtil essentialsUtil;
    LitebansUtil litebansUtil;

    public DiscordSRVListener(ConfigManager configManager) {
        super(configManager);
        if (Bukkit.getPluginManager().isPluginEnabled("Essentials")) {
            essentialsUtil = new EssentialsUtil(configManager);
        }
        if (Bukkit.getPluginManager().isPluginEnabled("LiteBans")) {
            litebansUtil = new LitebansUtil(configManager);
        }
    }

    @Subscribe
    public void discordMessagePostProcess(DiscordGuildMessagePostProcessEvent event) {
        TextChannel staffchatChannel = DiscordSRV.getPlugin().getDestinationTextChannelForGameChannelName("staff-chat");
        if (staffchatChannel != null && event.getChannel().getId().equals(staffchatChannel.getId())) {
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
        String spamExemptRole = configManager.get().getString("discord.spam-bypass-role-id");
        boolean spamExempt = event.getMember().getRoles().stream().anyMatch(role -> role.getId().equals(spamExemptRole));
        if (chatIsFrozen()) {
            String freezeExemptRole = configManager.get().getString("discord.spam-bypass-role-id");
            boolean freezeExempt = event.getMember().getRoles().stream().anyMatch(role -> role.getId().equals(freezeExemptRole));
            if (!freezeExempt) {
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
            if (muteSync) {
                if ((Bukkit.getPluginManager().isPluginEnabled("Essentials") && essentialsUtil.isMuted(uuid)) ||
                        (Bukkit.getPluginManager().isPluginEnabled("LiteBans") && litebansUtil.isMuted(uuid))) {
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
        String barrier = configManager.get().getString("discord.barrier");
        String originalFullMessage = event.getProcessedMessage();
        if (barrier != null) {
            int barrierPosition = originalFullMessage.indexOf(barrier);
            String preBarrier = "";
            String postBarrier = originalFullMessage;
            if (barrierPosition != -1) {
                preBarrier = originalFullMessage.substring(0, barrierPosition);
                postBarrier = originalFullMessage.substring(barrierPosition + barrier.length());
            }
            if (!spamExempt) {
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
                if (barrierPosition == -1) {
                    event.setProcessedMessage(postBarrier);
                } else {
                    event.setProcessedMessage(preBarrier + barrier + postBarrier);
                }
            }
            String searchRole = configManager.get().getString("discord.search-role-id");
            boolean searchAllowed = event.getMember().getRoles().stream().anyMatch(role -> role.getId().equals(searchRole));
            if (searchAllowed) {
                postBarrier = ChatColor.stripColor(postBarrier);
                postSearchResults(postBarrier);
            }
        }
    }

    @Subscribe
    public void discordMessage(DiscordGuildMessageReceivedEvent event) {
        String id = event.getMember().getUser().getId();
        AccountLinkManager alm = DiscordSRV.getPlugin().getAccountLinkManager();
        OfflinePlayer player = null;
        UUID uuid = null;
        if (alm != null) {
            uuid = alm.getUuid(id);
            if (uuid != null) {
                player = Bukkit.getOfflinePlayer(uuid);
            }
        }
        if (player != null) {
            String mutedID = configManager.get().getString("discord.mute-role-id");
            if (mutedID != null) {
                Role mutedRole = DiscordSRV.getPlugin().getMainGuild().getRoleById(mutedID);
                if (mutedRole != null) {
                    boolean hasMutedRole = event.getMember().getRoles().stream().anyMatch(role -> role.getId().equals(mutedID));
                    if (!hasMutedRole) {
                        if (Bukkit.getPluginManager().isPluginEnabled("Essentials") && essentialsUtil.isMuted(uuid)) {
                            DiscordSRV.getPlugin().getMainGuild().addRoleToMember(id, mutedRole).queue();
                        }
                        if ((Bukkit.getPluginManager().isPluginEnabled("Essentials") && essentialsUtil.isMuted(uuid)) ||
                                (Bukkit.getPluginManager().isPluginEnabled("LiteBans") && litebansUtil.isMuted(uuid))) {
                            event.getMessage().delete().queue();
                            return;
                        }
                    }
                }
            }
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