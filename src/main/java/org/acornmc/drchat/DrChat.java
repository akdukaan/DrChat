package org.acornmc.drchat;

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
            Bukkit.getLogger().info("[DrChat] DiscordSRV found!");
            DiscordSRV.api.subscribe(new DiscordSRVListener(configManager));
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
        }
    }
}
