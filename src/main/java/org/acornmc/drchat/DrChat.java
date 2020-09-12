package org.acornmc.drchat;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bstats.bukkit.Metrics;
import github.scarsz.discordsrv.DiscordSRV;

import java.util.logging.Logger;

public final class DrChat extends JavaPlugin {
    public DrChat plugin;
    ConfigManager configManager;
    private static final Logger log = Logger.getLogger("Minecraft");
    private static Economy econ = null;
    private static Permission perms = null;
    private static Chat chat = null;
    @Override
    public void onEnable() {
        plugin = this;
        configManager = new ConfigManager(this);
        if (!setupEconomy() ) {
            log.info(String.format("[%s] Vault not found!", getDescription().getName()));
        } else {
            log.info(String.format("[%s] Vault found!", getDescription().getName()));
            setupPermissions();
            setupChat();
        }
        this.getServer().getPluginManager().registerEvents(new PlayerChatListener(configManager), this);
        if (Bukkit.getPluginManager().isPluginEnabled("DiscordSRV")) {
            log.info(String.format("[%s] DiscordSRV found!", getDescription().getName()));
            DiscordSRV.api.subscribe(new DiscordSRVListener(configManager));
            if (Bukkit.getPluginManager().isPluginEnabled("Essentials")) {
                log.info(String.format("[%s] Essentials found!", getDescription().getName()));
                this.getServer().getPluginManager().registerEvents(new EssentialsMuteSync(configManager), this);
            } else {
                log.info(String.format("[%s] Essentials not found!", getDescription().getName()));
            }
        } else {
            log.info(String.format("[%s] DiscordSRV not found!", getDescription().getName()));
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
        log.info(String.format("[%s] Disabled Version %s", getDescription().getName(), getDescription().getVersion()));
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    private boolean setupChat() {
        RegisteredServiceProvider<Chat> rsp = getServer().getServicesManager().getRegistration(Chat.class);
        chat = rsp.getProvider();
        return chat != null;
    }

    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        perms = rsp.getProvider();
        return perms != null;
    }
    public static Economy getEconomy() {
        return econ;
    }
}
