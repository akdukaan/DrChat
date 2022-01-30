package org.acornmc.drchat;

import com.google.common.base.Throwables;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

public class Config {
    public static String LANGUAGE_FILE = "lang-en.yml";
    public static boolean ENABLE_BSTATS = true;
    public static List<String> SWEARS = Arrays.asList(".*(?i)nigger.*", ".*(?i)nigga.*");
    public static String DISCORD_TO_MC_FORMAT = "&x&e&d&8&0&a&7&l[Staff] &f%nickname% &9> &f%message%";
    public static String MC_TO_MC_FORMAT = "&x&e&d&8&0&a&7&l[Staff] &f%name% &7> &f%message%";
    public static long MUTED_ROLE_ID = 0;
    public static String SWEAR_PUNISHMENT = "ban %player% offensive language";
    public static String FREQUENCY_PUNISHMENT = "mute %player% 10m spam";
    public static ConfigurationSection PREFIXES = new YamlConfiguration();
    public static String MESSAGE_SPLITTER = "> ";
    public static boolean HANDLE_DEATH_EVENTS = false;
    public static int REWARD_INTERVAL = 300;
    public static double REWARD_AMOUNT = 0.00;
    private static YamlConfiguration config;

    private static void init() {
        LANGUAGE_FILE = getString("language-file", LANGUAGE_FILE);
        ENABLE_BSTATS = getBoolean("enable-bstats", ENABLE_BSTATS);
        SWEARS = getStringList("swears", SWEARS);
        DISCORD_TO_MC_FORMAT = getString("discord-to-mc-format", DISCORD_TO_MC_FORMAT);
        MC_TO_MC_FORMAT = getString("mc-to-mc-format", MC_TO_MC_FORMAT);
        MUTED_ROLE_ID = getLong("mute-role-id", MUTED_ROLE_ID);
        SWEAR_PUNISHMENT = getString("command-punishment-for-swears", SWEAR_PUNISHMENT);
        PREFIXES = getYamlConfiguration("prefixes", PREFIXES);
        MESSAGE_SPLITTER = getString("message-splitter", MESSAGE_SPLITTER);
        HANDLE_DEATH_EVENTS = getBoolean("handle-death-events", HANDLE_DEATH_EVENTS);
        REWARD_INTERVAL = getInt("reward-interval", REWARD_INTERVAL);
        REWARD_AMOUNT = getDouble("reward-amount", REWARD_AMOUNT);
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

    private static double getDouble(String path, double def) {
        config.addDefault(path, def);
        return config.getDouble(path, config.getDouble(path));
    }

    private static long getLong(String path, long def) {
        config.addDefault(path, def);
        return config.getLong(path, config.getLong(path));
    }

    private static ConfigurationSection getYamlConfiguration(String path, ConfigurationSection def) {
        config.addDefault(path, def);
        return config.getConfigurationSection(path);
    }

    private static BigDecimal getBigDecimal(String path, BigDecimal def) {
        config.addDefault(path, def);
        String string = config.getString(path, config.getString(path));
        if (string == null) return new BigDecimal(0);
        return new BigDecimal(string);
    }
}
