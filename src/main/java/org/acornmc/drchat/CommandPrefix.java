package org.acornmc.drchat;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CommandPrefix implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            return false;
        }

        ConfigurationSection prefixes = Config.PREFIXES;
        for (String key : prefixes.getKeys(false)) {
            boolean hasAllPerms = validatePerms(sender, prefixes.getStringList(key + ".permissions"));
            if (hasAllPerms) {

                //Check regex
                String regex = prefixes.getString(key + ".regex");
                if (args[0].matches(regex)) {

                    // Execute commands
                    List<String> commands = prefixes.getStringList(key + ".commands");
                    for (String c : commands) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), c.replace("<player>", sender.getName()).replace("<argument>", args[0]));
                    }
                    sender.sendMessage("§cYour prefix color has been changed");
                    return true;
                }
            }
        }

        sender.sendMessage("§cInvalid prefix");
        return true;
    }

    public boolean validatePerms(CommandSender sender, @NotNull List<String> permissions) {
        for (String perm : permissions) {
            if (perm.startsWith("!")) {
                if (sender.hasPermission(perm.substring(1))) {
                    return false;
                }
            } else {
                if (!sender.hasPermission(perm)) {
                    return false;
                }
            }
        }
        return true;
    }



}
