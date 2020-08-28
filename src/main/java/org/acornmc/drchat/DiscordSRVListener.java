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
        boolean linked = true;
        if (alm == null) {
            linked = false;
        }
        UUID uuid = null;
        if (alm != null) {
            uuid = alm.getUuid(id);
        }
        if (uuid == null) {
            linked = false;
        }
        OfflinePlayer player = null;
        String spamExemptRole = configManager.get().getString("discord.bypass-role");
        boolean spamExempt = event.getMember().getRoles().stream().anyMatch(role -> role.getName().equals(spamExemptRole));
        if (linked && !spamExempt) {
            player = Bukkit.getOfflinePlayer(uuid);
            boolean muteSync = configManager.get().getBoolean("discord.mute-sync");
            if (muteSync && Bukkit.getPluginManager().isPluginEnabled("Essentials")) {
                if (essentialsUtil.isMuted(uuid)) {
                    notifyCancelledMessage(player, event.getMessage().getContentRaw());
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
                notifyCancelledMessage(player, event.getMessage().getContentRaw());
                event.setCancelled(true);
                useTooFrequentCommand(player);
                return;
            }
            increment(player);
        }
        String originalMessage = event.getProcessedMessage();
        if (!spamExempt) {
            boolean checkFont = configManager.get().getBoolean("discord.checks.font");
            boolean checkSpacing = configManager.get().getBoolean("discord.checks.spacing");
            boolean checkCapital = configManager.get().getBoolean("discord.checks.capital");
            boolean checkCharacter = configManager.get().getBoolean("discord.checks.character");
            if (checkFont) {
                event.setProcessedMessage(fixFont(event.getProcessedMessage()));
            }
            if (checkSpacing) {
                event.setProcessedMessage(fixSpacing(event.getProcessedMessage()));
            }
            if (checkCapital) {
                event.setProcessedMessage(fixCapital(event.getProcessedMessage()));
            }
            if (checkCharacter) {
                event.setProcessedMessage(fixCharacter(event.getProcessedMessage()));
            }
            if (!originalMessage.equals(event.getProcessedMessage())) {
                String emote = configManager.get().getString("discord.modified-reaction");
                if (emote != null) {
                    event.getMessage().addReaction(emote).queue();
                }
                notifyModifiedMessage(player, originalMessage);
            }
        }
    }
}