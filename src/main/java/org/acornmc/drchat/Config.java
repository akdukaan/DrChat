package org.acornmc.drchat;

import com.google.common.base.Throwables;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

public class Config {
    public static String LANGUAGE_FILE = "lang-en.yml";
    public static boolean ENABLE_BSTATS = true;
    public static List<String> SWEARS = Arrays.asList("nigger", "nigga");
    public static String DISCORD_TO_MC_FORMAT = "&x&e&d&8&0&a&7&l[Staff] &f%nickname% &9> &f%message%";
    public static String MC_TO_MC_FORMAT = "&x&e&d&8&0&a&7&l[Staff] &f%name% &7> &f%message%";
    public static int MUTED_ROLE_ID = 0;
    private static YamlConfiguration config;

    private static void init() {
        LANGUAGE_FILE = getString("language-file", LANGUAGE_FILE);
        ENABLE_BSTATS = getBoolean("enable-bstats", ENABLE_BSTATS);
        SWEARS = getStringList("swears", SWEARS);
        DISCORD_TO_MC_FORMAT = getString("discord-to-mc-format", DISCORD_TO_MC_FORMAT);
        MC_TO_MC_FORMAT = getString("mc-to-mc-format", MC_TO_MC_FORMAT);
    }

    // ########################################################

    /**
     * Reload the configuration file
     */
    public static void reload(Plugin plugin) {
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        config = new YamlConfiguration();
        try {
            config.load(configFile);
        } catch (IOException ignore) {
        } catch (InvalidConfigurationException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Could not load config.yml, please correct your syntax errors", ex);
            throw Throwables.propagate(ex);
        }
        config.options().header("This is the configuration file for " + plugin.getName());
        config.options().copyDefaults(true);

        Config.init();

        try {
            config.save(configFile);
        } catch (IOException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Could not save " + configFile, ex);
        }
    }

    private static String getString(String path, String def) {
        config.addDefault(path, def);
        return config.getString(path, config.getString(path));
    }

    private static boolean getBoolean(String path, boolean def) {
        config.addDefault(path, def);
        return config.getBoolean(path, config.getBoolean(path));
    }

    private static List<String> getStringList(String path, List<String> def) {
        config.addDefault(path, def);
        return config.getStringList(path);
    }

    private static int getInt(String path, int def) {
        config.addDefault(path, def);
        return config.getInt(path, config.getInt(path));
    }
}
