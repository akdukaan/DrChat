package org.acornmc.drchat;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

public class ConfigManager {
    Plugin plugin;
    FileConfiguration fileConfiguration;

    public ConfigManager(Plugin plugin) {
        plugin.saveDefaultConfig();
        plugin.getConfig().options().copyDefaults(true);
        plugin.saveConfig();

        this.plugin = plugin;
        this.fileConfiguration = plugin.getConfig();
    }

    // returns the current file configuration
    public FileConfiguration get() {
        return fileConfiguration;
    }

    // call this function to reload the stored configuration
    public void reload() {
        this.plugin.reloadConfig();
        this.fileConfiguration = plugin.getConfig();
    }
}