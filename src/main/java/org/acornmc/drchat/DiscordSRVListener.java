package org.acornmc.drchat;


import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.*;
import github.scarsz.discordsrv.objects.managers.AccountLinkManager;
import org.bukkit.Bukkit;
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
            if (muteSync && Bukkit.getPluginManager().isPluginEnabled("Essentials")) {
                if (essentialsUtil.isMuted(uuid)) {
                    notifyCancelledMessage(playerName, event.getMessage().getContentRaw());
                    String emote = configManager.get().getString("discord.cancelled-reaction");
                    if (emote != null) {
                        event.getMessage().addReaction(emote).queue();
                    }
                    event.setCancelled(true);
                    return;
                }
            }

            if (isTooFrequent(player)) {
                String emote = configManager.get().getString("discord.cancelled-reaction");
                if (emote != null) {
                    event.getMessage().addReaction(emote).queue();
                }
                notifyCancelledMessage(playerName, event.getMessage().getContentRaw());
                event.setCancelled(true);
                useTooFrequentCommand(player);
                return;
            }
            increment(player);
        }
        String originalMessage = event.getProcessedMessage();
        String originalMessageRaw = event.getMessage().getContentRaw();
        if (!spamExempt) {
            boolean checkFont = configManager.get().getBoolean("discord.checks.font");
            boolean checkSpacing = configManager.get().getBoolean("discord.checks.spacing");
            boolean checkCapital = configManager.get().getBoolean("discord.checks.capital");
            boolean checkCharacter = configManager.get().getBoolean("discord.checks.character");
            if (checkFont && !fixFont(originalMessage).equals(originalMessage)) {
                event.setProcessedMessage(fixFont(event.getProcessedMessage()));
            }
            if (checkSpacing && !fixSpacing(originalMessage).equals(originalMessage)) {
                event.setProcessedMessage(fixSpacing(event.getProcessedMessage()));
            }
            if (checkCapital && !fixCapital(originalMessage).equals(originalMessage)) {
                event.setProcessedMessage(fixCapital(event.getProcessedMessage()));
            }
            if (checkCharacter && !fixCharacter(originalMessage).equals(originalMessage)) {
                event.setProcessedMessage(fixCharacter(event.getProcessedMessage()));
            }
            if (!originalMessage.equals(event.getProcessedMessage())) {
                String emote = configManager.get().getString("discord.modified-reaction");
                if (emote != null) {
                    event.getMessage().addReaction(emote).queue();
                }
                String playerName = "unknown";
                if (player != null) {
                    playerName = player.getName();
                }
                notifyModifiedMessage(playerName, originalMessageRaw);
            }
        }
    }
}