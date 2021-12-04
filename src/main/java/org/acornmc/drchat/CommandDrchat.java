package org.acornmc.drchat;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CommandDrchat implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("drchat.reload")) {
                Util.send(sender, Lang.COMMAND_NO_PERMISSION);
                return true;
            }
            Config.reload(DrChat.getInstance());
            Lang.reload(DrChat.getInstance());
            Util.send(sender, Lang.CONFIG_RELOADED);
            return true;
        }
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> list = new ArrayList<>();
        if (sender.hasPermission("drchat.reload")) {
            list.add("reload");
        }
        return StringUtil.copyPartialMatches(args[args.length - 1], list, new ArrayList<>());
    }
}
