package org.acornmc.drchat;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bstats.bukkit.Metrics;
import github.scarsz.discordsrv.DiscordSRV;

import java.util.HashMap;

public final class DrChat extends JavaPlugin {
    public static DrChat plugin;
    public static HashMap<Player, Integer> playerSet = new HashMap<>();
    ConfigManager configManager;

    @Override
    public void onEnable() {
        plugin = this;
        configManager = new ConfigManager(this);
        this.getServer().getPluginManager().registerEvents(new PlayerChatListener(configManager), this);
        if (Bukkit.getPluginManager().isPluginEnabled("DiscordSRV")) {
            DiscordSRV.api.subscribe(new DiscordSRVListener(configManager));
        }
        PluginCommand drchatCommand = getCommand("drchat");
        if (drchatCommand != null) {
            drchatCommand.setExecutor(new CommandDrChat(configManager));
        }
        int pluginId = 8683;
        new Metrics(this, pluginId);
    }

    @Override
    public void onDisable() {
        DiscordSRV.api.unsubscribe(new PlayerChatListener(configManager));
    }
}
