package org.acornmc.drchat;

import com.google.common.base.Throwables;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class Lang {
    private static YamlConfiguration config;

    public static String COMMAND_NO_PERMISSION = "&4You do not have permission for that command.";
    public static String STAFFCHAT_TOGGLED_ON = "&aYou toggled on staffchat";
    public static String STAFFCHAT_TOGGLED_OFF = "&aYou toggled off staffchat";
    public static String CONFIG_RELOADED = "&aConfig reloaded!";
    public static String CHANGE_PREFIX_COLOR_SUCCESS = "&cYour prefix color has been changed";
    public static String CHANGE_PREFIX_COLOR_FAIL = "&cInvalid prefix";
    public static String CHAT_IS_FROZEN = "&aChat is now frozen";
    public static String CHAT_IS_NOT_FROZEN = "&aChat is no longer frozen";
    public static String CANT_TALK_NOW = "&cYou can't talk right now";

    private static void init() {
        COMMAND_NO_PERMISSION = getString("command-no-permission", COMMAND_NO_PERMISSION);
        STAFFCHAT_TOGGLED_ON = getString("staffchat-toggled-on", STAFFCHAT_TOGGLED_ON);
        STAFFCHAT_TOGGLED_OFF = getString("staffchat-toggled-off", STAFFCHAT_TOGGLED_OFF);
        CONFIG_RELOADED = getString("config-reloaded", CONFIG_RELOADED);
        CHANGE_PREFIX_COLOR_SUCCESS = getString("change-prefix-color-success", CHANGE_PREFIX_COLOR_SUCCESS);
        CHANGE_PREFIX_COLOR_FAIL = getString("change-prefix-color-fail", CHANGE_PREFIX_COLOR_FAIL);
        CHAT_IS_FROZEN = getString("chat-is-frozen", CHAT_IS_FROZEN);
        CHAT_IS_NOT_FROZEN = getString("chat-is-not-frozen", CHAT_IS_NOT_FROZEN);
        CANT_TALK_NOW = getString("cant-talk-now", CANT_TALK_NOW);
    }

    // ########################################################

    /**
     * Reload the language file
     */
    public static void reload(Plugin plugin) {
        File configFile = new File(plugin.getDataFolder(), Config.LANGUAGE_FILE);
        config = new YamlConfiguration();
        try {
            config.load(configFile);
        } catch (IOException ignore) {
        } catch (InvalidConfigurationException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Could not load " + Config.LANGUAGE_FILE + ", please correct your syntax errors", ex);
            throw Throwables.propagate(ex);
        }
        config.options().header("This is the main language file for " + plugin.getName());
        config.options().copyDefaults(true);

        Lang.init();

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
}
