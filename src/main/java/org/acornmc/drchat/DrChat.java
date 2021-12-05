package org.acornmc.drchat;

import github.scarsz.discordsrv.DiscordSRV;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.bstats.bukkit.Metrics;

public final class DrChat extends JavaPlugin {

    private static DrChat instance;

    @Override
    public void onEnable() {
        instance = this;
        Config.reload(this);
        Lang.reload(this);

        if (Config.ENABLE_BSTATS) {
            new Metrics(this, 8683);
        }

        Bukkit.getPluginManager().registerEvents(new ChatListener(), this);
        Bukkit.getPluginManager().registerEvents(new LoginListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerDeathListener(), this);

        PluginCommand drchatCommand = getCommand("drchat");
        if (drchatCommand != null) {
            drchatCommand.setExecutor(new CommandDrchat());
        }
        PluginCommand staffchatCommand = getCommand("staffchat");
        if (staffchatCommand != null) {
            staffchatCommand.setExecutor(new CommandStaffchat());
        }
        PluginCommand prefixCommand = getCommand("prefix");
        if (prefixCommand != null) {
            prefixCommand.setExecutor(new CommandPrefix());
        }

        if (Bukkit.getPluginManager().isPluginEnabled("DiscordSRV")) {
            Util.log("DiscordSRV found!");
            DiscordSRV.api.subscribe(new DiscordSRVHook());
            Util.log("DiscordSRV connected!");
            if (Bukkit.getPluginManager().isPluginEnabled("Essentials")) {
                Util.log("Essentials found!");
                this.getServer().getPluginManager().registerEvents(new EssentialsHook(), this);
            } else {
                Util.log("Essentials not found!");
            }
        } else {
            Util.log("DiscordSRV not found!");
        }
    }

    @Override
    public void onDisable() {
    }

    public static DrChat getInstance() {
        return instance;
    }
}
