package org.acornmc.drchat;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.event.EventBus;
import net.luckperms.api.event.node.NodeAddEvent;
import net.luckperms.api.event.node.NodeRemoveEvent;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.types.InheritanceNode;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import static org.bukkit.Bukkit.getServer;

public class LuckPermsListener {

    private final DrChat plugin;
    private final LuckPerms luckPerms;

    public LuckPermsListener(DrChat plugin, LuckPerms luckPerms) {
        this.plugin = plugin;
        this.luckPerms = luckPerms;
    }

    public void register() {
        EventBus eventBus = this.luckPerms.getEventBus();
        eventBus.subscribe(this.plugin, NodeRemoveEvent.class, this::onNodeRemove);
        eventBus.subscribe(this.plugin, NodeAddEvent.class, this::onNodeAdd);

    }

    private void onNodeRemove(NodeRemoveEvent event) {
        User target = (User) event.getTarget();
        OfflinePlayer player = getServer().getOfflinePlayer(target.getUniqueId());
        Node node = event.getNode();
        if (node instanceof InheritanceNode) {
            String groupName = ((InheritanceNode) node).getGroupName();
            // TODO make this a list of strings instead of these
            if (groupName.equalsIgnoreCase("vip") || groupName.equalsIgnoreCase("dragon")) {
                // TODO make this a list of commands
                this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + player.getName() + " meta removeprefix 1");
                });
            }
        }
    }

    private void onNodeAdd(NodeAddEvent event) {
        User target = (User) event.getTarget();
        OfflinePlayer player = getServer().getOfflinePlayer(target.getUniqueId());
        Node node = event.getNode();
        if (node instanceof InheritanceNode) {
            // TODO make this a list of commands
            this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + player.getName() + " meta removeprefix 1");
            });
        }
    }
}
