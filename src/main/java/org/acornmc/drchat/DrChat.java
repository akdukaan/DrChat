package org.acornmc.drchat;

import github.scarsz.discordsrv.dependencies.jda.api.Permission;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Role;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.bstats.bukkit.Metrics;
import github.scarsz.discordsrv.DiscordSRV;

public final class DrChat extends JavaPlugin {
    public DrChat plugin;
    ConfigManager configManager;

    @Override
    public void onEnable() {
        plugin = this;
        configManager = new ConfigManager(this);
        this.getServer().getPluginManager().registerEvents(new PlayerChatListener(configManager), this);
        if (Bukkit.getPluginManager().isPluginEnabled("DiscordSRV")) {
            DiscordSRV.api.subscribe(new DiscordSRVListener(configManager));
            String roleId = configManager.get().getString("discord.lock-when-offline.role-id");
            if (roleId != null) {
                String channelId = configManager.get().getString("discord.lock-when-offline.channel-id");
                if (channelId != null) {
                    Role role = DiscordSRV.getPlugin().getMainGuild().getRoleById(roleId);
                    TextChannel textChannel = DiscordSRV.getPlugin().getMainGuild().getTextChannelById(channelId);
                    Permission permission = Permission.MESSAGE_WRITE;
                    if (role != null && textChannel != null && !role.hasPermission(textChannel, permission)) {
                        textChannel.getManager().getChannel().createPermissionOverride(role).setAllow(permission).queue();
                    }
                }
            }
        }
        PluginCommand drchatCommand = getCommand("drchat");
        if (drchatCommand != null) {
            drchatCommand.setExecutor(new CommandDrChat(configManager));
        }
        PluginCommand staffchatCommand = getCommand("staffchat");
        if (staffchatCommand != null) {
            staffchatCommand.setExecutor(new CommandStaffchat(configManager));
        }
        int pluginId = 8683;
        new Metrics(this, pluginId);
    }

    @Override
    public void onDisable() {
        if (Bukkit.getPluginManager().isPluginEnabled("DiscordSRV")) {
            DiscordSRV.api.unsubscribe(new DiscordSRVListener(configManager));
            String roleId = configManager.get().getString("discord.lock-when-offline.role-id");
            if (roleId != null) {
                String channelId = configManager.get().getString("discord.lock-when-offline.channel-id");
                if (channelId != null) {
                    Role role = DiscordSRV.getPlugin().getMainGuild().getRoleById(roleId);
                    TextChannel textChannel = DiscordSRV.getPlugin().getMainGuild().getTextChannelById(channelId);
                    Permission permission = Permission.MESSAGE_WRITE;
                    if (role != null && textChannel != null && role.hasPermission(textChannel, permission)) {
                        textChannel.getManager().getChannel().createPermissionOverride(role).setDeny(permission).queue();
                    }
                }
            }
        }
    }
}
