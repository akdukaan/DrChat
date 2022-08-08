package org.acornmc.drchat;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CommandStaffchat implements @Nullable CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("drchat.staffchat")) {
            sender.sendMessage(Lang.COMMAND_NO_PERMISSION);
            return true;
        }
        if (!(sender instanceof Player)) {
            Bukkit.getLogger().info("This command can only be used by players");
            return true;
        }
        Player player = (Player) sender;
        if (args.length < 1) {
            Util.toggleStaffchat(player);
            return true;
        }
        String message = String.join(" ", args);
        Util.sendStaffchatMCToMC(player, message);
        Util.sendStaffchatMCToDiscord(player, message);
        return true;
    }
}
